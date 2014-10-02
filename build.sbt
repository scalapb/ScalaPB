scalaVersion in ThisBuild := "2.11.2"

lazy val root =
  project.in(file("."))
    .aggregate(runtime, compilerPlugin, integration)

lazy val runtime = project in file("scalapb-runtime")

lazy val compilerPlugin = project in file("compiler-plugin")

lazy val integration = project.in(file("integration"))
  .dependsOn(runtime, compilerPlugin)
