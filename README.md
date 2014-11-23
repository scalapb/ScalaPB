ScalaPB
=======

[![Build Status](https://travis-ci.org/trueaccord/ScalaPB.svg?branch=master)](https://travis-ci.org/trueaccord/ScalaPB)

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will
generate Scala case classes, parsers and serializers for your protocol
buffers.

The biggest difference between ScalaPB and ScalaBuff is that ScalaPB uses
`protoc` to parse the source `.proto` files instead of rolling its own parser.
By using the parser implementation provided by Google, ScalaPB achieves
perfect compatibility with the language specification and can handle any valid
proto file.

ScalaPB generates case classes that can co-exist in the same project alongside
the Java-generated code for ProtocolBuffer. This makes it easy to gradually
migrate an existing project from the Java version of protocol buffers to
Scala. This is acheived by having the ScalaPB generated code use an outer
class with the `PB` suffix appended to its name.

Another cool feature of ScalaPB is that it can optionally generate methods
that convert a Java protocol buffer to a Scala protocol buffer and vice versa.
This is useful if you are gradually migrating a large code base from Java
protocol buffers to Scala.  The optional Java conversion is required if you
want to use `fromAscii` (parsing ASCII representation of a protocol buffer).
The current implementation delegates to the Java version.

Installing
==========

To automatically generate Scala case classes for your messages add ScalaPB's
sbt plugin to your project. Create a file named `project/scalapb.sbt`
containing the following line:

    addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.3.2")

Add the following line to your `build.sbt`:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

    PB.protobufSettings

For additional configuration options, see [sbt-scalapb](https://github.com/trueaccord/sbt-scalapb) documentation

Using ScalaPB
=============

Documentation is available at [ScalaPB website](http://trueaccord.github.io/ScalaPB/).

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

