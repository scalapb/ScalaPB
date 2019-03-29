Contributing to ScalaPB
=======================

Thank you for your interest in contributing to ScalaPB!

Before spending considerable amount of work on a new feature or bug fix, please reach out
through our mailing list, Gitter channel or Github to discuss the problem you
want to solve and how you want to solve it. Please indicate if you are able to
spend time working on a PR - we will be happy to give guidance.

Project Structure
=================

* `compiler-plugin`: this is where the code generator (transforms protos into Scala
    source code) lives.
* `scalapb-runtime`: this is a Scala library that the generated code depends
  on. It provides the base classes and traits that the generated code extends
  as well as base common function it calls. This library is cross-built for
  JVM, Scala.js and Native.
* `scalapb-runtime-grpc`: this is the Scala library that generated gRPC
  services depend on.
* `lenses`: a little lenses library that the generated code depends on.
* `proptest`: a test suite that generates random protocol buffers, fake date
  for them and tries to get it all to compile and pass property-based testing.
* `e2e`: a test suite that exercises all the functionality of ScalaPB end-to-end.
* `protobuf/scalapb/scalapb.proto`: the place where ScalaPB options are
  defined.
* `scalapbc`: a CLI and a library that can used invoke ScalaPB without SBT.

Running the e2e (end-to-end) tests
==================================

We assume that you have a JVM and SBT installed.

The end-to-end tests contain main test protobufs under `e2e/src/main/protobuf` and runtime
tests at `e2e/src/test/scala` that exercise the generated code.

To run the tests, use the e2e.sh script

```
$ ./e2e.sh
```

This script compiles the compiler and the runtime and publishes them locally,
so the end-to-end test can use your development version of those libraries.

You can run `e2e.sh` for a specific version of Scala by providing
`SCALA_VERSION` as an environment variable:

```
SCALA_VERSION=2.13.0-M5 ./e2e.sh
```

`e2e` is a normal SBT project, so if you already have a locally published
`compiler-plugin` and `runtime`, you can invoke `sbt` inside the `e2e`
directory and run `test`. You need to remember that changes that you make to
the runtime and compiler-plugin won't take effect until you run `e2e.sh`
again.

Note that e2e has a subproject called `noJava` where the generated protos do
not have Java converters enabled. Some features require testing on this
subproject, but for most features this does not matter.

Modifying scalapb.proto
=======================

Some features would require adding a new option in `scalapb.proto`. After each
time that you edit `scalapb.proto`, you need to run `./make_plugin_protos.sh`.
This script generates code out of scalapb.proto for both the compiler plugin
and the runtime library. Currently, this generated code gets checked in, so it
will become part of your PR.

Accessors to the option should be defined in the appropriate section in
`DescriptorImplicits.scala` and typically used in `ProtobufGenerator.scala`

If there are any validation rules of when this option can be applied,
it should be validated in `ProtoValidation.scala` and tested in
`ProtoValidationSpec.scala`.

Finally, a test proto and spec should be added to `e2e`.

Wrapping up your PR
===================

* Run `mima.sh` to list all binary incompatibilities you may have introduced
  while working on this. Add the exclusions to `build.sbt`. Binary
  incompatibilities are expected when modifying `scalapb.proto` and in the
  meantime we have some tolerance for certain type of incompatabilities.
* In SBT, run `scalafmt` and `test:scalafmt` to ensure the code compiles
  cleanly.
* Run `./make_plugin_protos.sh` to re-generate all the generated code that
  ships with ScalaPB.

