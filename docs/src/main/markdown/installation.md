---
sidebar_label: "Installation"
title: "Installing ScalaPB"
---

## Installing in SBT (Recommended!)

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt` containing the following lines:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "@sbt_protoc@")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "@scalapb@"
```

Add the following settings to your `build.sbt`:

```scala
Compile / PB.targets := Seq(
  scalapb.gen(
    scala3Sources = scalaBinaryVersion.value == "3"
  ) -> (Compile / sourceManaged).value / "scalapb"
)

// (optional) If you need scalapb/scalapb.proto or anything from
// google/protobuf/*.proto
libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
)
```


The `scala3Sources` setting selects Scala 3 syntax when the project uses Scala 3,
and Scala 2 syntax otherwise, including when cross-building. It ensures that
generated wildcard types use `?` on Scala 3, which is required with underscore
kind-projector compiler settings such as those enabled by sbt-typelevel.

ScalaPB will look for protocol buffer (`.proto`) files under `src/main/protobuf`, which can be customized. Running the `compile` command in sbt will generate Scala
sources for your protos and compile them.

For additional configuration options, see
[ScalaPB SBT Settings](sbt-settings.md).

## Running Standalone Using scalapbc

If you would like to compile protocol buffers into Scala outside SBT, you can
use scalapbc (ScalaPB compiler).

See [ScalaPBC](scalapbc.md).

## Running from Maven

Use `protoc-jar-maven-plugin` with the `protoc-gen-scala` plugin artifact to generate
Scala code in your Maven build.
Check out the [ScalaPB Maven example](https://github.com/thesamet/scalapb-maven-example).

## Next:

Read about the [Generated Code](generated-code.md).
