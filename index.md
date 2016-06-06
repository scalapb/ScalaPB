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

* Nested updates are easy by using lenses:
{%highlight scala%}
val newOrder = order.update(_.creditCard.expirationYear := 2015)
{%endhighlight%}

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

* **Newest:** Supports [Custom Options]({{site.baseurl}}/custom_options.html) (in 0.5.x).

## Installing in SBT (Recommended!)

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "{{site.data.version.sbt_scalapb}}")

Add the following line to your `build.sbt`:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

    PB.protobufSettings

ScalaPB looks for protocol buffer files in src/main/protobuf, but this can
be customized. Running the `compile` command in sbt will both generate Scala
sources from your protos and compile them. 

For additional configuration options, see
[ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

## Running Standalone Using spbc

If you would like to compile protocol buffers into Scala outside SBT, you can
use spbc (ScalaPB compiler).

The first step, is to [download the latest version](https://github.com/trueaccord/ScalaPB/releases/download/v{{site.data.version.scalapb}}/spbc-{{site.data.version.scalapb}}.zip)
from the [ScalaPB releases page on Github](https://github.com/trueaccord/ScalaPB/releases).

Unzip the file, and inside you will find two scripts: `bin/spbc` (and
`bin/spbc.bat`) that can be used on Unix/Linux/Mac OS X (and Windows,
respectively).

spbc is used exactly like protoc. In fact, spbc calls protoc, though it passes
to it a script that serves as the Scala plugin. Here is an example command
line to invoke it:

    bin/spbc my.proto --scala_out=.

If you would like to pass additional options, like java_conversions,
flat_package, or single_line_to_string, it can be done like this:

    bin/spbc my.proto --scala_out=OPT1,OPT2:path/to/output/dir/

where OPT1,OPT2 is a comma-separated list of options. For example:

    bin/spbc my.proto --scala_out=flat_packages,java_conversions:.

## Questions? Comments?

Feel free to post to our [mailing
list](https://groups.google.com/forum/#!forum/scalapb).  Found a bug? Missing
a feature? [Report on Github](https://github.com/trueaccord/ScalaPB/issues).

## Next:

Read about the [Generated Code]({{site.baseurl}}/generated-code.html).
