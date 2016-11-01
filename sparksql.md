---
title: "ScalaPB: Using with SparkSQL"
layout: page
---

# ScalaPB with SparkSQL

## Setting up your project

Make sure that you are using ScalaPB 0.5.23 or later.

We are going to use sbt-assembly to deploy a fat JAR containing ScalaPB, and
your compiled protos.  Make sure in project/plugins.sbt you have a line
that adds sbt-assembly:

{%highlight scala%}
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.2")
{%endhighlight%}

In `build.sbt` add a dependency on `sparksql-scalapb`:

{%highlight scala%}
libraryDepenencies += "com.trueaccord.scalapb" %% "sparksql-scalapb" % "0.1.3"
{%endhighlight%}

The running container contains an old version of Google's Protocol Buffers
runtime that is not compatible with the current version. Therefore, we need to
shade our copy of the Protocol Buffer runtime. Add this to your build.sbt:

{%highlight scala%}
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll
)
{%endhighlight%}

See [complete example of build.sbt](https://github.com/thesamet/sparksql-scalapb-test/blob/master/build.sbt).

## Running SQL queries on protos

Assuming you have an RDD of ScalaPB protos:

{%highlight scala%}
val persons: RDD[Person] = ...
{%endhighlight%}

You can convert it to a dataframe and register it as SparkSQL table:

{%highlight scala%}
import com.trueaccord.scalapb.spark._
sqlContext.protoToDF(persons).registerTempTable("persons")
{%endhighlight%}

The first import line adds an implicit conversion for SQLContext that supplies
`protoToDF`.

Now you can run code like this:

{%highlight scala%}
val query = sqlContext.sql(
  "SELECT name, age, size(addresses) FROM persons WHERE age > 30")

query
  .collect
  .foreach(println)
{%endhighlight%}

Check out a [complete example](https://github.com/thesamet/sparksql-scalapb-test) here.
