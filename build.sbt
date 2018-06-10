import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.{CrossType, crossProject}

val Scala210 = "2.10.7"

val Scala211 = "2.11.12"

val Scala212 = "2.12.6"

val protobufVersion = "3.6.0"

val ScalaDotty = "0.8.0-RC1"

val scalacheckVersion = "1.14.0"

// For e2e test
val sbtPluginVersion = "0.99.18"

val grpcVersion = "1.14.0"

scalaVersion in ThisBuild := Scala212

val ScalaVersionsWithoutDotty = Seq(Scala210, Scala211, Scala212)

crossScalaVersions in ThisBuild := Seq(Scala210, Scala211, Scala212, ScalaDotty)

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target:jvm-1.7")
    case _ => Nil
  }
}

javacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target", "7", "-source", "7")
    case _ => Nil
  }
}

organization in ThisBuild := "com.thesamet.scalapb"

resolvers in ThisBuild +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

publishTo in ThisBuild := sonatypePublishTo.value

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
  releaseStepCommandAndRemaining(";+publishSigned"),
  releaseStepCommandAndRemaining(s";++${Scala211};runtimeNative/publishSigned;lensesNative/publishSigned"),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  ReleaseStep(action = "sonatypeReleaseAll" :: _, enableCrossBuild = true)
)

lazy val sharedNativeSettings = List(
  nativeLinkStubs := true, // for utest
  scalaVersion := Scala211,
  crossScalaVersions := List(Scala211)
)

lazy val root =
  project.in(file("."))
    .settings(
      publishArtifact := false,
      publish := {},
      publishLocal := {},
      siteSubdirName in ScalaUnidoc := "api/scalapb/latest",
      addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), siteSubdirName in ScalaUnidoc),
      unidocProjectFilter in (ScalaUnidoc, unidoc) := inProjects(lensesJVM, runtimeJVM, grpcRuntime),
      git.remoteRepo := "git@github.com:scalapb/scalapb.github.io.git",
      ghpagesBranch := "master",
      ghpagesNoJekyll := false,
      includeFilter in ghpagesCleanSite := GlobFilter((ghpagesRepository.value / "api/scalapb/latest").getCanonicalPath + java.io.File.separator + "*")
    )
    .enablePlugins(ScalaUnidocPlugin, GhpagesPlugin)
    .aggregate(
      lensesJS,
      lensesJVM,
      runtimeJS,
      runtimeJVM,
      grpcRuntime,
      compilerPlugin,
      compilerPluginShaded,
      proptest,
      scalapbc)

lazy val runtime = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full).in(file("scalapb-runtime"))
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % "1.0.0",
      "com.lihaoyi" %%% "utest" % "0.6.4" % "test",
      "commons-codec" % "commons-codec" % "1.11" % "test",
      "com.google.protobuf" % "protobuf-java-util" % protobufVersion % "test",
    ).map(_.withDottyCompat(scalaVersion.value)),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../protobuf",
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime" % "0.7.0"),
    mimaBinaryIssueFilters ++= {
        import com.typesafe.tools.mima.core._
        Seq(
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.textformat.Basics.parserToParserApi"),
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.textformat.Basics.strToParserApi"),

            // getObjectName
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.getObjectName"),
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.getObjectNameBytes"),
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.Scalapb#ScalaPbOptionsOrBuilder.hasObjectName"),
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.ScalaPbOptions.copy"),
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.ScalaPbOptions.this"),
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.options.ScalaPbOptions.apply")
        )
    }
  )
  .dependsOn(lenses)
  .platformsSettings(JSPlatform, NativePlatform)(
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %%% "protobuf-runtime-scala" % "0.7.1"
    ).map(_.withDottyCompat(scalaVersion.value)),
    (unmanagedSourceDirectories in Compile) += baseDirectory.value / ".." / "non-jvm" / "src" / "main" / "scala"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % protobufVersion,
    ),
    libraryDependencies ++= (if (!isDotty.value) {
      Seq(
          "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
          "org.scalatest" %%% "scalatest" % "3.0.5" % "test"
      )
    } else Nil),
  )
  .jsSettings(
    // Add JS-specific settings here
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process.Process("git rev-parse HEAD").lineStream_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../third_party"
  )
  .nativeSettings(
    sharedNativeSettings
  )


lazy val runtimeJVM    = runtime.jvm
lazy val runtimeJS     = runtime.js
lazy val runtimeNative = runtime.native

lazy val grpcRuntime = project.in(file("scalapb-runtime-grpc"))
  .dependsOn(runtimeJVM)
  .settings(
    name := "scalapb-runtime-grpc",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-stub" % grpcVersion,
      "io.grpc" % "grpc-protobuf" % grpcVersion,
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.mockito" % "mockito-core" % "2.10.0" % "test"
    ).map(_.withDottyCompat(scalaVersion.value)),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "scalapb-runtime-grpc" % "0.7.0")
  )

val shadeTarget = settingKey[String]("Target to use when shading")

shadeTarget in ThisBuild := s"scalapbshade.v${version.value.replaceAll("[.-]","_")}.@0"

lazy val compilerPlugin = project.in(file("compiler-plugin"))
  .settings(
    crossScalaVersions := ScalaVersionsWithoutDotty,
    sourceGenerators in Compile += Def.task {
      val file = (sourceManaged in Compile).value / "scalapb" / "compiler" / "Version.scala"
      IO.write(file,
        s"""package scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |  val protobufVersion = "${protobufVersion}"
           |  val grpcJavaVersion = "${grpcVersion}"
           |}""".stripMargin)
      Seq(file)
    }.taskValue,
    sourceGenerators in Compile += Def.task {
      val src = baseDirectory.value / ".." / "scalapb-runtime" / "shared" / "src" / "main" / "scala" / "scalapb" / "Encoding.scala"
      val dest = (sourceManaged in Compile).value / "scalapb" / "compiler" / "internal" /"Encoding.scala"
      val s = IO.read(src).replace("package scalapb", "package scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }.taskValue,
    libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "protoc-bridge" % "0.7.3",
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "com.github.os72" % "protoc-jar" % "3.5.1.1" % "test",
    ),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "compilerplugin" % "0.7.0"),
    mimaBinaryIssueFilters ++= {
        import com.typesafe.tools.mima.core._
        Seq(
            ProblemFilters.exclude[DirectMissingMethodProblem]("scalapb.compiler.ProtoValidation.ForbiddenFieldNames"),
            ProblemFilters.exclude[IncompatibleMethTypeProblem]("scalapb.compiler.FunctionalPrinter.print"),

            // objectName
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.hasObjectName"),
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.getObjectNameBytes"),
            ProblemFilters.exclude[ReversedMissingMethodProblem]("scalapb.options.compiler.Scalapb#ScalaPbOptionsOrBuilder.getObjectName"),
        )
    }
  )

// Until https://github.com/scalapb/ScalaPB/issues/150 is fixed, we are
// publishing compiler-plugin bundled with protoc-bridge, and linked against
// shaded protobuf. This is a workaround - this artifact will be removed in
// the future.
lazy val compilerPluginShaded = project.in(file("compiler-plugin-shaded"))
  .dependsOn(compilerPlugin)
  .settings(
    name := "compilerplugin-shaded",
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("scalapb.options.Scalapb**" -> shadeTarget.value).inProject,
      ShadeRule.rename("com.google.**" -> shadeTarget.value).inAll
    ),
    assemblyExcludedJars in assembly := {
      val toInclude = Seq(
        "protobuf-java",
        "protoc-bridge"
      )

      (fullClasspath in assembly).value.filterNot {
        c => toInclude.exists(prefix => c.data.getName.startsWith(prefix))
      }
    },
    artifact in (Compile, packageBin) := (artifact in (Compile, assembly)).value,
    addArtifact(artifact in (Compile, packageBin), assembly),
    pomPostProcess := { (node: scala.xml.Node) =>
      new scala.xml.transform.RuleTransformer(new scala.xml.transform.RewriteRule {
        override def transform(node: scala.xml.Node): scala.xml.NodeSeq = node match {
          case e: scala.xml.Elem if e.label == "dependency" && e.child.exists(child => child.label == "artifactId" && child.text.startsWith("compilerplugin")) =>
            scala.xml.Comment(s"compilerplugin has been removed.")
          case _ => node
        }
      }).transform(node).head
    }
  )

lazy val scalapbc = project.in(file("scalapbc"))
  .dependsOn(compilerPlugin)

lazy val proptest = project.in(file("proptest"))
  .dependsOn(runtimeJVM, grpcRuntime, compilerPlugin)
    .configs( ShortTest )
    .settings( inConfig(ShortTest)(Defaults.testTasks): _*)
    .settings(
      publishArtifact := false,
      publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
      libraryDependencies ++= Seq(
        "com.github.os72" % "protoc-jar" % "3.5.1.1",
        "com.google.protobuf" % "protobuf-java" % protobufVersion,
        "io.grpc" % "grpc-netty" % grpcVersion % "test",
        "io.grpc" % "grpc-protobuf" % grpcVersion % "test",
        "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",
        "org.scalatest" %% "scalatest" % "3.0.5" % "test"
      ),
      scalacOptions in Compile ++= Seq("-Xmax-classfile-name", "128"),
      libraryDependencies += { "org.scala-lang" % "scala-compiler" % scalaVersion.value },
      testOptions += Tests.Argument(),
      fork in Test := false,
      testOptions in ShortTest += Tests.Argument(
        // verbosity specified because of ScalaCheck #108.
        "-verbosity", "3",
        "-minSuccessfulTests", "10")
    )

lazy val ShortTest = config("short") extend(Test)

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

val createVersionFile = TaskKey[Unit](
  "create-version-file", "Creates a file with the project version to be used by e2e.")

createVersionFile := {
  val v = (version in Compile).value
  val log = streams.value.log
  val base = baseDirectory.value
  val f1 = genVersionFile(base / "e2e/project/project", v)
  log.info(s"Created $f1")
  val f2 = genVersionFile(base / "e2e/project/", v)
  log.info(s"Created $f2")
}

lazy val lenses = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file("lenses"))
  .settings(
    name := "lenses",
    sources in Test := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        case Some((2, 13)) =>
          // TODO utest_2.13.0-M3
          Nil
        case _ =>
          (sources in Test).value
      },
    },
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
        "com.lihaoyi" %%% "utest" % "0.6.3" % "test"
    ).map(_.withDottyCompat(scalaVersion.value)),
    mimaPreviousArtifacts := Set("com.thesamet.scalapb" %% "lenses" % "0.7.0"),
    mimaBinaryIssueFilters ++= {
        import com.typesafe.tools.mima.core._
        Seq(
            ProblemFilters.exclude[IncompatibleMethTypeProblem]("scalapb.lenses.Lens#MapLens.:++=$extension"),
            ProblemFilters.exclude[IncompatibleMethTypeProblem]("scalapb.lenses.Lens#MapLens.:++=")
        )
    }
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process.Process("git rev-parse HEAD").lineStream_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )
  .nativeSettings(
    sharedNativeSettings
  )

lazy val lensesJVM = lenses.jvm
lazy val lensesJS = lenses.js
lazy val lensesNative = lenses.native
