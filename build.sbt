import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._
import BuildHelper._
import Dependencies._

val protobufCompilerVersion = "3.13.0"

val MimaPreviousVersion = "0.11.0-M1"

inThisBuild(
  List(
    scalaVersion := Scala212,
    javacOptions ++= List("-target", "8", "-source", "8"),
    organization := "com.thesamet.scalapb",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    ConsoleHelper.welcomeMessage
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")

lazy val sharedNativeSettings = List(
  nativeLinkStubs := true // for utest
)

lazy val root: Project =
  project
    .in(file("."))
    .settings(
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    )
    .aggregate(protocGenScala.agg)
    .aggregate(
      lenses.projectRefs ++
        runtime.projectRefs ++
        grpcRuntime.projectRefs ++
        compilerPlugin.projectRefs ++
        proptest.projectRefs ++
        scalapbc.projectRefs: _*
    )

lazy val runtime = (projectMatrix in file("scalapb-runtime"))
  .defaultAxes()
  .dependsOn(lenses)
  .settings(commonSettings)
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      protobufJava          % "protobuf",
      munit.value           % "test",
      munitScalaCheck.value % "test",
      commonsCodec          % "test",
      protobufJavaUtil      % "test"
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    Compile / unmanagedResourceDirectories += (LocalRootProject / baseDirectory).value / "protobuf",
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:globalFilters=avaGenerateEqualsAndHash in class .* is deprecated",
                           "-P:silencer:lineContentFilters=import scala.collection.compat._"
                         )
                       else Nil),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("*Extension*"),
      ProblemFilters.exclude[Problem]("scalapb.options.*"),
      ProblemFilters.exclude[FinalMethodProblem]("*.parseFrom")
    )
  )
  .jvmPlatform(
    scalaVersions = Seq(Scala212, Scala213, Dotty),
    settings = Seq(
      libraryDependencies ++= Seq(
        protobufJava
      ),
      Compile / PB.targets ++= Seq(
        PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value
      ),
      PB.protocVersion := versions.protobuf,
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "jvm-native",
      Compile / PB.protoSources := Seq(
        (LocalRootProject / baseDirectory).value / "protobuf"
      )
    )
  )
  .jsPlatform(
    scalaVersions = Seq(Scala212, Scala213),
    settings = Seq(
      libraryDependencies += protobufRuntimeScala.value,
      scalajsSourceMaps,
      Compile / unmanagedResourceDirectories += (LocalRootProject / baseDirectory).value / "third_party",
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "js-native",
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )
  .nativePlatform(
    scalaVersions = Seq(Scala212, Scala213),
    settings = sharedNativeSettings ++ Seq(
      libraryDependencies += protobufRuntimeScala.value,
      Compile / unmanagedResourceDirectories += (LocalRootProject / baseDirectory).value / "third_party",
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "js-native",
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "jvm-native",
      Test / sources ~= { files =>
        // TODO
        val exclude = Set(
          "TokenizerSpec.scala",
          "ParserSpec.scala",
          "FileDescriptorSpec.scala"
        )
        files.filterNot(f => exclude(f.getName))
      }
    )
  )

lazy val runtimeJVM2_12 = runtime.jvm(Scala212)

lazy val grpcRuntime = (projectMatrix in file("scalapb-runtime-grpc"))
  .defaultAxes()
  .dependsOn(runtime)
  .settings(commonSettings)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213, Dotty))
  .settings(
    name := "scalapb-runtime-grpc",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      grpcStub,
      grpcProtobuf,
      munit.value  % "test",
      (mockitoCore % "test").withDottyCompat(scalaVersion.value)
    ),
    mimaPreviousArtifacts := Set(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % MimaPreviousVersion
    )
  )

lazy val grpcRuntimeJVM2_12 = grpcRuntime.jvm(Scala212)

lazy val compilerPlugin = (projectMatrix in file("compiler-plugin"))
  .defaultAxes()
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      protocGen.withDottyCompat(scalaVersion.value),
      "com.google.protobuf" % "protobuf-java" % protobufCompilerVersion % "protobuf",
      (protocCacheCoursier  % "test").withDottyCompat(scalaVersion.value),
      scalaTest.value       % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    mimaBinaryIssueFilters := Seq(
    ),
    PB.protocVersion := protobufCompilerVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufCompilerVersion) -> (Compile / sourceManaged).value / "java_out"
    ),
    Compile / PB.protoSources := Seq((LocalRootProject / baseDirectory).value / "protobuf"),
    Compiler.generateVersionFile,
    Compiler.generateEncodingFile
  )
  .jvmPlatform(Seq(Scala212, Scala213, Dotty))

lazy val compilerPluginJVM2_12 = compilerPlugin.jvm(Scala212)

lazy val scalapbc = (projectMatrix in file("scalapbc"))
  .defaultAxes()
  .dependsOn(compilerPlugin)
  .enablePlugins(JavaAppPackaging)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      coursier,
      protocCacheCoursier.withDottyCompat(scalaVersion.value)
    ),
    /** Originally, we had scalapb.ScalaPBC as the only main class. Now when we added scalapb-gen, we start
      * to take advantage over sbt-native-package ability to create multiple scripts. As a result the name of the
      * executable it generates became scala-pbc. To avoid breakage we create under the scalapb.scripts the scripts
      * with the names we would like to feed into scala-native-packager. We keep the original scalapb.ScalaPBC to not
      * break integrations that use it (maven, pants), but we still want to exclude it below so a script named scala-pbc
      * is not generated for it.
      */
    Compile / discoveredMainClasses := (Compile / discoveredMainClasses).value
      .filter(_.startsWith("scalapb.scripts.")),
    Compile / mainClass := Some("scalapb.scripts.scalapbc"),
    maintainer := "thesamet@gmail.com"
  )

lazy val protocGenScala =
  protocGenProject("protoc-gen-scala", compilerPluginJVM2_12)
    .settings(
      commonSettings,
      Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
    )

lazy val protocGenScalaNativeImage =
  (project in file("protoc-gen-scala-native-image"))
    .enablePlugins(GraalVMNativeImagePlugin)
    .dependsOn(compilerPluginJVM2_12)
    .settings(
      name := "protoc-gen-scala-native-image",
      graalVMNativeImageOptions ++= Seq(
        "-H:ReflectionConfigurationFiles=" + baseDirectory.value + "/native-image-config/reflect-config.json",
        "-H:Name=protoc-gen-scala"
      ) ++ (
        if (System.getProperty("os.name").toLowerCase.contains("linux"))
          Seq("--static")
        else Seq.empty,
      ),
      publish / skip := true,
      Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
    )

lazy val proptest = (projectMatrix in file("proptest"))
  .defaultAxes()
  .dependsOn(compilerPlugin % "compile->compile;test->test", runtime, grpcRuntime)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213, Dotty))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
    libraryDependencies ++= Seq(
      protobufJava,
      grpcNetty                                               % "test",
      grpcProtobuf                                            % "test",
      protocCacheCoursier.withDottyCompat(scalaVersion.value) % "test",
      scalaTest.value                                         % "test",
      scalaTestPlusScalaCheck.value                           % "test"
    ),
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:lineContentFilters=import scala.collection.compat._"
                         )
                       else Nil),
    libraryDependencies ++= (if (!isDotty.value)
                               Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value)
                             else
                               Seq(
                                 "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
                                 "org.scala-lang" %% "scala3-library"  % scalaVersion.value
                               )),
    publish / skip := true,
    Test / fork := true,
    Test / baseDirectory := (LocalRootProject / baseDirectory).value,
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M")
  )

lazy val lenses = (projectMatrix in file("lenses"))
  .defaultAxes()
  .settings(commonSettings)
  .settings(
    name := "lenses",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      munit.value % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "lenses" % MimaPreviousVersion)
  )
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213, Dotty))
  .jsPlatform(
    scalaVersions = Seq(Scala212, Scala213),
    settings = scalajsSourceMaps ++ Seq(
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )
  .nativePlatform(
    scalaVersions = Seq(Scala212, Scala213),
    settings = sharedNativeSettings
  )

lazy val lensesJVM2_12 = lenses.jvm(Scala212)

val e2eCommonSettings = commonSettings ++ Seq(
  useCoursier := true,
  publish / skip := true,
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    grpcNetty,
    grpcProtobuf,
    grpcServices,
    grpcServices % "protobuf",
    annotationApi,
    cats,
    (scalaTest.value               % "test"),
    (scalaTestPlusScalaCheck.value % "test")
  ),
  Compile / PB.recompile := true, // always regenerate protos, not cache
  codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value
)

lazy val e2eGrpc = (projectMatrix in file("e2e-grpc"))
  .defaultAxes()
  .dependsOn(runtime, grpcRuntime)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(Seq(Scala212, Scala213, Dotty))
  .settings(e2eCommonSettings)
  .settings(
    libraryDependencies += (grpcProtocGen asProtocPlugin),
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:pathFilters=ServerReflectionGrpc.scala;ReflectionProto.scala",
                           "-P:silencer:lineContentFilters=import com.thesamet.pb.MisplacedMapper.weatherMapper"
                         )
                       else Nil),
    Compile / PB.protoSources += (Compile / PB.externalIncludePath).value / "grpc" / "reflection",
    PB.protocVersion := versions.protobuf,
    Compile / PB.targets := Seq(
      PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value,
      PB.gens.plugin("grpc-java")     -> (Compile / sourceManaged).value,
      (
        genModule("scalapb.ScalaPbCodeGenerator$"),
        Seq("grpc", "java_conversions")
      ) -> (Compile / sourceManaged).value
    ),
    codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value
  )

lazy val e2eWithJava = (projectMatrix in file("e2e-withjava"))
  .defaultAxes()
  .dependsOn(runtime)
  .enablePlugins(LocalCodeGenPlugin)
  .settings(e2eCommonSettings)
  .settings(
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:lineContentFilters=import com.thesamet.pb.MisplacedMapper.weatherMapper"
                         )
                       else Nil)
  )
  .jvmPlatform(
    Seq(Scala212, Scala213, Dotty),
    settings = Seq(
      Compile / PB.targets := Seq(
        PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value,
        (
          genModule("scalapb.ScalaPbCodeGenerator$"),
          Seq("java_conversions")
        ) -> (Compile / sourceManaged).value
      )
    )
  )
  .jsPlatform(
    Seq(Scala212, Scala213),
    settings = Seq(
      Compile / PB.includePaths += (ThisBuild / baseDirectory).value / "protobuf",
      Compile / PB.targets := Seq(
        (genModule("scalapb.ScalaPbCodeGenerator$")) -> (Compile / sourceManaged).value
      )
    )
  )

lazy val e2e = (projectMatrix in file("e2e"))
  .defaultAxes()
  .dependsOn(runtime, e2eWithJava)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(
    Seq(Scala212, Scala213, Dotty),
    settings = Seq(
      Test / unmanagedSourceDirectories += (Test / scalaSource).value.getParentFile / (if (
                                                                                         isDotty.value
                                                                                       )
                                                                                         "scalajvm-3"
                                                                                       else
                                                                                         "scalajvm-2")
    )
  )
  .jsPlatform(
    Seq(Scala212, Scala213),
    settings = Seq(
      Compile / PB.includePaths += (ThisBuild / baseDirectory).value / "protobuf"
    )
  )
  .settings(e2eCommonSettings)
  .settings(
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:globalFilters=value deprecatedInt32 in class TestDeprecatedFields is deprecated",
                           "-P:silencer:pathFilters=custom_options_use;CustomAnnotationProto.scala;TestDeprecatedFields.scala",
                           "-P:silencer:lineContentFilters=import com.thesamet.pb.MisplacedMapper.weatherMapper"
                         )
                       else Nil),
    PB.protocVersion := versions.protobuf,
    Compile / PB.protocOptions += "--experimental_allow_proto3_optional",
    Compile / PB.targets := Seq(
      genModule("scalapb.ScalaPbCodeGenerator$") -> (Compile / sourceManaged).value
    )
  )

lazy val docs = project
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
  .dependsOn(runtimeJVM2_12)
  .settings(commonSettings)
  .settings(
    publish / skip := true,
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s"   % "0.10.1",
      "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.10.1",
      "org.apache.spark"     %% "spark-sql"        % "2.4.4",
      "com.lihaoyi"          %% "ujson"            % "0.9.0",
      "com.lihaoyi"          %% "os-lib"           % "0.5.0",
      "org.plotly-scala"     %% "plotly-render"    % "0.7.2"
    ),
    mdocIn := baseDirectory.value / "src" / "main" / "markdown",
    ScalaUnidoc / siteSubdirName := "api/scalapb/latest",
    addMappingsToSiteDir(ScalaUnidoc / packageDoc / mappings, ScalaUnidoc / siteSubdirName),
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
      lensesJVM2_12,
      runtimeJVM2_12,
      grpcRuntimeJVM2_12
    ),
    ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
    cleanFiles += (ScalaUnidoc / unidoc / target).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value,
    mdocVariables := Map(
      "scalapb"          -> "0.10.10",
      "scalapb_latest"   -> "0.11.0-M4",
      "scala3"           -> Dependencies.Dotty,
      "sbt_protoc"       -> "1.0.0",
      "sbt_dotty"        -> "0.4.6",
      "protoc"           -> "3.11.4",
      "sparksql_scalapb" -> "0.10.4",
      "scalapb_validate" -> "0.2.0"
    ),
    git.remoteRepo := "git@github.com:scalapb/scalapb.github.io.git",
    ghpagesBranch := "master"
    /*
    ghpagesCleanSite / includeFilter := GlobFilter(
      (ghpagesRepository.value / "README.md").getCanonicalPath
    )
     */
  )
