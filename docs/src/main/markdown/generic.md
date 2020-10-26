---
title: "Writing generic code"
sidebar_label: "Generic programming"
layout: docs
---

In some cases you will want to write generic code that can handle arbitrary
message types. This guide will show you various techniques that you can use
to accomplish that.

## Generated types

For each message in your proto files, ScalaPB will generate a case class which
extends the `GeneratedMessage[MessageType]` and a companion object:

```scala
case class MyMessage(...) extends scalapb.GeneratedMessage

object MyMessage extends scalapb.generatedMessageCompanion[MyMessage]
```

Generally speaking, the case class would have methods that act on a given
instance. Here are the important ones:

```scala
trait GeneratedMessage {
  // Serializes this message to the given output stream.
  def writeTo(output: OutputStream): Unit
  
  // Serializes this message into an array of bytes
  def toByteArray: Array[Byte] = {

  // Returns the companion object of this message.
  def companion: GeneratedMessageCompanion[_]

  // Returns a proto string representation of this message.
  def toProtoString: String
}
```

The companion, in turn, provides methods to create new instances of the type,
as well as retrieving the descriptors of the messages. The descriptors can be
used to query structural information about the proto files at runtime. Note
that the `GeneratedMessageCompanion` takes a type parameter `A` which
represents the message case class (must be a subtype of `GeneratedMessage`):

```scala
trait GeneratedMessageCompanion[A <: GeneratedMessage] {
  // Parses a message of type A from a byte array.
  def parseFrom(s: Array[Byte]): A

  // Returns the Java descriptors
  def javaDescriptor: com.google.protobuf.Descriptors.Descriptor

  // Returns the Scala descriptors
  def scalaDescriptor: scalapb.descriptors.Descriptor

  // Returns the companion for a field. The field must be a message field.
  def messageCompanionForFieldNumber(field: Int): GeneratedMessageCompanion[_]
}
```

## Writing a function that accepts any message type

The following is an example of a function that takes an instance of a message
and writes it to a file.

```scala mdoc
import java.nio.file.{Files, Paths}
import scalapb.GeneratedMessage

def writeToFile[A <: GeneratedMessage](msg: A, fileName: String): Unit = {
  Files.write(Paths.get(fileName), msg.toByteArray)
  ()
}
```

## Summoning the companion

In our next example, we will write a function that reads a file and parse it
into a message of type `A`. To build a new instance of a message, we need to
call `parseFrom` on the companion. Using Scala's implicit search, it is easy
to obtain the companion of any message type:

```scala mdoc
import java.nio.file.{Files, Paths}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}

def readFromFile[A <: GeneratedMessage](fileName: String)(
  implicit cmp: GeneratedMessageCompanion[A]): A = {
    val byteArray = Files.readAllBytes(Paths.get(fileName))
    cmp.parseFrom(byteArray)
}
```

When calling this function, you need to provide the specific type you want it
to return, and the filename. The Scala compiler will automatically find the
appropriate message companion to pass as `cmp` via implicit search:

```scala
loadFromFile[Person]("/tmp/person.pb")
```
