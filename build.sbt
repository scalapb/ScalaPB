import ReleaseTransformations._
import sbtcrossproject.CrossPlugin.autoImport.crossProject

val Scala211 = "2.11.12"

scalaVersion in ThisBuild := Scala211

crossScalaVersions := Seq(Scala211, "2.10.6", "2.12.2", "2.13.0-M2")

organization in ThisBuild := "com.thesamet.scalapb"

scalacOptions in ThisBuild ++= {
  CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, v)) if v <= 11 => List("-target:jvm-1.7")
    case _ => Nil
  }
}

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
  releaseStepCommandAndRemaining(s";++${Scala211};lensesNative/publishSigned"),
  ReleaseStep(action = Command.process("publishSigned", _), enableCrossBuild = true),
  setNextVersion,
  commitNextVersion,
  pushChanges,
  ReleaseStep(action = Command.process("sonatypeReleaseAll", _), enableCrossBuild = true)
)

lazy val root = project.in(file("."))
  .aggregate(lensesJS, lensesJVM)
  .settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false
  )

lazy val lenses = crossProject(JSPlatform, JVMPlatform, NativePlatform).in(file("."))
  .settings(
    name := "lenses"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.13.5" % "test",
      "org.scalatest" %%% "scalatest" % "3.0.4" % "test"
    )
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/scalapb/Lenses/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )

lazy val lensesJVM = lenses.jvm
lazy val lensesJS = lenses.js
lazy val lensesNative = lenses.native
