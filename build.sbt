import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._

val Scala210 = "2.10.7"

val Scala211 = "2.11.12"

val Scala212 = "2.12.10"

val Scala213 = "2.13.0"

val protobufVersion = "3.8.0"

// Different version for compiler-plugin since >=3.8.0 is not binary
// compatible with 3.7.x. When loaded inside SBT (which has its own old
// version), the binary incompatibility surfaces.
val protobufCompilerVersion = "3.7.1"

val scalacheckVersion = "1.14.0"

// For e2e test
val sbtPluginVersion = "0.99.23"

val grpcVersion = "1.24.0"

val MimaPreviousVersion = "0.9.0"

val ProtocJar = "com.github.os72" % "protoc-jar" % "3.8.0"

val ScalaTest = "org.scalatest" %% "scalatest" % "3.0.8"

val utestVersion = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 =>
      // drop Scala 2.11 support since 0.6.9
      "0.6.8"
    case _ =>
      "0.6.9"
  }
)

val fastparseVersion = Def.setting(
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 =>
      // drop Scala 2.11 support since 2.1.3
      "2.1.2"
    case _ =>
      "2.1.3"
  }
)

scalaVersion in ThisBuild := Scala212

crossScalaVersions in ThisBuild := Seq(Scala211, Scala212, Scala213)

scalacOptions in ThisBuild ++= Seq("-deprecation", "-target:jvm-1.8")

javacOptions in ThisBuild ++= List("-target", "8", "-source", "8")

organization in ThisBuild := "com.thesamet.scalapb"

resolvers in ThisBuild +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

publishTo in ThisBuild := sonatypePublishToBundle.value

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
  releaseStepCommandAndRemaining(s"++${Scala212};protocGenScalapb/publishSigned"),
  // releaseStepCommandAndRemaining(s";++${Scala211};runtimeNative/publishSigned;lensesNative/publishSigned"),
  releaseStepCommand(s"sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)

lazy val sharedNativeSettings = List(
  nativeLinkStubs := true, // for utest
  scalaVersion := Scala211,
  crossScalaVersions := List(Scala211)
)

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
      "com.lihaoyi"         %%% "fastparse"        % fastparseVersion.value,
      "com.google.protobuf" % "protobuf-java"      % protobufVersion % "protobuf",
      "com.lihaoyi"         %%% "utest"            % utestVersion.value % "test",
      "commons-codec"       % "commons-codec"      % "1.13" % "test",
      "com.google.protobuf" % "protobuf-java-util" % protobufVersion % "test"
    ),
    unmanagedSourceDirectories in Compile ++= {
      val base = (baseDirectory in LocalRootProject).value / "scalapb-runtime" / "shared" / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v < 13 =>
          Seq(base / "scala-pre-2.13")
        case _ =>
          Nil
      }
    },
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../protobuf",
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      Seq(
        ProblemFilters.exclude[MissingClassProblem]("scalapb.Utils"),
        ProblemFilters.exclude[MissingClassProblem]("scalapb.Utils$"),
        // introduced in 2.12.9
        ProblemFilters.exclude[IncompatibleSignatureProblem]("*"),
        // Added noBox
        ProblemFilters.exclude[Problem]("scalapb.options.MessageOptions.*"),
        ProblemFilters.exclude[Problem]("scalapb.options.Scalapb#MessageOptionsOrBuilder.*"),
        // for no_default_values_in_constructor:
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.hasNoDefaultValuesInConstructor"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.getNoDefaultValuesInConstructor"
        ),
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.ScalaPbOptions.*")
      )
    }
  )
  .dependsOn(lenses)
  .platformsSettings(JSPlatform /*, NativePlatform*/ )(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.8.2"
    ),
    (unmanagedSourceDirectories in Compile) += baseDirectory.value / ".." / "non-jvm" / "src" / "main" / "scala"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "org.scalacheck"      %% "scalacheck" % scalacheckVersion % "test",
      ScalaTest             % "test"
    ),
    // Can be removed after JDK 11.0.3 is available on Travis
    Test / javaOptions ++= (
      if (scalaVersion.value.startsWith("2.13."))
        Seq("-XX:LoopStripMiningIter=0")
      else Nil
    ),
    PB.targets in Compile ++= Seq(
      PB.gens.java(protobufVersion) -> (sourceManaged in Compile).value
    ),
    PB.protocVersion in Compile := "-v" + protobufVersion,
    PB.protoSources in Compile := Seq(
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
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../third_party"
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
      "io.grpc"     % "grpc-stub" % grpcVersion,
      "io.grpc"     % "grpc-protobuf" % grpcVersion,
      "org.mockito" % "mockito-core" % "3.0.0" % "test",
      ScalaTest     % "test"
    ),
    mimaPreviousArtifacts := Set(
      "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % MimaPreviousVersion
    ),
    mimaBinaryIssueFilters ++= Seq(
      // introduced in 2.12.9
      ProblemFilters.exclude[IncompatibleSignatureProblem]("*")
    )
  )

val shadeTarget = settingKey[String]("Target to use when shading")

shadeTarget in ThisBuild := s"scalapbshade.v${version.value.replaceAll("[.-]", "_")}.@0"

val scalapbProtoPackageReplaceTask =
  TaskKey[Unit]("scalapb-proto-package-replace", "Replaces package name in scalapb.proto")

lazy val compilerPlugin = project
  .in(file("compiler-plugin"))
  .settings(
    crossScalaVersions := Seq(Scala210, Scala211, Scala212, Scala213),
    sourceGenerators in Compile += Def.task {
      val file = (sourceManaged in Compile).value / "scalapb" / "compiler" / "Version.scala"
      IO.write(
        file,
        s"""package scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |  val protobufVersion = "${protobufVersion}"
           |  val grpcJavaVersion = "${grpcVersion}"
           |}""".stripMargin
      )
      Seq(file)
    }.taskValue,
    scalapbProtoPackageReplaceTask := {
      /*
       SBT 1.x depends on scalapb-runtime which contains a compiled copy of
       scalapb.proto.  When the compiler plugin is loaded into SBT it may cause a
       conflict. To prevent that, we use a different package name for the generated
       code for the compiler-plugin.  In the past, we used shading for this
       purpose, but this makes it harder to create more protoc plugins that depend
       on compiler-plugin.
       */
      streams.value.log
        .info(s"Generating scalapb.proto with package replaced to scalapb.options.compiler.")
      val src  = baseDirectory.value / ".." / "protobuf" / "scalapb" / "scalapb.proto"
      val dest = (resourceManaged in Compile).value / "protobuf" / "scalapb" / "scalapb.proto"
      val s    = IO.read(src).replace("scalapb.options", "scalapb.options.compiler")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    },
    PB.generate in Compile := {
      scalapbProtoPackageReplaceTask.value
      (PB.generate in Compile).value
    },
    sourceGenerators in Compile += Def.task {
      val src  = baseDirectory.value / ".." / "scalapb-runtime" / "shared" / "src" / "main" / "scala" / "scalapb" / "Encoding.scala"
      val dest = (sourceManaged in Compile).value / "scalapb" / "compiler" / "internal" / "Encoding.scala"
      val s    = IO.read(src).replace("package scalapb", "package scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }.taskValue,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "protoc-bridge" % "0.7.10",
      "com.google.protobuf"  % "protobuf-java" % protobufCompilerVersion % "protobuf",
      ScalaTest              % "test",
      ProtocJar              % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      Seq(
        // introduced in 2.12.9
        ProblemFilters.exclude[IncompatibleSignatureProblem]("*"),
        ProblemFilters
          .exclude[Problem]("scalapb.options.compiler.Scalapb#MessageOptionsOrBuilder.*"),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.hasNoDefaultValuesInConstructor"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.getNoDefaultValuesInConstructor"
        )
      )
    },
    PB.protocVersion in Compile := "-v" + protobufCompilerVersion,
    PB.targets in Compile := Seq(
      PB.gens.java(protobufCompilerVersion) -> (sourceManaged in Compile).value / "java_out"
    ),
    PB.protoSources in Compile := Seq((resourceManaged in Compile).value / "protobuf")
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
    crossScalaVersions := Seq(Scala210, Scala211, Scala212, Scala213),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("scalapb.options.Scalapb**" -> shadeTarget.value).inProject,
      ShadeRule.rename("com.google.**"             -> shadeTarget.value).inAll
    ),
    assemblyExcludedJars in assembly := {
      val toInclude = Seq(
        "protobuf-java",
        "protoc-bridge"
      )

      (fullClasspath in assembly).value.filterNot { c =>
        toInclude.exists(prefix => c.data.getName.startsWith(prefix))
      }
    },
    artifact in (Compile, packageBin) := (artifact in (Compile, assembly)).value,
    addArtifact(artifact in (Compile, packageBin), assembly),
    pomPostProcess := { (node: scala.xml.Node) =>
      new scala.xml.transform.RuleTransformer(new scala.xml.transform.RewriteRule {
        override def transform(node: scala.xml.Node): scala.xml.NodeSeq = node match {
          case e: scala.xml.Elem
              if e.label == "dependency" && e.child.exists(
                child => child.label == "artifactId" && child.text.startsWith("compilerplugin")
              ) =>
            scala.xml.Comment(s"compilerplugin has been removed.")
          case _ => node
        }
      }).transform(node).head
    }
  )

lazy val scalapbc = project
  .in(file("scalapbc"))
  .dependsOn(compilerPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    libraryDependencies ++= Seq(
      ProtocJar
    ),
    /** Originally, we had scalapb.ScalaPBC as the only main class. Now when we added scalapb-gen, we start
      * to take advantage over sbt-native-package ability to create multiple scripts. As a result the name of the
      * executable it generates became scala-pbc. To avoid breakage we create under the scalapb.scripts the scripts
      * with the names we would like to feed into scala-native-packager. We keep the original scalapb.ScalaPBC to not
      * break integrations that use it (maven, pants), but we still want to exclude it below so a script named scala-pbc
      * is not generated for it.
      */
    discoveredMainClasses in Compile := (discoveredMainClasses in Compile).value
      .filter(_.startsWith("scalapb.scripts.")),
    mainClass in Compile := Some("scalapb.scripts.scalapbc"),
    maintainer := "thesamet@gmail.com"
  )

lazy val protocGenScalapbUnix = project
  .enablePlugins(AssemblyPlugin, GraalVMNativeImagePlugin)
  .dependsOn(scalapbc)
  .settings(
    graalVMNativeImageOptions ++= Seq(
      "-H:ReflectionConfigurationFiles=" + baseDirectory.value + "/native-image-config/reflect-config.json",
      "-H:Name=protoc-gen-scalapb"
    ) ++ (
      if (System.getProperty("os.name").contains("linux"))
        Seq("--static")
      else Seq.empty,
    ),
    assemblyOption in assembly := (assemblyOption in
      assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = true))
    ),
    skip in publish := true,
    mainClass in Compile := Some("scalapb.scripts.ProtocGenScala")
  )

lazy val protocGenScalapbWindows = project
  .enablePlugins(AssemblyPlugin)
  .dependsOn(scalapbc)
  .settings(
    assemblyOption in assembly := (assemblyOption in
      assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = false))
    ),
    skip in publish := true,
    mainClass in Compile := Some("scalapb.scripts.ProtocGenScala")
  )

lazy val protocGenScalapb = project
  .settings(
    crossScalaVersions := List(Scala212),
    name := "protoc-gen-scalapb",
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in (Compile, packageSrc) := false,
    crossPaths := false,
    addArtifact(
      Artifact("protoc-gen-scalapb", "jar", "sh", "unix"),
      assembly in (protocGenScalapbUnix, Compile)
    ),
    addArtifact(
      Artifact("protoc-gen-scalapb", "jar", "bat", "windows"),
      assembly in (protocGenScalapbWindows, Compile)
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
      ProtocJar,
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
      "io.grpc"             % "grpc-netty" % grpcVersion % "test",
      "io.grpc"             % "grpc-protobuf" % grpcVersion % "test",
      "org.scalacheck"      %% "scalacheck" % scalacheckVersion % "test",
      ScalaTest             % "test"
    ),
    libraryDependencies += { "org.scala-lang" % "scala-compiler" % scalaVersion.value },
    Test / fork := true,
    Test / baseDirectory := baseDirectory.value / "..",
    Test / javaOptions ++= Seq("-Xmx2G", "-XX:MetaspaceSize=256M"),
    // Can be removed after JDK 11.0.3 is available on Travis
    Test / javaOptions ++= (
      if (scalaVersion.value.startsWith("2.13."))
        Seq("-XX:LoopStripMiningIter=0", "-Xmx4G")
      else Nil
    )
  )

def genVersionFile(out: File, version: String): File = {
  out.mkdirs()
  val f = out / "Version.scala"
  val w = new java.io.FileOutputStream(f)
  w.write(s"""|// Generated by ScalaPB's build.sbt.
              |
              |package scalapb
              |
              |object Version {
              |  val sbtPluginVersion = "$sbtPluginVersion"
              |  val scalapbVersion = "$version"
              |}
              |""".stripMargin.getBytes("UTF-8"))
  w.close()
  f
}

val createVersionFile =
  TaskKey[Unit]("create-version-file", "Creates a file with the project version to be used by e2e.")

createVersionFile := {
  val v    = (version in Compile).value
  val log  = streams.value.log
  val base = baseDirectory.value
  val f1   = genVersionFile(base / "e2e/project/project", v)
  log.info(s"Created $f1")
  val f2 = genVersionFile(base / "e2e/project/", v)
  log.info(s"Created $f2")
}

lazy val lenses = crossProject(JSPlatform, JVMPlatform /*, NativePlatform*/ )
  .in(file("lenses"))
  .settings(
    name := "lenses",
    unmanagedSourceDirectories in Compile ++= {
      val base = (baseDirectory in LocalRootProject).value / "lenses" / "shared" / "src" / "main"
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, v)) if v < 13 =>
          Seq(base / "scala-pre-2.13")
        case _ =>
          Nil
      }
    },
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % utestVersion.value % "test"
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "lenses" % MimaPreviousVersion),
    mimaBinaryIssueFilters ++= {
      import com.typesafe.tools.mima.core._
      Seq(
        ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.lenses.Lens.setIfDefined"),
        // introduced in 2.12.9
        ProblemFilters.exclude[IncompatibleSignatureProblem]("*")
      )
    }
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
  .settings(
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s" % "0.9.0-M1"
    ),
    micrositeName := "ScalaPB",
    micrositeCompilingDocsTool := WithMdoc,
    mdocIn := baseDirectory.value / "src" / "main" / "markdown",
    micrositeDescription := "Protocol buffer compiler for Scala",
    micrositeDocumentationUrl := "/",
    micrositeAuthor := "Nadav Samet",
    micrositeGithubOwner := "scalapb",
    micrositeGithubRepo := "ScalaPB",
    micrositeGitterChannelUrl := "trueaccord/ScalaPB",
    micrositeHighlightTheme := "atom-one-light",
    micrositeHighlightLanguages := Seq("scala", "java", "bash", "protobuf"),
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
