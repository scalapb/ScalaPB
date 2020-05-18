---
title: "About"
layout: docs
---

# ScalaPB: Scala Protocol Buffer Compiler

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will
generate Scala case classes, parsers and serializers for your protocol
buffers.

ScalaPB is hosted on [Github](https://github.com/scalapb/ScalaPB).

## Main features

* Built on top of Google's protocol buffer compiler to ensure perfect
  compatibility with the language specification.

* Supports both proto2 and proto3.

* Nested updates are easy by using lenses:
```scala
val newOrder = order.update(_.creditCard.expirationYear := 2015)
```

* Generated case classes can co-exist alongside the Java-generated code (the
  class names will not clash). This allows gradual transition from Java to
  Scala.

* Can optionally generate conversion methods between the Java generated
  version of Protocol Buffers to the Scala generated version. This makes
  it possible to migrate your project gradually.

* **New:** Supports for
  [Oneof](https://developers.google.com/protocol-buffers/docs/proto#oneof)'s 
  that were introduced in Protocol Buffers 2.6.0.

* **Newer:** Supports [Scala.js]({{site.baseurl}}/scala.js.html) (in 0.5.x).

* **Newer** Supports [gRPC](http://www.grpc.io/) (in 0.5.x).

* **Newest:** Supports [SparkSQL]({{site.baseurl}}/sparksql.html) (in 0.5.23).

* **Newest:** Supports [converting to and from JSON]({{site.baseurl}}/json.html) (in 0.5.x).

* **Newest:** Supports [User-defined options]({{site.baseurl}}/user_defined_options.html) (in 0.5.29).

## Installing in SBT (Recommended!)

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.thesamet" % "sbt-protoc" % "{{site.data.version.sbt_protoc}}")

    libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"

Add the following line to your `build.sbt`:

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    )

    // (optional) If you need scalapb/scalapb.proto or anything from
    // google/protobuf/*.proto
    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"


ScalaPB looks for protocol buffer files in `src/main/protobuf`, but this can
be customized. Running the `compile` command in sbt will both generate Scala
sources from your protos and compile them. 

For additional configuration options, see
[ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

## Running Standalone Using scalapbc

If you would like to compile protocol buffers into Scala outside SBT, you can
use scalapbc (ScalaPB compiler).

See [ScalaPBC]({{site.baseurl}}/scalapbc.html).

## Running from Maven

Using ScalaPBC, you can get maven to generate the code for you.
Check out the [ScalaPB Maven example](https://github.com/thesamet/scalapb-maven-example).

## Questions? Comments?

Feel free to post to our [mailing
list](https://groups.google.com/forum/#!forum/scalapb).  Found a bug? Missing
a feature? [Report on Github](https://github.com/scalapb/ScalaPB/issues).

## Next:

Read about the [Generated Code]({{site.baseurl}}/generated-code.html).

