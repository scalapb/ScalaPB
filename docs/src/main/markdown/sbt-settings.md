---
title: "SBT Settings"
layout: docs
---

# SBT Settings

## Basic Installation

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "{{site.data.version.sbt_protoc}}")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"
```

Add the following line to your `build.sbt`:

```scala
PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)
```

Running the `compile` command in sbt will both generate Scala sources from
your protos and compile them. If you just want to generate Scala sources for
your protocol buffers without compiling them, run `protoc-generate`

Additionally, if you need [customizations]({{site.baseurl}}/customizations.html) from
scalapb/scalapb.proto or anything from `google/protobuf/*.proto`, add the
following to your `build.sbt`:

```scala
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
```

## Defaults

The plugin assumes your `proto` files are under `src/main/protobuf`,
however this is configurable using the `PB.protoSources in Compile` setting.

By default, `sbt-protoc` invokes `protoc` 3.0.0 that is shipped with `protoc-jar`.
If you would like to run a different version of `protoc`:

```scala
PB.protocVersion := "-v261"
```

See all available options in [sbt-protoc documentation](https://github.com/thesamet/sbt-protoc)

## Running on Windows

Before sbt-protoc 0.99.15, generating Scala code on Windows required Python 2.x
to be installed on your system. If you are using sbt-protoc 0.99.15 or later,
then things should just work.

If you are using an older version of sbt-protoc and unable to upgrade, then
you must have Python 2.x installed on your system.  If Python is not installed
on your system, you can [download it from here](https://www.python.org/downloads/windows/).

If Python.exe can be found in your PATH, then ScalaPB should just work.  If
not, you can set the location of the Python executable explicitly:

```scala
PB.pythonExe := "C:\\Python27\\Python.exe"
```

## Java Conversions

To enable Java conversions add the following to your build.sbt:

```scala
PB.targets in Compile := Seq(
  PB.gens.java -> (sourceManaged in Compile).value,
  scalapb.gen(javaConversions=true) -> (sourceManaged in Compile).value
)
```

## gRPC

Generating gRPC stubs for services is enabled by default. To disable:

```scala
PB.targets in Compile := Seq(
  scalapb.gen(grpc=false) -> (sourceManaged in Compile).value
)
```

## Additional options to the generator

```scala
scalapb.gen(
  flatPackage: Boolean = false,
  javaConversions: Boolean = false,
  grpc: Boolean = true,
  singleLineToProtoString: Boolean = false,
  asciiFormatToString: Boolean = false,
  lenses: Boolean = true,
  retainSourceCodeInfo: Boolean = false,
  oneofsAfterFieldsInConstructor: Boolean = false
)
```

Note that in ScalaPB 0.7, `singleLineToString` has been renamed to 
`singleLineToProtoString`.

**`flatPackage`**: When true, ScalaPB will not append the protofile base name
to the package name.

**`singleLineToProtoString`**: By default, ScalaPB generates a `toProtoString()` method
that renders the message as a multi-line format (using `TextFormat.printToUnicodeString`).
Set to true If you would like ScalaPB to generate `toString()` methods that use the single line
format.

**`asciiFormatToString`**: Setting this to true, overrides `toString` to
return a standard ASCII representation of the message by calling
`toProtoString`.

**`lenses`**: By default, ScalaPB generates lenses for each message for easy
updating. If you are not using this feature and would like to reduce code size
or compilation time, you can set this to `false` and lenses will not be
generated.

**`retainSourceCodeInfo`**: Retain source code information (locations,
comments) provided by protoc in the descriptors. Use the `location` accessor
to get that information from a descriptor.

**`oneofsAfterFieldsInConstructor`**: When set to `true`, brings back the deprecated
behavior for constructor parameters order: up to ScalaPB 0.9.1, oneof fields appeared
last in the constructor parameters list for generated messages, no matter where they
were declared in the proto file. This flag was introduced to provide a smoother
transition for clients that rely on the legacy order.