---
title: "JSON"
layout: docs
---

# ScalaPB and JSON

ScalaPB can convert protocol buffers to and from JSON, using
[json4s](http://json4s.org/).

## Setting up your project

Make sure that you are using ScalaPB 0.5.x or later.

In `build.sbt` add a dependency on `scalapb-json4s`:

```scala
// For ScalaPB 0.10.x:
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.10.1-M1"

// For ScalaPB 0.9.x:
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.9.3"

// For ScalaPB 0.8.x:
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.2"

// For ScalaPB 0.7.x:
libraryDependencies += "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.2"

// For ScalaPB 0.6.x (note the different groupId):
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.3.2"

// For ScalaPB 0.5.x (note the different groupId):
libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-json4s" % "0.1.6"
```

In your code, you can now convert to JSON:

```scala
import scalapb.json4s.JsonFormat

val r: String = JsonFormat.toJsonString(myProto)
```

Parse JSON back to a protocol buffer:

```scala
import scalapb.json4s.JsonFormat

val proto: MyProto = JsonFormat.fromJsonString[MyProto](
    """{"x": "17"}""")
```

There are lower-level functions `toJson()` and `fromJson()` that convert from
protos to json4s's `JValue`:

```scala
def toJson(m: GeneratedMessage): JObject

def fromJson[Proto](value: JValue): Proto
```

Finally, in JsonFormat there are two implicit methods that instantiate
`Reader[Proto]` and `Writer[Proto]`.

## More printing and parsing options

There are a few more options available to customize the format used to print
and parse JSON. To take advantage of that, instantiate `Printer` and `Parser` and
call `toJson()` / `fromJson()` as usual.

For example:

```scala
new scalapb.json4s.Printer(
  includingDefaultValueFields = true,
  preservingProtoFieldNames = true,
  formattingLongAsNumber = true
).toJson(myProto)
```

Options:

- `includingDefaultValueFields` (default: `false`): should fields
  that are set to their default value be included in the output.
- `preservingProtoFileNames` (default: `false`): by default, field names are mapped to 
  lowerCamelCase and become JSON object keys. Setting this option to `true` would
  make the parser and the printer use the original field names as specified in the proto
  file (normally, in snake_case)
- `formatLongAsNumber` (default: `false`): by default, longs are serialized as
  strings. To use the numeric representation, set this option to true. Note that
  due to the way Javascript represents numbers, there is a possibility to lose
  precision ([more details here](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Number/isSafeInteger)).

See the list of [constructor paramerters here](https://github.com/scalapb/scalapb-json4s/blob/master/src/main/scala/scalapb/json4s/JsonFormat.scala)

# Printing and parsing Anys

In Protocol Buffers, `google.protobuf.Any` is a type that embeds an arbitrary protobuf message. An `Any` is represented as a message that contains a `typeUrl` field that identifies the type, and a bytes field `value` which contains the serialized contents of a message. In JSON, the message embedded in the `Any` is serialized as usual, and there is a `@type` key added to it to identify which message it is. The parser expects this `@type` key to know which message it is. To accomplish this, all the expected embedded types need to be registered with a `TypeRegistry` so the printer and parser know how to process the embedded message.

The following example is based [on this proto](https://github.com/scalapb/ScalaPB/blob/master/docs/src/main/protobuf/json.proto).

```scala mdoc
import com.thesamet.docs.json._
import scalapb.json4s.{Printer, Parser, TypeRegistry}

val c = MyContainer(
  myAny=Some(
    com.google.protobuf.any.Any.pack(
      MyMessage(x=17)
    )
  )
)

val typeRegistry = TypeRegistry().addMessage[MyMessage]

val printer = new Printer().withTypeRegistry(typeRegistry)

printer.print(c)
```

Conversely, you can start from a JSON and parse it back to a `MyContainer` that contains an `Any` field:

```scala mdoc
val parser = new Parser().withTypeRegistry(typeRegistry)

parser.fromJsonString[MyContainer]("""
  {
    "myAny": {
      "@type": "type.googleapis.com/com.thesamet.docs.MyMessage",
      "x": 17
    }
  }""")
```
