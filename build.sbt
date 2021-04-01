import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._
import BuildHelper._
import Dependencies._

val protobufCompilerVersion = "3.12.2"

val MimaPreviousVersion = "0.10.0"

inThisBuild(
  List(
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212, Scala213),
    scalacOptions ++= BuildHelper.compilerOptions,
    javacOptions ++= List("-target", "8", "-source", "8"),
    organization := "com.thesamet.scalapb",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  )
)

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
    .aggregate(
      lensesJS,
      lensesJVM,
      runtimeJS,
      runtimeJVM,
      grpcRuntime,
      compilerPlugin,
      compilerPluginShaded,
      protocGenScala,
      proptest,
      scalapbc
    )

lazy val runtime = crossProject(JSPlatform, JVMPlatform /*, NativePlatform*/ )
  .crossType(CrossType.Full)
  .in(file("scalapb-runtime"))
  .dependsOn(lenses)
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      scalaCollectionCompat.value,
      fastparse.value,
      protobufJava     % "protobuf",
      utest.value      % "test",
      commonsCodec     % "test",
      protobufJavaUtil % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "../../protobuf",
    scalacOptions ++= (if (scalaVersion.value == Scala213)
                         Seq(
                           "-Xfatal-warnings",
                           "-Wconf:origin=.*EqualsAndHash:s",
                           "-Wconf:src=UnknownFieldSet.scala:s"
                         )
                       else Nil),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[DirectMissingMethodProblem]("*.of"),
      ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.MessageOptions.*"),
      ProblemFilters.exclude[FinalClassProblem]("scalapb.UnknownFieldSet$Field"),
      ProblemFilters.exclude[IncompatibleResultTypeProblem](
        "scalapb.options.MessageOptions.*$default$8"
      ),
      ProblemFilters.exclude[IncompatibleMethTypeProblem]("scalapb.options.MessageOptions.of"),
      ProblemFilters.exclude[ReversedMissingMethodProblem](
        "scalapb.options.Scalapb#MessageOptionsOrBuilder.getUnknownFieldsAnnotations*"
      ),
      ProblemFilters.exclude[InheritedNewAbstractMethodProblem]("*Extension*"),
      ProblemFilters.exclude[Problem]("scalapb.options.*"),
      ProblemFilters.exclude[FinalMethodProblem]("*.parseFrom")
    )
  )
  .platformsSettings(JSPlatform /*, NativePlatform*/ )(
    libraryDependencies += protobufRuntimeScala.value,
    (Compile / unmanagedSourceDirectories) += baseDirectory.value / ".." / "non-jvm" / "src" / "main" / "scala"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      protobufJava,
      scalaTest               % "test",
      scalaTestPlusScalaCheck % "test"
    ),
    Compile / PB.targets ++= Seq(
      PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value
    ),
    PB.protocVersion := versions.protobuf,
    Compile / PB.protoSources := Seq(
      baseDirectory.value / ".." / ".." / "protobuf"
    )
  )
  .jsSettings(
    scalajsSourceMaps,
    Compile / unmanagedResourceDirectories += baseDirectory.value / "../../third_party"
  )
/*
  .nativeSettings(
    sharedNativeSettings
  )
 */

lazy val runtimeJVM = runtime.jvm
lazy val runtimeJS  = runtime.js
//lazy val runtimeNative = runtime.native

lazy val grpcRuntime = project
  .in(file("scalapb-runtime-grpc"))
  .dependsOn(runtimeJVM)
  .settings(
    name := "scalapb-runtime-grpc",
    libraryDependencies ++= Seq(
      grpcStub,
      grpcProtobuf,
      scalaTest            % "test",
      scalaTestPlusMockito % "test",
      mockitoCore          % "test"
    ),
    mimaPreviousArtifacts := Set(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % MimaPreviousVersion
    )
  )

lazy val compilerPlugin = project
  .in(file("compiler-plugin"))
  .settings(
    crossScalaVersions := Seq(Scala212, Scala213),
    libraryDependencies ++= Seq(
      scalaCollectionCompat.value,
      protocGen,
      "com.google.protobuf" % "protobuf-java" % protobufCompilerVersion % "protobuf",
      scalaTest             % "test",
      coursier              % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    PB.protocVersion := protobufCompilerVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufCompilerVersion) -> (Compile / sourceManaged).value / "java_out"
    ),
    Compile / PB.protoSources := Seq((LocalRootProject / baseDirectory).value / "protobuf"),
    Compiler.generateVersionFile,
    Compiler.generateEncodingFile
  )

// Until https://github.com/scalapb/ScalaPB/issues/150 is fixed, we are
// publishing compiler-plugin bundled with protoc-bridge, and linked against
// shaded protobuf. This is a workaround - this artifact will be removed in
// the future.
lazy val compilerPluginShaded = project
  .in(file("compiler-plugin-shaded"))
  .dependsOn(compilerPlugin)
  .settings(
    name := "compilerplugin-shaded",
    crossScalaVersions := Seq(Scala212, Scala213),
    Compiler.shadedLibSettings
  )

lazy val scalapbc = project
  .in(file("scalapbc"))
  .dependsOn(compilerPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      protocCacheCoursier
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

lazy val protocGenScalaUnix =
  (project in file("protoc-gen-scala-native-image"))
    .enablePlugins(AssemblyPlugin, NativeImagePlugin)
    .dependsOn(compilerPlugin)
    .settings(
      name := "protoc-gen-scala-native-image",
      scalaVersion := Scala212,
      nativeImageOutput := file("target") / "protoc-gen-scala",
      nativeImageOptions ++= Seq(
        "-H:ReflectionConfigurationFiles=" + baseDirectory.value + "/native-image-config/reflect-config.json",
        "-H:Name=protoc-gen-scala"
      ) ++ (
        if (System.getProperty("os.name").toLowerCase.contains("linux"))
          Seq("--static", "--no-fallback")
        else Seq.empty,
      ),
      assemblyOption in assembly := (assemblyOption in
        assembly).value.copy(
        prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = true))
      ),
      publish / skip := true,
      Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
    )

lazy val protocGenScalaWindows = project
  .enablePlugins(AssemblyPlugin)
  .dependsOn(compilerPlugin)
  .settings(
    assemblyOption in assembly := (assemblyOption in
      assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = false))
    ),
    skip in publish := true,
    Compile / mainClass := Some("scalapb.ScalaPbCodeGenerator")
  )

lazy val protocGenScala = project
  .settings(
    crossScalaVersions := List(Scala212),
    name := "protoc-gen-scala",
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    crossPaths := false,
    addArtifact(
      Artifact("protoc-gen-scala", "jar", "sh", "unix"),
      assembly in (protocGenScalaUnix, Compile)
    ),
    addArtifact(
      Artifact("protoc-gen-scala", "jar", "bat", "windows"),
      assembly in (protocGenScalaWindows, Compile)
    ),
    autoScalaLibrary := false
  )

lazy val proptest = project
  .in(file("proptest"))
  .dependsOn(compilerPlugin % "compile->compile;test->test", runtimeJVM, grpcRuntime)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
    libraryDependencies ++= Seq(
      protobufJava,
      grpcNetty               % "test",
      grpcProtobuf            % "test",
      scalaTest               % "test",
      scalaTestPlusScalaCheck % "test"
    ),
    libraryDependencies += { "org.scala-lang" % "scala-compiler" % scalaVersion.value },
    Test / fork := true,
    Test / baseDirectory := baseDirectory.value / "..",
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M")
  )

lazy val lenses = crossProject(JSPlatform, JVMPlatform /*, NativePlatform*/ )
  .in(file("lenses"))
  .settings(
    name := "lenses",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      scalaCollectionCompat.value,
      utest.value % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "lenses" % MimaPreviousVersion)
  )
  .jsSettings(
    scalajsSourceMaps
  )
/*
  .nativeSettings(
    sharedNativeSettings
  )
 */

lazy val lensesJVM = lenses.jvm
lazy val lensesJS  = lenses.js
//lazy val lensesNative = lenses.native

val e2eCommonSettings = Seq(
  useCoursier := true,
  skip in publish := true,
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    grpcNetty,
    grpcProtobuf,
    grpcServices,
    grpcServices % "protobuf",
    annotationApi,
    cats,
    grpcProtocGen asProtocPlugin,
    scalaTest               % "test",
    scalaTestPlusScalaCheck % "test"
  ),
  Test / fork := true,           // For https://github.com/scala/bug/issues/9237
  Compile / PB.recompile := true // always regenerate protos, not cache
)

lazy val e2e = (project in file("e2e"))
  .dependsOn(runtimeJVM)
  .dependsOn(grpcRuntime)
  .settings(e2eCommonSettings)
  .settings(
    scalacOptions ++= (if (scalaVersion.value == Scala213)
                         Seq(
                           "-Xfatal-warnings",
                           "-Wconf:origin=.*EqualsAndHash:s",
                           "-Wconf:origin=.*eprecatedInt32:s",
                           "-Wconf:origin=.*v1alpha.*:s",
                           "-Wconf:src=FieldAnnotations\\.scala:s"
                         )
                       else Nil),
    scalacOptions -= "-Ywarn-unused:imports", // Not sure how to silence individual ones with -Wconf
    Compile / PB.protoSources += (Compile / PB.externalIncludePath).value / "grpc" / "reflection",
    Compile / PB.generate := ((Compile / PB.generate) dependsOn (protocGenScalaUnix / Compile / assembly)).value,
    PB.protocVersion := versions.protobuf,
    Compile / PB.targets := Seq(
      PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value,
      (
        PB.gens.plugin(
          "scalapb",
          (protocGenScalaUnix / assembly / target).value / "protocGenScalaUnix-assembly-" + version.value + ".jar"
        ),
        Seq("grpc", "java_conversions")
      )                           -> (Compile / sourceManaged).value,
      PB.gens.plugin("grpc-java") -> (Compile / sourceManaged).value
    )
  )

lazy val e2eNoJava = (project in file("e2e-nojava"))
  .dependsOn(runtimeJVM)
  .settings(e2eCommonSettings)
  .settings(
    PB.protocVersion := versions.protobuf,
    Compile / PB.targets := Seq(
      (
        PB.gens.plugin(
          "scalapb",
          (protocGenScalaUnix / assembly / target).value / "protocGenScalaUnix-assembly-" + version.value + ".jar"
        ),
        Seq()
      ) -> (Compile / sourceManaged).value
    )
  )

lazy val docs = project
  .in(file("docs"))
  .enablePlugins(MicrositesPlugin, ScalaUnidocPlugin)
  .dependsOn(runtimeJVM)
  .settings(
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
    micrositeAnalyticsToken := "UA-346180-20",
    micrositeName := "ScalaPB",
    micrositeCompilingDocsTool := WithMdoc,
    mdocIn := baseDirectory.value / "src" / "main" / "markdown",
    micrositeDescription := "Protocol buffer compiler for Scala",
    micrositeDocumentationUrl := "/",
    micrositeAuthor := "Nadav Samet",
    micrositeGithubOwner := "scalapb",
    micrositeGithubRepo := "ScalaPB",
    micrositeGitterChannelUrl := "ScalaPB/community",
    micrositeHighlightTheme := "atom-one-light",
    micrositeHighlightLanguages := Seq("scala", "java", "bash", "protobuf"),
    micrositeTheme := "pattern",
    micrositePalette := Map(
      "brand-primary"   -> "#D62828", // active item marker on the left
      "brand-secondary" -> "#003049", // menu background
      "brand-tertiary"  -> "#F77F00", // active item
      "gray-dark"       -> "#F77F00", // headlines
      "gray"            -> "#000000", // text
      "gray-light"      -> "#D0D0D0", // stats on top
      "gray-lighter"    -> "#F4F3F4", // content wrapper background
      "white-color"     -> "#FFFFFF"  // ???
    ),
    siteSubdirName in ScalaUnidoc := "api/scalapb/latest",
    addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
    unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(lensesJVM, runtimeJVM, grpcRuntime),
    git.remoteRepo := "git@github.com:scalapb/scalapb.github.io.git",
    ghpagesBranch := "master",
    includeFilter in ghpagesCleanSite := GlobFilter(
      (ghpagesRepository.value / "README.md").getCanonicalPath
    )
  )
