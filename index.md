---
title: "ScalaPB: Scala Protocol Buffer Compiler"
layout: page
---

# ScalaPB

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will
generate Scala case classes, parsers and serializers for your protocol
buffers.

ScalaPB is hosted on [Github](https://github.com/trueaccord/ScalaPB).

## Main features

* Built on top of Google's protocol buffer compiler to ensure perfect
  compatibility with the language specification.

* Generated case classes can co-exist alongside the Java-generated code (the
  class names will not clash). This allows gradual transition from Java to
  Scala.

* Can optionally generate conversion methods between the Java generated
  version of Protocol Buffers to the Scala generated version. This makes
  it possible to migrate your project gradually.

* **New:** Supports for
  [Oneof](https://developers.google.com/protocol-buffers/docs/proto#oneof)'s 
  that were introduced in Protocol Buffers 2.6.0.

## Installing

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.3.2")

Add the following line to your `build.sbt`:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

    PB.protobufSettings

For additional configuration options, see
[sbt-scalapb](https://github.com/trueaccord/sbt-scalapb) documentation

## Next:

Read about the [Generated Code](/generated-code.html).
