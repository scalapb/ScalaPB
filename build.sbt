import SonatypeKeys._

scalaVersion := "2.11.6"

sonatypeSettings

crossScalaVersions := Seq("2.11.6", "2.10.5", "2.12.0-M1")

organization := "com.trueaccord.lenses"

profileName := "com.trueaccord"

scalacOptions += "-target:jvm-1.7"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.4" % "test",
  "org.scalatest" %% "scalatest" % (if (scalaVersion.value.startsWith("2.12")) "2.2.5-M1" else "2.2.5") % "test"
)

pomExtra in ThisBuild := {
  <url>https://github.com/trueaccord/lenses</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:github.com:trueaccord/lenses.git</connection>
    <developerConnection>scm:git:git@github.com:trueaccord/lenses.git</developerConnection>
    <url>github.com/trueaccord/lenses</url>
  </scm>
  <developers>
    <developer>
      <id>thesamet</id>
      <name>Nadav S. Samet</name>
      <url>http://www.thesamet.com/</url>
    </developer>
  </developers>
}

releaseSettings

ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value

ReleaseKeys.crossBuild := true

