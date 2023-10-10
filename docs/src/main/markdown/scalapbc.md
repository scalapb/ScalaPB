---
title: "ScalaPBC: ScalaPB's standalone compiler"
sidebar_label: "ScalaPBC"
---

ScalaPBC is a tool that lets you generate Scala sources from the command line (or from a maven build).

## Installation

You can download the current release here: [scalapbc-@scalapb@.zip](https://github.com/scalapb/ScalaPB/releases/download/v@scalapb@/scalapbc-@scalapb@.zip).

Older versions can be found in the [releases page](https://github.com/scalapb/ScalaPB/releases).

Unzip the file, and inside you will find two scripts: `bin/scalapbc` (and
`bin/scalapbc.bat`) that can be used on Unix/Linux/Mac OS X (and Windows,
respectively).

## Usage

ScalaPBC is used exactly like protoc. In fact, scalapbc calls protoc.
On the first invocation, the protoc binary will be automatically
fetched via Coursier and stored for further invocations in a
[local cache](https://github.com/dirs-dev/directories-jvm#basedirectories),
which can be customized by setting the `PROTOC_CACHE` environment variable.
You can pass a custom protoc version as the first command line argument:

```bash
./bin/scalapbc -v3.11.1 [options]
```

To generate Scala code, invoke ScalaPBC like this:

```bash
./bin/scalapbc -v3.5.1 --scala_out=some/output/directory myproto.proto
```

To generate both Scala code and Java code along with Java conversions:

```bash
./bin/scalapbc -v3.5.1 \
    --scala_out=java_conversions:some/output/directory \
    --java_out=some/output/directory \
    myproto.proto
```

> The generated code depends on `scalapb-runtime` to compile. You will To get the code to work, add a dependency on [https://mvnrepository.com/artifact/com.thesamet.scalapb/scalapb-runtime] to your project. The version of `scalapb-runtime` needs to match or be newer than the version of the plugin.

## Passing generator parameters

If you would like to pass additional options, like `java_conversions`,
`flat_package`, or `single_line_to_proto_string`, it can be done like this:

```bash
bin/scalapbc my.proto --scala_out=OPT1,OPT2:path/to/output/dir/
```

where OPT1,OPT2 is a comma-separated list of options, followed by a colon
(`:`) and then the output directory. For example:

```bash
bin/scalapbc my.proto --scala_out=flat_package,java_conversions:protos/src/scala/main/
```

The supported parameters are: `flat_package`, `java_conversions`, `grpc` and `single_line_to_proto_string`, `no_lenses`, `no_getters`, `retain_source_code_info`.

Those parameters are described in [SBT settings](sbt-settings.md#additional-options-to-the-generator)

## Loading additional generators from Maven

ScalaPBC (starting version 0.10.1) can fetch generators from Maven using
Coursier:

    bin/scalapbc --plugin-artifact=io.grpc:protoc-gen-grpc-java:1.27.2:default,classifier=linux-x86_64,ext=exe,type=jar -- e2e/src/main/protobuf/service.proto --grpc-java_out=/tmp/out -Ie2e/src/main/protobuf -Ithird_party -Iprotobuf

If you use zio-grpc, you can use the following command to generate services
that use ZIO. This also generates ScalaPB case classes for messages and the
GRPC descriptors that the generated ZIO code depends on.

    bin/scalapbc --plugin-artifact=com.thesamet.scalapb.zio-grpc:protoc-gen-zio:0.1.0:default,classifier=unix,ext=sh,type=jar -- e2e/src/main/protobuf/service.proto --zio_out=/tmp/out --scala_out=grpc:/tmp/out -Ie2e/src/main/protobuf -Ithird_party -Iprotobuf

bin/scalapbc --plugin-artifact=io.grpc:grpc-java:
## Using ScalaPB as a proper protoc plugin

You may want to use ScalaPB code generator as a standard protoc plugin (rather
than using scalapbc as a wrapper or through SBT).

For Linux and Mac OS X, you can download a native executable version of the plugin for Scala from our [release page](https://github.com/scalapb/ScalaPB/releases):

* For Linux: [protoc-gen-scala-@scalapb@-linux-x86_64.zip](https://github.com/scalapb/ScalaPB/releases/download/v@scalapb@/protoc-gen-scala-@scalapb@-linux-x86_64.zip)
* For OS X: [protoc-gen-scala-@scalapb@-osx-x86_64.zip](https://github.com/scalapb/ScalaPB/releases/download/v@scalapb@/protoc-gen-scala-@scalapb@-osx-x86_64.zip)

Those zip files contain native executables of the plugin for the respective operating system built using GraalVM. If you are using another operating system, or prefer to use a JVM based plugin implementation, you will find executable scripts for Windows and Unix-like operating systems [in maven](https://repo1.maven.org/maven2/com/thesamet/scalapb/protoc-gen-scala/@scalapb@/). These scripts require a JVM to run. The JVM needs to be available on the path, or through the `JAVA_HOME` environment variable.

To generate code:

    protoc my.protos --plugin=/path/to/bin/protoc-gen-scala-@scalapb@-unix.sh --scala_out=scala

On Windows:

    protoc my.protos --plugin=protoc-gen-scala=/path/to/bin/protoc-gen-scala.bat --scala_out=scala

For passing parameters to the plugin, see the section above.

> Note that the standalone plugin provided in `scalapbc` needs to be able to find a JVM in the path or through `JAVA_HOME` environment variable. If you encounter unexpected errors, try to execute the plugin directly from the command line, and the output printed may be useful for further debugging.

> The generated code depends on `scalapb-runtime` to compile. To get the code to work, add a dependency on [scalapb-runtime](https://mvnrepository.com/artifact/com.thesamet.scalapb/scalapb-runtime) to your project. The version of `scalapb-runtime` needs to match or be newer than the version of the plugin.
