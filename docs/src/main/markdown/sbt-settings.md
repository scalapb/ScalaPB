---
title: "SBT Settings"
layout: docs
---

# SBT Settings

## Basic Installation

To automatically generate Scala case classes for your messages add ScalaPB's sbt plugin to your project. Create a file named `project/plugins.sbt` containing the following line (or add the following lines if a file with this name already exists):

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

Running the `compile` command in sbt will generate Scala sources from
your protos and compile them. If you just want to generate Scala sources for your protocol buffers without compiling them, run `protoc-generate`.

Additionally, if you need [customizations]({{site.baseurl}}/customizations.html) from
scalapb/scalapb.proto or anything from `google/protobuf/*.proto`, add the
following to your `build.sbt`:

```scala
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
```

## Defaults

The plugin assumes your `proto` files are under `src/main/protobuf`,
however this is configurable using the `PB.protoSources in Compile` setting.

By default, `sbt-protoc` invokes `protoc` 3.x that is shipped with `protoc-jar`.
If you would like to run a different version of `protoc`:

```scala
PB.protocVersion := "-v3.10.0"
```

See all available options in [sbt-protoc documentation](https://github.com/thesamet/sbt-protoc)

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
  retainSourceCodeInfo: Boolean = false
)
```

| Option | scalapbc | Description |
| ------ | -------- | ----------- |
| `flatPackage` | `flat_package` | When set, ScalaPB will not append the protofile base name to the package name. |
| `javaConversions` | `java_conversions` | Generates in the companion object two functions, `toJavaProto` and `fromJavaProto` that convert between the Scala case class and the Java protobufs. For the generated code to compile, the Java protobuf code need to be also generated or available as a library dependency. |
| `grpc` | `grpc` | Generates gRPC code for services. Default is `true` in `scalapb.gen`, and need to be explicitly specified in `scalapbc`. |
|`singleLineToProtoString` | `single_line_to_proto_string` | By default, ScalaPB generates a `toProtoString()` method that renders the message as a multi-line format (using `TextFormat.printToUnicodeString`). If set, ScalaPB generates `toString()` methods that use the single line format. |
|`asciiFormatToString` | `ascii_format_to_string` | Setting this to true, overrides `toString` to return a standard ASCII representation of the message by calling `toProtoString`. |
|`lenses` | `no_lenses` | By default, ScalaPB generates lenses for each message for easy updating. If you are not using this feature and would like to reduce code size or compilation time, you can set this to `false` and lenses will not be generated. |
| `retainSourceCodeInfo` | `retain_source_code_info` | Retain source code information (locations, comments) provided by protoc in the descriptors. Use the `location` accessor to get that information from a descriptor.
