import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._
import BuildHelper._
import Dependencies._

val protobufCompilerVersion = "3.12.2"

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

/*
lazy val sharedNativeSettings = List(
  nativeLinkStubs := true, // for utest
  scalaVersion := Scala211,
  crossScalaVersions := List(Scala211)
)
 */

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
  .dependsOn(lenses)
  .settings(commonSettings)
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      fastparse.value.withDottyCompat(scalaVersion.value),
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
      Compile / PB.protocVersion := "-v" + versions.protobuf,
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
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )
/*
  .nativeSettings(
    sharedNativeSettings
  )
 */

lazy val runtimeJVM2_12 = runtime.jvm(Scala212)

//lazy val runtimeNative = runtime.native

lazy val grpcRuntime = (projectMatrix in file("scalapb-runtime-grpc"))
  .dependsOn(runtime)
  .settings(commonSettings)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213, Dotty))
  .settings(
    name := "scalapb-runtime-grpc",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      grpcStub,
      grpcProtobuf,
      munit.value % "test",
      (mockitoCore % "test").withDottyCompat(scalaVersion.value)
    ),
    mimaPreviousArtifacts := Set(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % MimaPreviousVersion
    )
  )

lazy val grpcRuntimeJVM2_12 = grpcRuntime.jvm(Scala212)

lazy val compilerPlugin = (projectMatrix in file("compiler-plugin"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      protocGen.withDottyCompat(scalaVersion.value),
      "com.google.protobuf" % "protobuf-java" % protobufCompilerVersion % "protobuf",
      scalaTest             % "test",
      protocJar             % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    mimaBinaryIssueFilters := Seq(
      ),
    Compile / PB.protocVersion := "-v" + protobufCompilerVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufCompilerVersion) -> (Compile / sourceManaged).value / "java_out"
    ),
    Compile / PB.protoSources := Seq((Compile / resourceManaged).value / "protobuf"),
    Compiler.generateVersionFile,
    Compiler.generateEncodingFile,
    Compiler.shadeProtoBeforeGenerate
  )
  .jvmPlatform(Seq(Scala212, Scala213, Dotty))

lazy val compilerPluginJVM2_12 = compilerPlugin.jvm(Scala212)

lazy val scalapbc = (projectMatrix in file("scalapbc"))
  .dependsOn(compilerPlugin)
  .enablePlugins(JavaAppPackaging)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      coursier,
      protocJar
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
  .dependsOn(compilerPlugin, runtime, grpcRuntime)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
    libraryDependencies ++= Seq(
      protocJar,
      protobufJava,
      grpcNetty               % "test",
      grpcProtobuf            % "test",
      scalaTest               % "test",
      scalaTestPlusScalaCheck % "test"
    ),
    libraryDependencies += { "org.scala-lang" % "scala-compiler" % scalaVersion.value },
    publish / skip := true,
    Test / fork := true,
    Test / baseDirectory := (LocalRootProject / baseDirectory).value,
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M")
  )

lazy val lenses = (projectMatrix in file("lenses"))
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

lazy val lensesJVM2_12 = lenses.jvm(Scala212)

val e2eCommonSettings = commonSettings ++ Seq(
  useCoursier := true,
  skip in publish := true,
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    grpcNetty,
    grpcProtobuf,
    grpcServices,
    grpcServices % "protobuf",
    annotationApi,
    grpcProtocGen asProtocPlugin,
    (scalaTest               % "test"),
    (scalaTestPlusScalaCheck % "test")
  ),
  Test / fork := true,           // For https://github.com/scala/bug/issues/9237
  Compile / PB.recompile := true // always regenerate protos, not cache
)

lazy val e2e = (projectMatrix in file("e2e"))
  .dependsOn(runtime, grpcRuntime)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(Seq(Scala212, Scala213, Dotty))
  .settings(e2eCommonSettings)
  .settings(
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:globalFilters=value deprecatedInt32 in class TestDeprecatedFields is deprecated",
                           "-P:silencer:pathFilters=custom_options_use;CustomAnnotationProto.scala;changed/scoped;ServerReflectionGrpc.scala;ReflectionProto.scala;TestDeprecatedFields.scala",
                           "-P:silencer:lineContentFilters=import com.thesamet.pb.MisplacedMapper.weatherMapper"
                         )
                       else Nil),
    Compile / PB.protoSources += (Compile / PB.externalIncludePath).value / "grpc" / "reflection",
    Compile / PB.protocVersion := "-v" + versions.protobuf,
    Compile / PB.targets := Seq(
      PB.gens.java(versions.protobuf)                                               -> (Compile / sourceManaged).value,
      PB.gens.plugin("grpc-java")                                                   -> (Compile / sourceManaged).value,
      (genModule("scalapb.ScalaPbCodeGenerator$"), Seq("grpc", "java_conversions")) -> (Compile / sourceManaged).value
    ),
    codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value
  )

lazy val e2eNoJava = (projectMatrix in file("e2e-nojava"))
  .dependsOn(runtime)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(Seq(Scala212, Scala213, Dotty))
  .settings(e2eCommonSettings)
  .settings(
    Compile / PB.protocVersion := "-v" + versions.protobuf,
    Compile / PB.protocOptions += "--experimental_allow_proto3_optional",
    Compile / PB.targets := Seq(
      genModule("scalapb.ScalaPbCodeGenerator$") -> (Compile / sourceManaged).value
    ),
    codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value
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
    siteSubdirName in ScalaUnidoc := "api/scalapb/latest",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(
      lensesJVM2_12,
      runtimeJVM2_12,
      grpcRuntimeJVM2_12
    ),
    target in (ScalaUnidoc, unidoc) := (baseDirectory in LocalRootProject).value / "website" / "static" / "api",
    cleanFiles += (target in (ScalaUnidoc, unidoc)).value,
    docusaurusCreateSite := docusaurusCreateSite.dependsOn(unidoc in Compile).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(unidoc in Compile).value,
    mdocVariables := Map(
      "scalapb"          -> "0.10.8",
      "sbt_protoc"       -> "0.99.34",
      "protoc"           -> "3.11.4",
      "sparksql_scalapb" -> "0.10.4",
      "scalapb_validate" -> "0.1.2"
    ),
    git.remoteRepo := "git@github.com:scalapb/scalapb.github.io.git",
    ghpagesBranch := "master"
    /*
    includeFilter in ghpagesCleanSite := GlobFilter(
      (ghpagesRepository.value / "README.md").getCanonicalPath
    )
   */
  )
