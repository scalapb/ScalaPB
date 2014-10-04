ScalaPB
=======

[![Build Status](https://travis-ci.org/trueaccord/ScalaPB.svg?branch=master)](https://travis-ci.org/trueaccord/ScalaPB)

ScalaPB is a protocol buffer compiler (`protoc`) plugin for Scala. It will generate Scala case classes,
parsers and serializers for your protocol buffers.

The biggest difference between ScalaPB and ScalaBuff is that ScalaPB uses `protoc` to parse the source
`.proto` files instead of rolling its own parser. By using the parser implementation provided by Google,
ScalaPB achieves perfect compatibility with the language specification and can handle any valid proto
file.

ScalaPB generates case classes that can co-exist in the same project alongside the Java-generated code
for ProtocolBuffer. This makes it easy to gradually migrate an existing project from the Java version
of protocol buffers to Scala. This is acheived by having the ScalaPB generated code use an outer class
with the `Scala` suffix appended to its name.

The current implementation of ScalaPB delegates parsing and serializing to the Java implementation.
This is an "implementation detail" that will change shortly.

Testing
=======

ScalaPB uses ScalaCheck to aggressively test the generated code. The test generates hundred different sets of
proto files. The sets are growing in complexity: number of files, references to messages from other protos,
message nesting and so on. Then, test data is generated to populate this protocol schema, then we check
that both implementations generate the same serialized result from this data.

Running the tests:

    $ sbt test
