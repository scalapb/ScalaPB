---
sidebar_label: "Dotty"
title: "Using with Dotty"
---

## Installing in SBT

ScalaPB 0.11.x fully supports Dotty. There is no need to use `withDottyCompat`
to get the runtime dependencies correctly added.

To use ScalaPB with dotty, ensure you use a recent sbt-protoc.

In `project/plugins.sbt`:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "@sbt_protoc@")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "@scalapb_latest@"
```

In build.sbt:

```scala
scalaVersion := "@scala3@"

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)
```

## Generated code

The generated code is designed to compile cleanly on all support versions of
the Scala compiler with the default compiler settings. It is known that currently
the generator will provide an error if `-language:strictEquality` is set.
