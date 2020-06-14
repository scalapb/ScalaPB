ScalaPB
=======

[![Join the chat at https://gitter.im/ScalaPB/community](https://badges.gitter.im/ScalaPB/community)](https://gitter.im/ScalaPB/community)

![Build Status](https://github.com/scalapb/ScalaPB/workflows/CI/badge.svg)

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will
generate Scala case classes, parsers and serializers for your protocol
buffers.

ScalaPB generates case classes that can co-exist in the same project alongside
the Java-generated code for ProtocolBuffer. This makes it easy to gradually
migrate an existing project from the Java version of protocol buffers to
Scala. This is achieved by having the ScalaPB generated code use the proto
file as part of the package name (in contrast to Java which uses the file name
in CamelCase as an outer class)

Each top-level message and enum is written to a separate Scala file. This
results in a significant improvement in incremental compilations.

Another cool feature of ScalaPB is that it can optionally generate methods
that convert a Java protocol buffer to a Scala protocol buffer and vice versa.
This is useful if you are gradually migrating a large code base from Java
protocol buffers to Scala.  The optional Java conversion is required if you
want to use `fromAscii` (parsing ASCII representation of a protocol buffer).
The current implementation delegates to the Java version.

Highlights
==========

- Supports proto2 and proto3

- Easily update nested structure in functional way using lenses

- Scala.js integration

- GRPC integration

- Compatible with SparkSQL (through a helper library)

- Conversion to and from JSON

- Support user-defined options (since 0.5.29)

- Support extensions (since 0.6.0)

Versions
========

Version | Description
------- | -----------
0.6.x   | Stable. Supports Protobuf 2.6.x to 3.5.x
0.5.x   | Supports Protobuf 2.6.x and Protobuf 3.1.x.
0.4.x   | Stable, unsupported. Works with Protobuf 2.6.x


Installing
==========

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/protoc.sbt`
containing the following line:

    addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.28")

    libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.10.0"

Add the following line to your `build.sbt`:

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value / "scalapb"
    )

For additional configuration options, see [ScalaPB SBT Settings](https://scalapb.github.io/sbt-settings.html) documentation

Using ScalaPB
=============

Documentation is available at [ScalaPB website](https://scalapb.github.io/).

Questions?
==========

See [contacting us](https://scalapb.github.io/contact.html).


Testing
=======

ScalaPB uses ScalaCheck to aggressively test the generated code. The test
generates many different sets of proto files. The sets are growing in
complexity: number of files, references to messages from other protos, message
nesting and so on. Then, test data is generated to populate this protocol
schema, then we check that the ScalaPB generated code behaves exactly like
the reference implementation in Java.

Running the tests:

    $ sbt test

The tests take a few minutes to run. There is a smaller test suite called
`e2e` that uses the sbt plugin to compile the protos and runs a series of
ScalaChecks on the outputs. To run it:

    $ ./e2e.sh

Sponsors
========
We are very thankful to our sponsors: 

* [Oliver Wickham (ollyw)](https://github.com/ollyw)
* [Evgeny Rubtsov (bikaras)](https://github.com/bikaras)
