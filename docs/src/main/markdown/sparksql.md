---
title: "SparkSQL"
layout: docs
---

# ScalaPB with SparkSQL

## Introduction

By default, Spark uses reflection to derive schemas and encoders from case
classes. This doesn't work well when there are messages that contain types that
Spark does not understand such as enums, `ByteString`s and `oneof`s. To get around this, sparksql-scalapb provides its own `Encoder`s for protocol buffers.

However, it turns out there is another obstacle. Spark does not provide any mechanism to compose user-provided encoders with its own reflection-derived Encoders. Therefore, merely providing an `Encoder` for protocol buffers is insufficient to derive an encoder for regular case-classes that contain a protobuf as a field. To solve this problem, ScalaPB uses [frameless](https://github.com/typelevel/frameless) which relies on implicit search to derive encoders. This approach enables combining ScalaPB's encoders with frameless encoders that takes care for all non-protobuf types.

## Setting up your project

Make sure that you are using ScalaPB 0.9.0 or later.

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

Spark ships with an old version of Google's Protocol Buffers runtime that is not compatible with
the current version. Therefore, we need to shade our copy of the Protocol Buffer runtime.
Add this to your build.sbt:

```scala
assemblyShadeRules in assembly := Seq(
  ShadeRule.rename("com.google.protobuf.**" -> "shadeproto.@1").inAll
)
```

See [complete example of build.sbt](https://github.com/thesamet/sparksql-scalapb-test/blob/master/build.sbt).

## Using sparksql-scalapb

We assume you have a `SparkSession` assigned to the variable `spark`. In a standalone Scala program, this can be created with:

```scala mdoc
import org.apache.spark.sql.SparkSession

val spark: SparkSession = SparkSession
  .builder()
  .appName("ScalaPB Demo")
  .master("local[2]")
  .getOrCreate()
```

*IMPORTANT*: Ensure you do not import `spark.implicits._` to avoid ambiguity between ScalaPB provided encoders and Spark's default encoders. You may want to import `StringToColumn` to convert `$"col name"` into a `Column`. Add an import `scaslapb.spark.Implicits` to add ScalaPB's encoders for protocol buffers into the implicit search scope:

```scala mdoc
import org.apache.spark.sql.{Dataset, DataFrame, functions => F}
import spark.implicits.StringToColumn
import scalapb.spark.ProtoSQL

import scalapb.spark.Implicits._
```

The code snippets below use the [`Person` message](https://github.com/scalapb/ScalaPB/blob/master/docs/src/main/protobuf/person.proto).

We start by creating some test data:
```scala mdoc:silent
import scalapb.docs.person.Person
import scalapb.docs.person.Person.{Address, AddressType}

val testData = Seq(
   Person(name="John", age=32, addresses=Vector(
     Address(addressType=AddressType.HOME, street="Market", city="SF"))
   ),
   Person(name="Mike", age=29, addresses=Vector(
     Address(addressType=AddressType.WORK, street="Castro", city="MV"),
     Address(addressType=AddressType.HOME, street="Church", city="MV"))
   ),
   Person(name="Bart", age=27)
)
```

We can create a `DataFrame` from the test data:
```scala mdoc
val df = ProtoSQL.createDataFrame(spark, testData)
df.printSchema()
df.show()
```

and then process it as any other Dataframe in Spark:

```scala mdoc
df.select($"name", F.size($"addresses").alias("address_count")).show()

val nameAndAddress = df.select($"name", $"addresses".getItem(0).alias("firstAddress"))

nameAndAddress.show()
```

Using the datasets API it is possible to bring the data back to ScalaPB case classes:
```scala mdoc

nameAndAddress.as[(String, Option[Address])].collect().foreach(println)
```

You can create a Dataset directly using Spark APIs:
```scala mdoc
spark.createDataset(testData)
```

## From Binary to protos and back

In some situations, you may need to deal with datasets that contain serialized protocol buffers. This can be handled by mapping the datasets through ScalaPB's `parseFrom` and `toByteArray` functions.

Let's start by preparing a dataset with test binary data by mapping our `testData`:

```scala mdoc
val binaryDS: Dataset[Array[Byte]] = spark.createDataset(testData.map(_.toByteArray))

binaryDS.show()
```

To turn this dataset into a `Dataset[Person]`, we map it through `parseFrom`:

```scala mdoc
val protosDS: Dataset[Person] = binaryDS.map(Person.parseFrom(_))
```

to turn a dataset of protos into `Dataset[Array[Byte]]`:
```scala mdoc
val protosBinary: Dataset[Array[Byte]] = protosDS.map(_.toByteArray)
```

## On enums

In SparkSQL-ScalaPB, enums are represented as strings. Unrecognized enum values are represented as strings containing the numeric value.

## Dataframes and Datasets from RDDs

```scala
import org.apache.spark.rdd.RDD

val protoRDD: RDD[Person] = spark.sparkContext.parallelize(testData)

val protoDF: DataFrame = ProtoSQL.protoToDataFrame(spark, protoRDD)

val protoDS: Dataset[Person] = spark.createDataset(protoRDD)
```

## Datasets and `<none> is not a term`

You will see this error if for some reason Spark's `Encoder`s are being picked up
instead of the ones provided by sparksql-scalapb. Please ensure you are not importing `spark.implicits._`. See instructions above for imports.

## Example

Check out a [complete example](https://github.com/thesamet/sparksql-scalapb-test) here.
