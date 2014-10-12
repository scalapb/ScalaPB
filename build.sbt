import SonatypeKeys._

scalaVersion := "2.11.3"

sonatypeSettings

crossScalaVersions := Seq("2.11.2", "2.10.4")

organization in ThisBuild := "com.trueaccord.lenses"

 profileName in ThisBuild := "com.trueaccord.lenses"

version in ThisBuild := "0.1-SNAPSHOT"

libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.11.6" % "test",
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
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

