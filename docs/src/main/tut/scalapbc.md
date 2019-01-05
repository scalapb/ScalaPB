---
title: "ScalaPBC"
layout: docs
---

# ScalaPBC: ScalaPB standalone compiler

ScalaPBC is a tool that lets you generate Scala sources from the command line (or from a maven build).

## Installation

You can download the current release here: [scalapbc-{{site.data.version.scalapb}}.zip](https://github.com/scalapb/ScalaPB/releases/download/v{{site.data.version.scalapb}}/scalapbc-{{site.data.version.scalapb}}.zip).

Older versions can be found in the [releases page](https://github.com/scalapb/ScalaPB/releases).

Unzip the file, and inside you will find two scripts: `bin/scalapbc` (and
`bin/scalapbc.bat`) that can be used on Unix/Linux/Mac OS X (and Windows,
respectively).

## Usage

ScalaPBC is used exactly like protoc. In fact, scalapbc calls protoc.
It ships with [multiple versions](https://github.com/os72/protoc-jar) of protoc embedded in the
jar and you can pick the one you want to use by passing the desired version as the
first command line argument:

```bash
./bin/scalapbc -v351 [options]
```

```bash
./bin/scalapbc -v261 [options]
```

To generate Scala code, invoke ScalaPBC like this:

```bash
./bin/scalapbc -v351 --scala_out=some/output/directory myproto.proto
```

To generate both Scala code and Java code along with Java conversions:

```bash
./bin/scalapbc -v351 \
    --scala_out=java_conversions:some/output/directory \
    --java_out=some/output/directory \
    myproto.proto
```

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

The supported parameters are: `flat_package`, `java_conversions`, `grpc` and
`single_line_to_proto_string`, `no_lenses`, `retain_source_code_info`. 

Those parameters are described in [SBT settings]({{site.baseurl}}/sbt-settings.html)
