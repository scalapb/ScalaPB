import ReleaseTransformations._

scalaVersion in ThisBuild := "2.12.3"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.11", "2.12.3")

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

organization in ThisBuild := "com.trueaccord.scalapb"

resolvers in ThisBuild +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

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
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true)
)

lazy val root =
  project.in(file("."))
    .settings(
      publishArtifact := false,
      publish := {},
      publishLocal := {}
    ).aggregate(
      runtimeJS, runtimeJVM, grpcRuntime, compilerPlugin, compilerPluginShaded, proptest, scalapbc)

lazy val runtime = crossProject.crossType(CrossType.Full).in(file("scalapb-runtime"))
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      "com.trueaccord.lenses" %%% "lenses" % "0.4.12",
      "com.lihaoyi" %%% "fastparse" % "0.4.3",
      "com.lihaoyi" %%% "utest" % "0.5.3" % "test",
      "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
      "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../protobuf"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % protobufVersion
    )
  )
  .jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "protobuf-runtime-scala" % "0.1.18"
    ),
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    },
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../third_party"
  )

lazy val runtimeJVM = runtime.jvm
lazy val runtimeJS = runtime.js

val grpcVersion = "1.6.1"

lazy val grpcRuntime = project.in(file("scalapb-runtime-grpc"))
  .dependsOn(runtimeJVM)
  .settings(
    name := "scalapb-runtime-grpc",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-stub" % grpcVersion,
      "io.grpc" % "grpc-protobuf" % grpcVersion,
      "org.scalatest" %% "scalatest" % "3.0.3" % "test",
      "org.mockito" % "mockito-core" % "2.8.47" % "test"
    )
  )

lazy val compilerPlugin = project.in(file("compiler-plugin"))
  .settings(
    sourceGenerators in Compile += Def.task {
      val file = (sourceManaged in Compile).value / "com" / "trueaccord" / "scalapb" / "compiler" / "Version.scala"
      IO.write(file,
        s"""package com.trueaccord.scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |  val protobufVersion = "${protobufVersion}"
           |  val grpcJavaVersion = "${grpcVersion}"
           |}""".stripMargin)
      Seq(file)
    }.taskValue,
    sourceGenerators in Compile += Def.task {
      val src = baseDirectory.value / ".." / "scalapb-runtime" / "shared" / "src" / "main" / "scala" / "com" / "trueaccord" / "scalapb" / "Encoding.scala"
      val dest = (sourceManaged in Compile).value / "com" / "trueaccord" / "scalapb" / "compiler" / "internal" /"Encoding.scala"
      val s = IO.read(src).replace("package com.trueaccord.scalapb", "package com.trueaccord.scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }.taskValue,
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "protoc-bridge" % "0.2.7",
      "org.scalatest" %% "scalatest" % "3.0.3" % "test"
    ),
    //shade our output to replace com.trueaccord.scalapb.Scalapb on the classpath, suitable for using in sbt 1.x,
    // which can cause runtime conflicts due to scalapb-runtime being included on the classpath as well
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false, includeDependency = false),
    assemblyJarName in assembly := artifactName.value.apply(ScalaVersion((scalaVersion in artifactName).value, (scalaBinaryVersion in artifactName).value), projectID.value, (artifact in (Compile, assembly)).value),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("com.trueaccord.scalapb.Scalapb**" -> "scalapbshade.@0").inProject
    ),
    packageBin in Compile := assembly.value,
    //replace the main artifact with the shaded non-fat jar
    artifact in (Compile, packageBin) := (artifact in (Compile, assembly)).value
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
      ShadeRule.rename("com.google.**" -> "scalapb.@0").inAll,
      ShadeRule.rename("org.apache.**" -> "scalapb.@0").inAll
    ),
    assemblyExcludedJars in assembly := {
      val toInclude = Seq(
        "protobuf-java",
        "protoc-bridge",
        "commons-io"
      )

      (fullClasspath in assembly).value.filterNot {
        c => toInclude.exists(prefix => c.data.getName.startsWith(prefix))
      }
    },
    artifact in (Compile, packageBin) := {
      val art = (artifact in (Compile, assembly)).value
      // art.copy(`classifier` = Some("assembly"))
      art
    },
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
        "com.github.os72" % "protoc-jar" % "3.4.0",
        "com.google.protobuf" % "protobuf-java" % protobufVersion,
        "io.grpc" % "grpc-netty" % grpcVersion % "test",
        "io.grpc" % "grpc-protobuf" % grpcVersion % "test",
        "com.trueaccord.lenses" %% "lenses" % "0.4.12",
        "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.5",
        "org.scalacheck" %% "scalacheck" % "1.13.5" % "test",
        "org.scalatest" %% "scalatest" % "3.0.3" % "test"
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

val protobufVersion = "3.4.0"

// For e2e test
val sbtPluginVersion = "0.99.11"

def genVersionFile(out: File, version: String): File = {
  out.mkdirs()
  val f = out / "Version.scala"
  val w = new java.io.FileOutputStream(f)
  w.write(s"""|// Generated by ScalaPB's build.sbt.
              |
              |package com.trueaccord.scalapb
              |
              |object Version {
              |  val sbtPluginVersion = "$sbtPluginVersion"
              |  val scalapbVersion = "$version"
              |  val grpcJavaVersion = "${grpcVersion}"
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

