import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._
import BuildHelper._
import Dependencies._

val Scala212 = "2.12.10"

val Scala213 = "2.13.1"

// Different version for compiler-plugin since >=3.8.0 is not binary
// compatible with 3.7.x. When loaded inside SBT (which has its own old
// version), the binary incompatibility surfaces.
val protobufCompilerVersion = "3.7.1"

val MimaPreviousVersion = "0.10.0"

inThisBuild(
  List(
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212, Scala213),
    scalacOptions ++= BuildHelper.compilerOptions,
    javacOptions ++= List("-target", "8", "-source", "8"),
    organization := "com.thesamet.scalapb",
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= silencer
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

releaseCrossBuild := true

releasePublishArtifactsAction := PgpKeys.publishSigned.value

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommandAndRemaining(s"++${Scala212};protocGenScala/publishSigned"),
  // releaseStepCommandAndRemaining(s";++${Scala211};runtimeNative/publishSigned;lensesNative/publishSigned"),
  releaseStepCommand(s"sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
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
      proptest,
      scalapbc
    )

// fastparse 2 is not available for Scala Native yet
// https://github.com/lihaoyi/fastparse/issues/215
lazy val runtime = crossProject(JSPlatform, JVMPlatform /*, NativePlatform*/ )
  .crossType(CrossType.Full)
  .in(file("scalapb-runtime"))
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
    scalacOptions ++= Seq(
      "-P:silencer:globalFilters=avaGenerateEqualsAndHash in class .* is deprecated",
      "-P:silencer:lineContentFilters=import scala.collection.compat._"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[DirectMissingMethodProblem]("*.of")
    )
  )
  .dependsOn(lenses)
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
    // Can be removed after JDK 11.0.3 is available on Travis
    Test / javaOptions ++= (
      if (scalaVersion.value.startsWith("2.13."))
        Seq("-XX:LoopStripMiningIter=0")
      else Nil
    ),
    Compile / PB.targets ++= Seq(
      PB.gens.java(versions.protobuf) -> (Compile / sourceManaged).value
    ),
    Compile / PB.protocVersion := "-v" + versions.protobuf,
    Compile / PB.protoSources := Seq(
      baseDirectory.value / ".." / ".." / "protobuf"
    )
  )
  .jsSettings(
    // Add JS-specific settings here
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process
        .Process("git rev-parse HEAD")
        .lineStream_!
        .head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
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
      protocBridge,
      "com.google.protobuf" % "protobuf-java" % protobufCompilerVersion % "protobuf",
      scalaTest             % "test",
      protocJar             % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    mimaBinaryIssueFilters := Seq(
      ProblemFilters.exclude[MissingTypesProblem]("scalapb.compiler.ConstructorField$"),
      ProblemFilters
        .exclude[DirectMissingMethodProblem]("scalapb.compiler.ConstructorField.tupled"),
      ProblemFilters
        .exclude[DirectMissingMethodProblem]("scalapb.compiler.ConstructorField.curried")
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

lazy val protocGenScalaUnix = project
  .enablePlugins(AssemblyPlugin, GraalVMNativeImagePlugin)
  .dependsOn(compilerPlugin)
  .settings(
    graalVMNativeImageOptions ++= Seq(
      "-H:ReflectionConfigurationFiles=" + baseDirectory.value + "/native-image-config/reflect-config.json",
      "-H:Name=protoc-gen-scala"
    ) ++ (
      if (System.getProperty("os.name").toLowerCase.contains("linux"))
        Seq("--static")
      else Seq.empty,
    ),
    assemblyOption in assembly := (assemblyOption in
      assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = true))
    ),
    skip in publish := true,
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
  .dependsOn(compilerPlugin, runtimeJVM, grpcRuntime)
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
    Test / fork := true,
    Test / baseDirectory := baseDirectory.value / "..",
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M"),
    // Can be removed after JDK 11.0.3 is available on Travis
    Test / javaOptions ++= (if (scalaVersion.value.startsWith("2.13."))
                              Seq("-XX:LoopStripMiningIter=0")
                            else Nil)
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
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process
        .Process("git rev-parse HEAD")
        .lineStream_!
        .head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )
/*
  .nativeSettings(
    sharedNativeSettings
  )
 */

lazy val lensesJVM = lenses.jvm
lazy val lensesJS  = lenses.js
//lazy val lensesNative = lenses.native

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

val e2eCommonSettings = Seq(
  // https://github.com/thesamet/sbt-protoc/issues/104
  useCoursier := false,
  skip in publish := true,
  javacOptions ++= Seq("-Xlint:deprecation"),
  libraryDependencies ++= Seq(
    grpcNetty,
    grpcProtobuf,
    grpcServices,
    grpcServices % "protobuf",
    annotationApi,
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
    scalacOptions ++= Seq(
      "-P:silencer:globalFilters=value deprecatedInt32 in class TestDeprecatedFields is deprecated",
      "-P:silencer:pathFilters=custom_options_use;CustomAnnotationProto.scala;changed/scoped;ServerReflectionGrpc.scala",
      "-P:silencer:lineContentFilters=import com.thesamet.pb.MisplacedMapper.weatherMapper"
    ),
    Compile / PB.protoSources += (Compile / PB.externalIncludePath).value / "grpc" / "reflection",
    Compile / PB.generate := ((Compile / PB.generate) dependsOn (protocGenScalaUnix / Compile / assembly)).value,
    Compile / PB.protocVersion := "-v" + versions.protobuf,
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
    Compile / PB.protocVersion := "-v" + versions.protobuf,
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
