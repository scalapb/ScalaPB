import ReleaseTransformations._

scalaVersion in ThisBuild := "2.11.7"

crossScalaVersions in ThisBuild := Seq("2.10.6", "2.11.7"
  // "2.12.0-M2"  // disabled until fastparse releases for 2.12
)

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
      runtimeJS, runtimeJVM, grpcRuntime, compilerPlugin, proptest, scalapbc)

lazy val runtime = crossProject.crossType(CrossType.Full).in(file("scalapb-runtime"))
  .settings(
    name := "scalapb-runtime",
    libraryDependencies ++= Seq(
      "com.trueaccord.lenses" %%% "lenses" % "0.4.4",
      "com.lihaoyi" %%% "fastparse" % "0.3.4",
      "com.lihaoyi" %%% "utest" % "0.3.1" % "test",
      "org.scalacheck" %% "scalacheck" % "1.12.5" % "test",
      "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M2" else "2.2.5") % "test"
    ),
    testFrameworks += new TestFramework("utest.runner.Framework"),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../protobuf"
  )
  .jvmSettings(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-2"
    )
  )
  .jsSettings(
    // Add JS-specific settings here
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %%% "protobuf-runtime-scala" % "0.1.6"
    ),
    unmanagedResourceDirectories in Compile += baseDirectory.value / "../../third_party"
  )

lazy val runtimeJVM = runtime.jvm
lazy val runtimeJS = runtime.js

val grpcVersion = "0.13.1"

lazy val grpcRuntime = project.in(file("scalapb-runtime-grpc"))
  .dependsOn(runtimeJVM)
  .settings(
    name := "scalapb-runtime-grpc",
    libraryDependencies ++= Seq(
      "io.grpc" % "grpc-all" % grpcVersion
    )
  )

lazy val compilerPlugin = project.in(file("compiler-plugin"))
  .dependsOn(runtimeJVM)
  .settings(
    sourceGenerators in Compile <+= Def.task {
      val file = (sourceManaged in Compile).value / "com" / "trueaccord" / "scalapb" / "compiler" / "Version.scala"
      IO.write(file,
        s"""package com.trueaccord.scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |}""".stripMargin)
      Seq(file)
    },
    libraryDependencies ++= Seq(
      "com.trueaccord.scalapb" %% "protoc-bridge" % "0.1.4"
      ))

lazy val scalapbc = project.in(file("scalapbc"))
  .dependsOn(compilerPlugin, runtimeJVM)
  .settings(
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
  )

lazy val proptest = project.in(file("proptest"))
  .dependsOn(runtimeJVM, grpcRuntime, compilerPlugin)
    .configs( ShortTest )
    .settings( inConfig(ShortTest)(Defaults.testTasks): _*)
    .settings(
      publishArtifact := false,
      publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
      libraryDependencies ++= Seq(
        "com.github.os72" % "protoc-jar" % "3.0.0-b1",
        "com.google.protobuf" % "protobuf-java" % "3.0.0-beta-2",
        "io.grpc" % "grpc-all" % grpcVersion % "test",
        "com.trueaccord.lenses" %% "lenses" % "0.4.1",
        "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
        "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M2" else "2.2.5") % "test"
      ),
      libraryDependencies <+= (scalaVersion) { v => "org.scala-lang" % "scala-compiler" % v },
      testOptions += Tests.Argument(),
      fork in Test := false,
      testOptions in ShortTest += Tests.Argument(
        // verbosity specified because of ScalaCheck #108.
        "-verbosity", "3",
        "-minSuccessfulTests", "10")
    )

lazy val ShortTest = config("short") extend(Test)

// For e2e test
val sbtPluginVersion = "0.5.18"

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
              |}
              |""".stripMargin.getBytes("UTF-8"))
  w.close()
  f
}

val createVersionFile = TaskKey[Unit](
  "create-version-file", "Creates a file with the project version to be used by e2e.")

createVersionFile <<= (streams, baseDirectory, version in Compile) map {
  (streams, baseDirectory, version) =>
    val f1 = genVersionFile(baseDirectory / "e2e/project/project", version)
    streams.log.info(s"Created $f1")
    val f2 = genVersionFile(baseDirectory / "e2e/project/", version)
    streams.log.info(s"Created $f2")
}

