import com.typesafe.tools.mima.core._
import BuildHelper._
import Dependencies._
import sbtassembly.AssemblyPlugin.defaultUniversalScript

val protobufCompilerVersion = "4.32.0"

val MimaPreviousVersion = "0.11.0"

inThisBuild(
  List(
    scalaVersion := Scala212,
    javacOptions ++= List("-target", "8", "-source", "8"),
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    ConsoleHelper.welcomeMessage
  )
)

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

ThisBuild / PB.protocVersion := versions.protobuf

lazy val sharedNativeSettings = List(
  nativeConfig ~= { _.withLinkStubs(true) } // for utest
)

lazy val root: Project =
  project
    .in(file("."))
    .settings(
      publishArtifact := false,
      publish         := {},
      publishLocal    := {}
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
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.GeneratedEnum.asRecognized"),
      ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("*Extension*"),
      ProblemFilters.exclude[Problem]("scalapb.options.*"),
      ProblemFilters.exclude[FinalMethodProblem]("*.parseFrom")
    ),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq(
            "-Wconf:msg=[Uu]nused&origin=scala[.]collection[.]compat._:s",
            "-Wconf:cat=deprecation&msg=.*[Jj]avaGenerateEqualsAndHash.*deprecated.*:s",
            "-Wconf:cat=deprecation&msg=.*[dD]eprecatedLegacyJsonFieldConflicts:s",
            "-Wconf:cat=deprecation&msg=.*[eE]dition.*deprecated.*:s",
            "-Wconf:cat=deprecation&msg=.*[wW]eak.*:s",
            "-Wconf:cat=deprecation&msg=.*[sS]yntax.*:s"
          )
        case _ => Seq.empty // Scala 2.12 or other (e.g. pre-2.13)
      }
    }
  )
  .jvmPlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
    settings = Seq(
      libraryDependencies ++= Seq(
        protobufJava
      ),
      Compile / PB.targets ++= Seq(
        PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value
      ),
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "jvm-native",
      Compile / PB.protoSources := Seq(
        (LocalRootProject / baseDirectory).value / "protobuf"
      )
    )
  )
  .jsPlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
    settings = Seq(
      libraryDependencies += protobufRuntimeScala.value,
      scalajsSourceMaps,
      Compile / unmanagedResourceDirectories += (LocalRootProject / baseDirectory).value / "third_party",
      Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / "js-native",
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )
  .nativePlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
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
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213, Scala3))
  .settings(
    name := "scalapb-runtime-grpc",
    testFrameworks += new TestFramework("munit.Framework"),
    libraryDependencies ++= Seq(
      grpcStub,
      grpcProtobuf,
      munit.value % "test",
      mockitoCore % "test"
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
    scalacOptions ++= (if (!isDotty.value)
                         Seq(
                           "-P:silencer:globalFilters=object FlatPackage in object GeneratorOption is deprecated"
                         )
                       else Nil),
    libraryDependencies ++= Seq(
      protocGen.cross(CrossVersion.for3Use2_13),
      "com.google.protobuf" % "protobuf-java" % protobufCompilerVersion % "protobuf",
      (protocCacheCoursier  % "test").cross(CrossVersion.for3Use2_13),
      scalaTest.value       % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    mimaBinaryIssueFilters := Seq(
      ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.*"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.compiler.GeneratorParams.*"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.gen.*"),
      ProblemFilters.exclude[DirectMissingMethodProblem](
        "scalapb.compiler.ProtobufGenerator.generateMessageCompanionMatcher"
      ),
      ProblemFilters.exclude[DirectMissingMethodProblem](
        "scalapb.compiler.ProtobufGenerator.generateTypeMappers"
      )
    ),
    PB.protocVersion := protobufCompilerVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufCompilerVersion) -> (Compile / sourceManaged).value / "java_out"
    ),
    Compile / PB.protoSources := Seq((LocalRootProject / baseDirectory).value / "protobuf"),
    Compiler.generateVersionFile,
    Compiler.generateEncodingFile
  )
  .jvmPlatform(Seq(Scala212, Scala213, Scala3))

lazy val compilerPluginJVM2_12 = compilerPlugin.jvm(Scala212)

lazy val compilerPluginJVM2_13 = compilerPlugin.jvm(Scala213)

lazy val scalapbc = (projectMatrix in file("scalapbc"))
  .defaultAxes()
  .dependsOn(compilerPlugin)
  .enablePlugins(JavaAppPackaging)
  .jvmPlatform(scalaVersions = Seq(Scala212, Scala213))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      coursier,
      protocCacheCoursier.cross(CrossVersion.for3Use2_13)
    ),
    /** Originally, we had scalapb.ScalaPBC as the only main class. Now when we added scalapb-gen,
      * we start to take advantage over sbt-native-package ability to create multiple scripts. As a
      * result the name of the executable it generates became scala-pbc. To avoid breakage we create
      * under the scalapb.scripts the scripts with the names we would like to feed into
      * scala-native-packager. We keep the original scalapb.ScalaPBC to not break integrations that
      * use it (maven, pants), but we still want to exclude it below so a script named scala-pbc is
      * not generated for it.
      */
    Compile / discoveredMainClasses := (Compile / discoveredMainClasses).value
      .filter(_.startsWith("scalapb.scripts.")),
    Compile / mainClass := Some("scalapb.scripts.scalapbc"),
    maintainer          := "thesamet@gmail.com"
  )

lazy val protocGenScala =
  protocGenProject("protoc-gen-scala", compilerPluginJVM2_12)
    .settings(
      commonSettings,
      Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
    )

lazy val protocGenScalaNativeImage =
  (project in file("protoc-gen-scala-native-image"))
    .enablePlugins(NativeImagePlugin)
    .dependsOn(compilerPluginJVM2_13)
    .settings(
      name              := "protoc-gen-scala-native-image",
      scalaVersion      := Scala213,
      nativeImageOutput := file("target") / "protoc-gen-scala",
      nativeImageOptions ++= Seq(
        "-H:ReflectionConfigurationFiles=" + baseDirectory.value + "/native-image-config/reflect-config.json",
        "-H:Name=protoc-gen-scala"
      ) ++ (
        if (System.getProperty("os.name").toLowerCase.contains("linux"))
          Seq("--static", "--no-fallback")
        else Seq.empty
      ),
      publish / skip      := true,
      Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
    )

lazy val ScalaSources  = GeneratorAxis("", "scalagen")
lazy val Scala3Sources = GeneratorAxis("Scala3Sources", "scala3sources")

lazy val proptest = (projectMatrix in file("proptest"))
  .dependsOn(compilerPlugin % "compile->compile;test->test", runtime, grpcRuntime)
  .defaultAxes()
  .customRow(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
    axisValues = Seq(ScalaSources, VirtualAxis.jvm),
    settings = Seq()
  )
  .customRow(
    scalaVersions = Seq(Scala213, Scala3),
    axisValues = Seq(Scala3Sources, VirtualAxis.jvm),
    settings = Seq(
      Test / javaOptions += "-Dscala3_sources"
    )
  )
  .settings(commonSettings)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
    libraryDependencies ++= Seq(
      protobufJava,
      grpcNetty                                           % "test",
      grpcProtobuf                                        % "test",
      protocCacheCoursier.cross(CrossVersion.for3Use2_13) % "test",
      scalaTest.value                                     % "test",
      scalaTestPlusScalaCheck.value                       % "test"
    ),
    libraryDependencies ++= (if (!isScala3.value)
                               Seq("org.scala-lang" % "scala-compiler" % scalaVersion.value)
                             else
                               Seq(
                                 "org.scala-lang" %% "scala3-compiler" % scalaVersion.value,
                                 "org.scala-lang" %% "scala3-library"  % scalaVersion.value
                               )),
    publish / skip       := true,
    Test / fork          := true,
    Test / baseDirectory := (LocalRootProject / baseDirectory).value,
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M"),
    scalacOptions ++= {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, _)) =>
          Seq(
            "-Wconf:msg=[Uu]nused&origin=scala[.]collection[.]compat._:s",
            "-Wconf:cat=deprecation&msg=.*[Jj]avaGenerateEqualsAndHash.*deprecated.*:s",
            "-Wconf:cat=deprecation&msg=.*[dD]eprecatedLegacyJsonFieldConflicts:s"
          )
        case _ => Seq.empty // Scala 2.12 or other (e.g. pre-2.13)
      }
    }
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
  .jvmPlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3)
  )
  .jsPlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
    settings = scalajsSourceMaps ++ Seq(
      scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
    )
  )
  .nativePlatform(
    scalaVersions = Seq(Scala212, Scala213, Scala3),
    settings = sharedNativeSettings
  )

lazy val lensesJVM2_12 = lenses.jvm(Scala212)

val e2eCommonSettings = commonSettings ++ Seq(
  useCoursier    := true,
  publish / skip := true,
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    grpcInprocess,
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
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, _)) =>
        Seq(
          "-Wconf:msg=[uU]nused&origin=com.thesamet.pb.MisplacedMapper.weatherMapper:s",
          "-Wconf:cat=deprecation&src=.*custom_options.*:s",
          "-Wconf:cat=deprecation&src=.*CustomAnnotationProto.scala.*:s",
          "-Wconf:cat=deprecation&src=.*TestDeprecatedFields.scala.*:s",
          "-Wconf:cat=deprecation&origin=.*ServerReflectionProto.*:s",
          "-Wconf:msg=Unused import:s,origin=com.thesamet.proto.e2e.custom_options_use.FooMessage:s"
        )
      case _ => Seq.empty
    }
  },
  codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value
)

lazy val e2eGrpc = (projectMatrix in file("e2e-grpc"))
  .defaultAxes()
  .dependsOn(runtime, grpcRuntime)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(Seq(Scala212, Scala213, Scala3))
  .settings(e2eCommonSettings)
  .settings(
    libraryDependencies += (grpcProtocGen asProtocPlugin),
    Compile / PB.protoSources += (Compile / PB.externalIncludePath).value / "grpc" / "reflection",
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
  .jvmPlatform(
    Seq(Scala212, Scala213, Scala3),
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
    Seq(Scala212, Scala213, Scala3),
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
    Seq(Scala212, Scala213, Scala3),
    settings = Seq(
      Test / unmanagedSourceDirectories += (Test / scalaSource).value.getParentFile / (if (
                                                                                         isScala3.value
                                                                                       )
                                                                                         "scalajvm-3"
                                                                                       else
                                                                                         "scalajvm-2"),
      Compile / PB.targets := Seq(
        genModule("scalapb.ScalaPbCodeGenerator$") -> (Compile / sourceManaged).value
      )
    )
  )
  .customRow(
    scalaVersions = Seq(Scala213, Scala3),
    axisValues = Seq(Scala3Sources, VirtualAxis.jvm),
    settings = Seq(
      Test / unmanagedSourceDirectories += (Test / scalaSource).value.getParentFile / (if (
                                                                                         isScala3.value
                                                                                       )
                                                                                         "scalajvm-3"
                                                                                       else
                                                                                         "scalajvm-2"),
      Test / unmanagedSourceDirectories += (Test / scalaSource).value.getParentFile / "scalajvm-3-source",
      scalacOptions ++=
        (if (isScala3.value) Seq("-source", "future") else Seq("-Xsource:3")),
      Test / scalacOptions --=
        (if (isScala3.value) Seq("-source", "future") else Seq("-Xsource:3")),
      Compile / PB.targets := Seq(
        (
          genModule("scalapb.ScalaPbCodeGenerator$"),
          Seq("scala3_sources")
        ) -> (Compile / sourceManaged).value
      )
    )
  )
  .jsPlatform(
    Seq(Scala212, Scala213, Scala3),
    settings = Seq(
      Compile / PB.includePaths += (ThisBuild / baseDirectory).value / "protobuf",
      Compile / PB.targets := Seq(
        genModule("scalapb.ScalaPbCodeGenerator$") -> (Compile / sourceManaged).value
      )
    )
  )
  .settings(e2eCommonSettings)
  .settings(
    Compile / PB.protoSources ++= (if (isScala3.value)
                                     Seq(sourceDirectory.value / "main" / "protobuf-scala3")
                                   else Nil),
    Compile / PB.protocOptions += "--experimental_allow_proto3_optional"
  )

lazy val conformance = (projectMatrix in file("conformance"))
  .defaultAxes()
  .dependsOn(runtime)
  .enablePlugins(LocalCodeGenPlugin)
  .jvmPlatform(
    Seq(Scala213)
  )
  .settings(
    Compile / PB.targets := Seq(
      genModule("scalapb.ScalaPbCodeGenerator$") -> (Compile / sourceManaged).value
    ),
    codeGenClasspath := (compilerPluginJVM2_12 / Compile / fullClasspath).value,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s" % "1.0.0-alpha.1" exclude ("com.thesamet.scalapb", "scalapb-runtime_2.13")
    ),
    maintainer                 := "thesamet@gmail.com",
    Compile / mainClass        := Some("scalapb.ConformanceScala"),
    assemblyJarName            := "conformance",
    assemblyPrependShellScript := Some(defaultUniversalScript(shebang = true)),
    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )

lazy val docs = project
  .enablePlugins(MdocPlugin, DocusaurusPlugin, ScalaUnidocPlugin)
  .dependsOn(runtimeJVM2_12)
  .settings(commonSettings)
  .settings(
    publish / skip     := true,
    scalaVersion       := Scala212,
    crossScalaVersions := Seq(Scala212),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s"   % "0.11.0",
      "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.11.0",
      "org.apache.spark"     %% "spark-sql"        % "3.1.1",
      "com.lihaoyi"          %% "ujson"            % "0.9.0",
      "com.lihaoyi"          %% "os-lib"           % "0.5.0",
      "org.plotly-scala"     %% "plotly-render"    % "0.7.2"
    ),
    mdocIn := baseDirectory.value / "src" / "main" / "markdown",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
      lensesJVM2_12,
      runtimeJVM2_12,
      grpcRuntimeJVM2_12
    ),
    ScalaUnidoc / unidoc / target := (LocalRootProject / baseDirectory).value / "website" / "static" / "api",
    cleanFiles += (ScalaUnidoc / unidoc / target).value,
    docusaurusCreateSite     := docusaurusCreateSite.dependsOn(Compile / unidoc).value,
    docusaurusPublishGhpages := docusaurusPublishGhpages.dependsOn(Compile / unidoc).value,
    mdocVariables := Map(
      "scalapb"          -> "0.11.11",
      "scalapb_latest"   -> "0.11.11",
      "scala3"           -> Dependencies.Scala3,
      "sbt_protoc"       -> "1.0.6",
      "protoc"           -> "3.15.6",
      "sparksql_scalapb" -> "1.0.4",
      "scalapb_validate" -> "0.3.1"
    ),
    // scalameta tree's uses ScalaPB 0.10.x, which is "sufficiently binary compatible".
    libraryDependencySchemes += "com.thesamet.scalapb" %% "scalapb-runtime" % "always"

    /*
    ghpagesCleanSite / includeFilter := GlobFilter(
      (ghpagesRepository.value / "README.md").getCanonicalPath
    )
     */
  )
