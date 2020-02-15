---
title: "Benchmarks"
layout: docs
---

```scala mdoc:invisible
import scalapb.docs.benchmarks.ProtoSnippet.source
import scalapb.perf.protos._
import scalapb.docs.benchmarks.Charts.makeChartPair
```
<script src="https://cdn.plot.ly/plotly-1.41.3.min.js" type="text/javascript"></script>

# ScalaPB Benchmarks

The following benchmarks were produced on a workstation with an Intel(R) Core(TM) i7-4770 CPU running at 3.40GHz with 32GB of RAM using jmh.

The benchmarks are built around proto definitions and a single instances of that definition. The serialization benchmark is serializing a fixed instance using `toByteArray` (note that the serialized size is only computed once and cached in that instance (this is also true for Java). The parsing test calls `parseFrom` over the same array of bytes.

Each benchmark is executed against multiple versions of ScalaPB, Scala 2.12 and Scala 2.13. For comparison, it is ran against Java protobuf (currently 3.11.1).

In 0.10.x, ScalaPB started to preserve unknown fields, which means that every generated class has an additional member called `unknownFields` that represents unknown fields that represents unknown fields encountered when `parseFrom` - as this introduces overhead in parsing, serialization, allocation and so on, that feature is enabled in the following benchmarks for all Scala versions.

The charts show the average time per operation in nanoseconds - lower is better.

Like any benchmarks, they should be taken with a grain of salt, especially when used to compare Java and Scala. If you are here to make a decision on which library to pick solely by looking at performance data, consider that:

* The results are very sensitive to the JVM you use and other environment factors. You can change the "winner" of some of the benchmarks by changing the environment.
* Your specific protos and data may have different performance characteristics than the examples below.
* The overall conclusion is that the performance is similar (and fast!), give or take a few percentages. Make sure you are optimizing where the real bottlenecks are.

## SimpleMessage

A simple flat message:

```scala mdoc:passthrough
source[Color]
```

```scala mdoc:passthrough
source[SimpleMessage]
```

```scala mdoc:passthrough
makeChartPair("SimpleMessage")
```

## MessageContainer

```scala mdoc:passthrough
source[MessageContainer]
```

```scala mdoc:passthrough
makeChartPair("MessageContainer")
```

## Enum

```scala mdoc:passthrough
source[Enum]
```

Java stores the enums as a primitive int inside a message class. ScalaPB is a little slower in serializing since it needs to call `.value` on the enum to convert to int.

```scala mdoc:passthrough
makeChartPair("Enum")
```

## StringMessage

The Java implementation does a trick to save time when serializing the same instance multiple times. The reference to the string is replaced with a UTF8 ByteString representation of it, so the cost of encoding as UTF8 is paid only once. In normal usage, the same instance is unlikely to be serialized many times, so the difference should not matter to real-world applications.

```scala mdoc:passthrough
source[StringMessage]
```

```scala mdoc:passthrough
makeChartPair("StringMessage")
```

## IntVector

Here we parse and serialize a vector with hundreds of ints. The difference in performance is attributed partly to the performance difference of the underlying collection libraries.

```scala mdoc:passthrough
source[IntVector]
```

```scala mdoc:passthrough
makeChartPair("IntVector")
```

## EnumVector

```scala mdoc:passthrough
source[EnumVector]
```


```scala mdoc:passthrough
makeChartPair("EnumVector")
```

## Running the tests on your own

Inside the main ScalaPB repository, change into the benchmarks directory. To run a single benchmark, in SBT:

    jmh:run -t 1 -f 1 -i 5 -wi 5 -w 1 -r 1 "IntVectorTest.serializeScala"

To set the version of ScalaPB being used, set the ScalaPB environment variable:

    SCALAPB=0.10.0-SNAPSHOT sbt

To generate the entire set of measurements used to produce this page, use ammonite to run:

```
amm ./run_benchmarks.sc
```

To add an additional test scenario, register it in `projects/TestNames.scala` and add a test instance at `src/main/scala/TestCases.scala`.