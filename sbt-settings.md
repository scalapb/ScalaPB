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
however this is configurable using the `sourceDirectories in PB.protobufConfig` setting.

The sbt-scalapb plugin uses
[sbt-protobuf](https://github.com/sbt/sbt-protobuf) so all the options of
sbt-protobuf are available through sbt-scalapb.

## Running on Windows

Generating Scala code on Windows requires Python 2.x to be installed on your
system.  If Python is not installed on your system, you can [download it from
here](https://www.python.org/downloads/windows/).

If Python.exe can be found in your PATH, then ScalaPB should just work.  If
not, you can set the location of the Python executable explicitly:

    PB.pythonExecutable in PB.protobufConfig := "C:\\Python27\\Python.exe"

## Java Conversions

To enable Java conversions add the following to your build.sbt:

    PB.javaConversions in PB.protobufConfig := true

## GRPC

Generating GRPC stubs for services is enabled by default. To disable:

    PB.grpc := false

## Flat Packages

You can request that ScalaPB will not append the protofile base name
by adding:

    PB.flatPackage in PB.protobufConfig := true

## Single-line toString

By default, ScalaPB generates a `toString()` method that renders the message
as a multi-line format (using `TextFormat.printToUnicodeString`). If you
would like ScalaPB to generate `toString()` methods that use the single line
format, use:

    PB.singleLineToString in PB.protobufConfig := true

This option is available since ScalaPB 0.5.25.

## Pulling a newer version of the Scala code generator

The sbt-scalapb plugin is configured to use a specific version of the code
generator. However, if you would like to use a newer version, add the
following line to your `project/scalapb.sbt`:

{%highlight scala%}
// Controls which version is used by sbt-plugin
// to generate your code.
libraryDependencies ++= Seq(
  "com.trueaccord.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"
)
{%endhighlight%}

You should also pull a corresponding version of ScalaPB runtime library into
your build by adding the following line to your `build.sbt` file:

{%highlight scala%}
// Controls which version of ScalaPB runtime is added to the
// project's library dependencies.
PB.scalapbVersion := "{{site.data.version.scalapb}}"
{%endhighlight%}

## Running without protoc

By default, ScalaPB assumes there is a protoc installed somewhere on your machine (you can
tell it where to look for it using `PB.protoc`).  Sometimes it is desirable to
avoid this additional dependency and have your SBT build self-contained.
To achieve that, you can use [protoc-jar](https://github.com/os72/protoc-jar)
which provides a JAR containing protoc executables for Windows, Mac and Linux.  protoc-jar is
not affiliated with ScalaPB.  To get started, add the following lines to your
`project/scalapb.sbt`:

{%highlight scala%}
libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "2.x.5"
)
{%endhighlight%}

and the following lines to your `build.sbt`:

{%highlight scala%}
PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray))
{%endhighlight%}

Replace `-v261` with the version of protoc you would like to run. See
[protoc-jar](https://github.com/os72/protoc-jar) for more details.

