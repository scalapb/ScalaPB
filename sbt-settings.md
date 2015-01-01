---
title: "ScalaPB: SBT Settings"
layout: page
---

# SBT Settings

## Basic Installation

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "{{site.data.version.sbt_scalapb}}")

Add the following line to your `build.sbt`:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

    PB.protobufSettings

Running the `compile` command in sbt will both generate Scala sources from your protos and compile them. If you just want to generate Scala sources for your protocol buffers without compiling them, run `protobuf:protobufScalaGenerate`

## Defaults

By default, the plugin assumes your `proto` files are under `src/main/protobuf`,
however this is configurable using the `PB.sourceDirectories` setting.

The sbt-scalapb plugin uses
[sbt-protobuf](https://github.com/sbt/sbt-protobuf) so all the options of
sbt-protobuf are available through sbt-scalapb.

## Java Conversions

To enable Java conversions add the following to your build.sbt:

    PB.javaConversions in PB.protobufConfig := true

## Pulling a newer version of the Scala code generator

The sbt-scalapb plugin is configured to use a specific version of the code
generator. However, if you would like to use a newer version, add the
following like to your `project/scalapb.sbt`:

{%highlight scala%}
// Controls which version is used by sbt-plugin
// to generate your code.
libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"
)
{%endhighlight%}

You should also pull a corresponding version of ScalaPB runtime library into
your build by adding the following like to your `build.sbt` file:

{%highlight scala%}
// Controls which version of ScalaPB runtime is added to the
// project's library dependencies.
PB.scalapbVersion := "{{site.data.version.scalapb}}"
{%endhighlight%}
