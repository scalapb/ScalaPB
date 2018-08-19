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
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.2")
```

In `build.sbt` add a dependency on `sparksql-scalapb`:

```scala
libraryDepenencies += "com.thesamet.scalapb" %% "sparksql-scalapb" % "0.7.0"
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

Check out a [complete example](https://github.com/thesamet/sparksql-scalapb-test) here.
