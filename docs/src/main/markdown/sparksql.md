---
title: "SparkSQL"
layout: docs
---

# ScalaPB with SparkSQL

## Setting up your project

Make sure that you are using ScalaPB 0.5.23 or later.

We are going to use sbt-assembly to deploy a fat JAR containing ScalaPB, and
your compiled protos.  Make sure in project/plugins.sbt you have a line
that adds sbt-assembly:

```scala
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
```

In `build.sbt` add a dependency on `sparksql-scalapb`:

```scala
libraryDepenencies += "com.thesamet.scalapb" %% "sparksql-scalapb" % "{{site.data.version.sparksql_scalapb}}"
```

The running container contains an old version of Google's Protocol Buffers
runtime that is not compatible with the current version. Therefore, we need to
shade our copy of the Protocol Buffer runtime. Add this to your build.sbt:

```scala
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll
)
```

See [complete example of build.sbt](https://github.com/thesamet/sparksql-scalapb-test/blob/master/build.sbt).

## Running SQL queries on protos

Assuming you have an RDD of ScalaPB protos:

```scala
val persons: RDD[Person] = ...
```

You can convert it to a dataframe and register it as SparkSQL table:

```scala
import scalapb.spark._
sqlContext.protoToDataFrame(persons).registerTempTable("persons")
```

The first import line adds an implicit conversion for SQLContext that supplies
`protoToDF`. An equivalent alternative you can use is:

```scala
import scalapb.spark._
persons.toDataFrame(sqlContext).registerTempTable("persons")
```

Now you can run code like this:

```scala
val query = sqlContext.sql(
  "SELECT name, age, size(addresses) FROM persons WHERE age > 30")

query
  .collect
  .foreach(println)
```

## Datasets and `<none> is not a term`

It is also possible to use Datasets of protos. With Datasets, Spark needs to
be able to represent each field in your message as some Spark SQL type. This
works well for standard types, but is causing a problem with enums (since they
are represented in ScalaPB as a sealed trait extended by case objects).

The solution to this problem is to include an additional generator that would
generate mappings between the ScalaPB generated types to SparkSQL primitives.

Add the following to `project/plugins.sbt`:

    libraryDependencies += "com.thesamet.scalapb" %% "sparksql-scalapb-gen" % "{{site.data.version.sparksql_scalapb}}"

Then in `build.sbt`, call the UDT generator:

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value,
      scalapb.UdtGenerator -> (sourceManaged in Compile).value
    )

In `sbt` run `clean` and `compile`. This would generate, for each proto file
`file.proto` a scala file named `FileUdt.scala` containing an object with a
`register()` function. Add a call to this function early enough in your program, before attempting
to create a dataset of protos. Here is an [example of such a call](https://github.com/thesamet/sparksql-scalapb-test/blob/7653eb062f6691082c1512b94db32c13eb381138/src/main/scala/RunDemo.scala#L15).

## Example

Check out a [complete example](https://github.com/thesamet/sparksql-scalapb-test) here.
