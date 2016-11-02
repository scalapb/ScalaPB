---
title: "ScalaPB: SBT Settings"
layout: page
---

# SBT Settings

## Basic Installation

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.thesamet" % "sbt-protoc" % "{{site.data.version.sbt_protoc}}")

    libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"

Add the following line to your `build.sbt`:

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )

    // If you need scalapb/scalapb.proto or anything from
    // google/protobuf/*.proto
    libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

Running the `compile` command in sbt will both generate Scala sources from
your protos and compile them. If you just want to generate Scala sources for
your protocol buffers without compiling them, run `protoc-generate`

## Defaults

The plugin assumes your `proto` files are under `src/main/protobuf`,
however this is configurable using the `PB.protoSources in Compile` setting.

By default, `sbt-protoc` invokes `protoc` 3.0.0 that is shipped with `protoc-jar`.
If you would like to run a different version of `protoc`:

    PB.protocVersion := "-v261"

See all available options in [sbt-protoc documentation](https://github.com/thesamet/sbt-protoc)

## Running on Windows

Generating Scala code on Windows requires Python 2.x to be installed on your
system.  If Python is not installed on your system, you can [download it from
here](https://www.python.org/downloads/windows/).

If Python.exe can be found in your PATH, then ScalaPB should just work.  If
not, you can set the location of the Python executable explicitly:

    PB.pythonExe := "C:\\Python27\\Python.exe"

## Java Conversions

To enable Java conversions add the following to your build.sbt:

    PB.targets in Compile := Seq(
      PB.gens.java -> (sourceManaged in Compile).value,
      scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
    )

## GRPC

Generating GRPC stubs for services is enabled by default. To disable:

    PB.targets in Compile := Seq(
      scalapb.gen(grpc=false) -> (sourceManaged in Compile).value
    )

## Additional options to the generator

```
scalapb.gen(
  flatPackage: Boolean = false,
  javaConversions: Boolean = false,
  grpc: Boolean = true,
  singleLineToString: Boolean = false)
```

**`flatPackage`**: When true, ScalaPB will not append the protofile base name
to the package name.

**`singleLineToString`**: By default, ScalaPB generates a `toString()` method
that renders the message as a multi-line format (using `TextFormat.printToUnicodeString`).
Set to true If you would like ScalaPB to generate `toString()` methods that use the single line
format.
