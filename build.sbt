import ReleaseTransformations._

scalaVersion := "2.11.8"

crossScalaVersions := Seq("2.11.8", "2.10.6", "2.12.0-M4")

organization in ThisBuild := "com.trueaccord.lenses"

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

lazy val lenses = crossProject.in(file("."))
  .settings(
    name := "lenses"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalacheck" %%% "scalacheck" % "1.12.5" % "test",
      "org.scalatest" %%% "scalatest" % "2.2.6" % "test"
    )
  )
  .jsSettings(
    scalacOptions += {
      val a = (baseDirectory in LocalRootProject).value.toURI.toString
      val g = "https://raw.githubusercontent.com/trueaccord/Lenses/" + sys.process.Process("git rev-parse HEAD").lines_!.head
      s"-P:scalajs:mapSourceURI:$a->$g/"
    }
  )

lazy val lensesJVM = lenses.jvm
lazy val lensesJS = lenses.js

