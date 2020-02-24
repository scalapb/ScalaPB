import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}
import com.typesafe.tools.mima.core._

val Scala212 = "2.12.10"

val Scala213 = "2.13.1"

val protobufVersion = "3.11.4"

// Different version for compiler-plugin since >=3.8.0 is not binary
// compatible with 3.7.x. When loaded inside SBT (which has its own old
// version), the binary incompatibility surfaces.
val protobufCompilerVersion = "3.7.1"

val grpcVersion = "1.27.1"

val MimaPreviousVersion = "0.9.0"

val ProtocJar = "com.github.os72" % "protoc-jar" % "3.11.1"

val ScalaTest = "org.scalatest" %% "scalatest" % "3.1.1"

val ScalaTestPlusScalaCheck = "org.scalatestplus" %% "scalacheck-1-14" % "3.1.0.1"

val ScalaTestPlusMockito = "org.scalatestplus" %% "mockito-1-10" % "3.1.0.0"

val utestVersion = "0.7.4"

val fastparseVersion = "2.2.4"

val silencerVersion = "1.5.0"

val collectionCompatVersion = "2.1.4"

ThisBuild / scalaVersion := Scala212

ThisBuild / crossScalaVersions := Seq(Scala212, Scala213)

ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-target:jvm-1.8",
  "-feature",
  "-Xfatal-warnings",
  "-explaintypes",
  "-Xlint:adapted-args",           // Warn if an argument list is modified to match the receiver.
  "-Xlint:constant",               // Evaluation of a constant arithmetic expression results in an error.
  "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
  "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
  "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
  "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
  "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
  "-Xlint:nullary-override",       // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
  "-Xlint:option-implicit",        // Option.apply used implicit view.
  "-Xlint:package-object-classes", // Class or object defined in package object.
  "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
  "-Xlint:private-shadow",         // A private field (or class parameter) shadows a superclass field.
  "-Xlint:stars-align",            // Pattern sequence wildcard must align with sequence component.
  "-Xlint:type-parameter-shadow",  // A local type parameter shadows a type already in scope.
  "-Ywarn-dead-code",              // Warn when dead code is identified.
  "-Ywarn-extra-implicit",         // Warn when more than one implicit parameter section is defined.
  "-Ywarn-numeric-widen",          // Warn when numerics are widened.
  "-Ywarn-unused:implicits",       // Warn if an implicit parameter is unused.
  "-Ywarn-unused:imports",         // Warn if an import selector is not referenced.
  "-Ywarn-unused:locals",          // Warn if a local definition is unused.
  "-Ywarn-unused:params",          // Warn if a value parameter is unused.
  "-Ywarn-unused:patvars",         // Warn if a variable bound in a pattern is unused.
  "-Ywarn-unused:privates",        // Warn if a private member is unused.
  "-Ywarn-value-discard",          // Warn when non-Unit expression results are unused.
  "-Ybackend-parallelism",
  "8",                                         // Enable paralellisation — change to desired number!
  "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
  "-Ycache-macro-class-loader:last-modified"   // and macro definitions. This can lead to performance improvements.
)

ThisBuild / javacOptions ++= List("-target", "8", "-source", "8")

ThisBuild / organization := "com.thesamet.scalapb"

ThisBuild / resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

ThisBuild / publishTo := sonatypePublishToBundle.value

ThisBuild / libraryDependencies ++= Seq(
  sbt.compilerPlugin(
    "com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full
  ),
  "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
)

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
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion,
      "com.lihaoyi"            %%% "fastparse"               % fastparseVersion,
      "com.google.protobuf"    % "protobuf-java"             % protobufVersion % "protobuf",
      "com.lihaoyi"            %%% "utest"                   % utestVersion % "test",
      "commons-codec"          % "commons-codec"             % "1.14" % "test",
      "com.google.protobuf"    % "protobuf-java-util"        % protobufVersion % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    Compile / unmanagedResourceDirectories += baseDirectory.value / "../../protobuf",
    scalacOptions ++= Seq(
      "-P:silencer:globalFilters=avaGenerateEqualsAndHash in class .* is deprecated",
      "-P:silencer:lineContentFilters=import scala.collection.compat._"
    ),
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
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.ScalaPbOptions.*"),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.hasEnumValueNaming"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.getEnumValueNaming"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#EnumValueOptionsOrBuilder.hasScalaName"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#EnumValueOptionsOrBuilder.getScalaName"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.Scalapb#EnumValueOptionsOrBuilder.getScalaNameBytes"
        ),
        ProblemFilters.exclude[DirectMissingMethodProblem](
          "scalapb.options.EnumValueOptions.apply"
        ),
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.EnumValueOptions.of"),
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.EnumValueOptions.copy"),
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.EnumValueOptions.this"),
        ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.EnumValueOptions.of"),
        ProblemFilters.exclude[DirectMissingMethodProblem](
          "scalapb.options.EnumValueOptions.apply"
        ),
        // Added Recognized
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.FieldDescriptorProto#Label.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.FieldOptions#CType.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.FieldOptions#JSType.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.FieldDescriptorProto#Type.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.FileOptions#OptimizeMode.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.descriptor.MethodOptions#IdempotencyLevel.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.struct.NullValue.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.type.Syntax.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.type.Field#Kind.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "com.google.protobuf.type.Field#Cardinality.asRecognized"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.ScalaPbOptions#OptionsScope.asRecognized"
        )
      )
    }
  )
  .dependsOn(lenses)
  .platformsSettings(JSPlatform /*, NativePlatform*/ )(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.8.4"
    ),
    (Compile / unmanagedSourceDirectories) += baseDirectory.value / ".." / "non-jvm" / "src" / "main" / "scala"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf"   % "protobuf-java" % protobufVersion,
      ScalaTest               % "test",
      ScalaTestPlusScalaCheck % "test"
    ),
    // Can be removed after JDK 11.0.3 is available on Travis
    Test / javaOptions ++= (
      if (scalaVersion.value.startsWith("2.13."))
        Seq("-XX:LoopStripMiningIter=0")
      else Nil
    ),
    Compile / PB.targets ++= Seq(
      PB.gens.java(protobufVersion) -> (Compile / sourceManaged).value
    ),
    Compile / PB.protocVersion := "-v" + protobufVersion,
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
      "io.grpc"            % "grpc-stub" % grpcVersion,
      "io.grpc"            % "grpc-protobuf" % grpcVersion,
      ScalaTest            % "test",
      ScalaTestPlusMockito % "test",
      "org.mockito"        % "mockito-core" % "3.2.0" % "test"
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
    crossScalaVersions := Seq(Scala212, Scala213),
    Compile / sourceGenerators += Def.task {
      val file = (Compile / sourceManaged).value / "scalapb" / "compiler" / "Version.scala"
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
      val dest = (Compile / resourceManaged).value / "protobuf" / "scalapb" / "scalapb.proto"
      val s    = IO.read(src).replace("scalapb.options", "scalapb.options.compiler")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    },
    Compile / PB.generate := {
      scalapbProtoPackageReplaceTask.value
      (Compile / PB.generate).value
    },
    Compile / sourceGenerators += Def.task {
      val src =
        baseDirectory.value / ".." / "scalapb-runtime" / "shared" / "src" / "main" / "scala" / "scalapb" / "Encoding.scala"
      val dest =
        (Compile / sourceManaged).value / "scalapb" / "compiler" / "internal" / "Encoding.scala"
      val s = IO.read(src).replace("package scalapb", "package scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }.taskValue,
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion,
      "com.thesamet.scalapb"   %% "protoc-bridge" % "0.7.14",
      "com.google.protobuf"    % "protobuf-java" % protobufCompilerVersion % "protobuf",
      ScalaTest                % "test",
      ProtocJar                % "test"
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
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.hasEnumValueNaming"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.getEnumValueNaming"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#EnumValueOptionsOrBuilder.hasScalaName"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#EnumValueOptionsOrBuilder.getScalaName"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "scalapb.options.compiler.Scalapb#EnumValueOptionsOrBuilder.getScalaNameBytes"
        )
      )
    },
    Compile / PB.protocVersion := "-v" + protobufCompilerVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufCompilerVersion) -> (Compile / sourceManaged).value / "java_out"
    ),
    Compile / PB.protoSources := Seq((Compile / resourceManaged).value / "protobuf")
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
              if e.label == "dependency" && e.child.exists(child =>
                child.label == "artifactId" && child.text.startsWith("compilerplugin")
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
    Compile / discoveredMainClasses := (Compile / discoveredMainClasses).value
      .filter(_.startsWith("scalapb.scripts.")),
    Compile / mainClass := Some("scalapb.scripts.scalapbc"),
    maintainer := "thesamet@gmail.com"
  )

lazy val protocGenScalaUnix = project
  .enablePlugins(AssemblyPlugin, GraalVMNativeImagePlugin)
  .dependsOn(scalapbc)
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
    Compile / mainClass := Some("scalapb.scripts.ProtocGenScala")
  )

lazy val protocGenScalaWindows = project
  .enablePlugins(AssemblyPlugin)
  .dependsOn(scalapbc)
  .settings(
    assemblyOption in assembly := (assemblyOption in
      assembly).value.copy(
      prependShellScript = Some(sbtassembly.AssemblyPlugin.defaultUniversalScript(shebang = false))
    ),
    skip in publish := true,
    Compile / mainClass := Some("scalapb.scripts.ProtocGenScala")
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
      ProtocJar,
      "com.google.protobuf"   % "protobuf-java" % protobufVersion,
      "io.grpc"               % "grpc-netty" % grpcVersion % "test",
      "io.grpc"               % "grpc-protobuf" % grpcVersion % "test",
      ScalaTest               % "test",
      ScalaTestPlusScalaCheck % "test"
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
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompatVersion,
      "com.lihaoyi"            %%% "utest"                   % utestVersion % "test"
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
  .dependsOn(runtimeJVM)
  .settings(
    scalaVersion := Scala212,
    crossScalaVersions := Seq(Scala212),
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "scalapb-json4s"   % "0.10.0-M3",
      "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.9.0",
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
    ScalaTest               % "test",
    ScalaTestPlusScalaCheck % "test",
    "io.grpc"               % "grpc-netty" % grpcVersion, //netty transport of grpc
    "io.grpc"               % "grpc-protobuf" % grpcVersion, //protobuf message encoding for java implementation
    "io.grpc"               % "grpc-services" % grpcVersion,
    "io.grpc"               % "grpc-services" % grpcVersion % "protobuf",
    "javax.annotation"      % "javax.annotation-api" % "1.3.2" // needed for grpc-java on JDK9
  ),
  libraryDependencies += ("io.grpc" % "protoc-gen-grpc-java" % grpcVersion) asProtocPlugin (),
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
    Compile / PB.protocVersion := "-v" + protobufVersion,
    Compile / PB.targets := Seq(
      PB.gens.java(protobufVersion) -> (Compile / sourceManaged).value,
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
    Compile / PB.protocVersion := "-v" + protobufVersion,
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
