---
sidebar_label: "Dotty"
title: "Using with Dotty"
---

## Installing in SBT

ScalaPB 0.11.x fully supports Dotty. There is no need to use `withDottyCompat`
to get the dependencies correctly added. 

To use ScalaPB with dotty, ensure you use a recent sbt-protoc. 

In `project/plugins.sbt`:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "@sbt_protoc@")

addSbtPlugin("ch.epfl.lamp" % "sbt-dotty" % "0.4.6")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.0-M4"
```

In build.sbt:

```scala
scalaVersion := "3.0.0-RC1"

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
)
```

## Generated code

The generated code is designed to compile cleanly on all support versions of
the Scala compiler with the default compiler settings. It is known that currently
the generator will provide an error if `-language:strictEquality` is set.
