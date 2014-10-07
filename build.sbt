import SonatypeKeys._

sonatypeSettings

crossScalaVersions := Seq("2.10.4", "2.11.2")

organization in ThisBuild := "com.trueaccord.scalapb"

profileName in ThisBuild:= "com.trueaccord.scalapb"

version in ThisBuild := "0.1-SNAPSHOT"

pomExtra in ThisBuild := {
  <url>https://github.com/trueaccord/ScalaPB</url>
  <licenses>
    <license>
      <name>Apache 2</name>
      <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
    </license>
  </licenses>
  <scm>
    <connection>scm:git:github.com:trueaccord/ScalaPB.git</connection>
    <developerConnection>scm:git:git@github.com:trueaccord/ScalaPB.git</developerConnection>
    <url>github.com/trueaccord/ScalaPB</url>
  </scm>
  <developers>
    <developer>
      <id>thesamet</id>
      <name>Nadav S. Samet</name>
      <url>http://www.thesamet.com/</url>
    </developer>
  </developers>
}

lazy val root =
  project.in(file("."))
    .settings(publishArtifact := false)
    .aggregate(runtime, compilerPlugin, integration)

lazy val runtime = project in file("scalapb-runtime")

lazy val compilerPlugin = project in file("compiler-plugin")

lazy val integration = project.in(file("integration"))
  .dependsOn(runtime, compilerPlugin)
    .configs( ShortTest )
    .settings( inConfig(ShortTest)(Defaults.testTasks): _*)
    .settings(
      publishArtifact := false,
      publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))),
      testOptions in ShortTest += Tests.Argument(
        // verbosity specified because of ScalaCheck #108.
        "-verbosity", "0",
        "-minSuccessfulTests", "10")
    )

lazy val sbtPlugin = project.in(file("sbt-plugin"))
  .dependsOn(compilerPlugin)

lazy val ShortTest = config("short") extend(Test)
