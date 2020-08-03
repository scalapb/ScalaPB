---
title: "Protocol Buffer Tutorial: Scala"
sidebar_label: "Tutorial"
layout: docs
---

This tutorial provides a basic Scala programmer's introduction to working with protocol buffers. By walking through creating a simple example application, it shows you how to

* Define message formats in a .proto file.

* Use SBT to generate Scala case classes from proto files.

* Use ScalaPB's API to write and read messages.

This isn't a comprehensive guide to using protocol buffers in Scala. For more detailed reference information, see the [Generated Code page](generated-code.md).


## Why Use Protocol Buffers?￼

The example we're going to use is a very simple "address book" application that can read and write people's contact details to and from a file. Each person in the address book has a name, an ID, an email address, and a contact phone number.

How do you serialize and retrieve structured data like this? There are a few ways to solve this problem:

* Use Java Serialization. This is the default approach since it's built into the language, but it has a host of well-known problems (see Effective Java, by Josh Bloch pp. 213), and also doesn't work very well if you need to share data with applications written in C++ or Python.
* You can invent an ad-hoc way to encode the data items into a single string – such as encoding 4 ints as "12:3:-23:67". This is a simple and flexible approach, although it does require writing one-off encoding and parsing code, and the parsing imposes a small run-time cost. This works best for encoding very simple data.
* Serialize the data to JSON (or XML). This approach can be very attractive since JSON and SML are (sort of) human readable and there are plenty of libraries for lots of languages. This can be a good choice if you want to share data with other applications/projects. However, JSON and XML are notoriously space intensive, and encoding/decoding it can impose a huge performance penalty on applications. Also, navigating JSON AST trees or XML DOM trees is considerably more complicated than navigating simple fields in a case class normally would be.

Protocol buffers are the flexible, efficient, automated solution to solve exactly this problem. With protocol buffers, you write a `.proto` description of the data structure you wish to store. From that, the protocol buffer compiler creates a case class that implements automatic encoding and parsing of the protocol buffer data with an efficient binary format. Importantly, the protocol buffer format supports the idea of extending the format over time in such a way that the code can still read data encoded with the old format.

## Where to Find the Example Code￼

The example code for this tutorial is under the `examples/basic` directory
in ScalaPB's repo. To get your copy:

```bash
git clone https://github.com/scalapb/ScalaPB.git
cd examples/basic
```

## Defining Your Protocol Format

By default, all proto files under `src/main/protobuf` will be compiled to Scala case classes. The following example `.proto` file resides in `src/main/protobuf/addressbook.proto`.

```protobuf
syntax = "proto2";

package tutorial;

message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;

  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  message PhoneNumber {
    required string number = 1;
    optional PhoneType type = 2 [default = HOME];
  }

  repeated PhoneNumber phones = 4;
}

message AddressBook {
  repeated Person people = 1;
}
```

Let's go through each part of the file and see what it does.

The `.proto` file starts with a package declaration, which helps to prevent naming conflicts between different projects. In Scala, the package name followed by the file name is used as the Scala package unless you have either explicitly specified a java_package, or specified a [scala package option](customizations.md).

Next, you have your message definitions. A message is just an aggregate containing a set of typed fields. Many standard simple data types are available as field types, including `bool`, `int32`, `float`, `double`, and `string`. You can also add further structure to your messages by using other message types as field types – in the above example the `Person` message contains `PhoneNumber` messages, while the `AddressBook` message contains `Person` messages. You can even define message types nested inside other messages – as you can see, the `PhoneNumber` type is defined inside `Person`. You can also define `enum` types if you want one of your fields to have one of a predefined list of values – here you want to specify that a phone number can be one of `MOBILE`, `HOME`, or `WORK`.

The `" = 1"`, `" = 2"` markers on each element identify the unique "tag" that field uses in the binary encoding. Tag numbers 1-15 require one less byte to encode than higher numbers, so as an optimization you can decide to use those tags for the commonly used or repeated elements, leaving tags 16 and higher for less-commonly used optional elements. Each element in a repeated field requires re-encoding the tag number, so repeated fields are particularly good candidates for this optimization.

Each field must be annotated with one of the following modifiers:

* **`required`**: a value for the field must be provided when constructing a message case class. Parsing a message that misses a required field will throw an `InvalidProtocolBufferException`. Other than this, a required field behaves exactly like an optional field.
* **`optional`**: the field may or may not be set. If an optional field value isn't set, a default value is used. For simple types, you can specify your own default value, as we've done for the phone number type in the example. Otherwise, a system default is used: zero for numeric types, the empty string for strings, false for bools. For embedded messages, the default value is always the "default instance" or "prototype" of the message, which has none of its fields set. Calling the accessor to get the value of an optional (or required) field which has not been explicitly set always returns that field's default value. In proto2, optional fields are represented as `Option[]`. In proto3, optional primitives are not wrapped in `Option[]`, but messages are.
* **`repeated`**: the field may be repeated any number of times (including zero). The order of the repeated values will be preserved in the protocol buffer. Think of repeated fields as dynamically sized arrays. They are represented in Scala as `Seq`s.

> **Required Is Forever**. You should be very careful about marking fields as required. If at some point you wish to stop writing or sending a required field, it will be problematic to change the field to an optional field – old readers will consider messages without this field to be incomplete and may reject or drop them unintentionally. You should consider writing application-specific custom validation routines for your buffers instead. Some engineers at Google have come to the conclusion that using required does more harm than good; they prefer to use only optional and repeated. However, this view is not universal.

You'll find a complete guide to writing `.proto` files – including all the possible field types – in the [Protocol Buffer Language Guide](https://developers.google.com/protocol-buffers/docs/proto). Don't go looking for facilities similar to class inheritance, though – protocol buffers don't do that.

## Compiling Your Protocol Buffers

Start `sbt` and type `compile` to compile the tutorial project.

Now, take a look at the files that were generated under
`target/scala-2.13/src_managed/main/scalapb/com/example/tutorial/addressbook`:

You will find `Person.scala`, with a case class that conceptually looks like this:

```scala
final case class Person(
    name: String,
    id: Int,
    email: Option[String] = None,
    phones: Seq[PhoneNumber] = Seq.empty,
    ...
```

The actual generated code will contain fully qualified class names (such as `_root_.scala.Predef.String`) to prevent name collissions between entities defined in your protocol buffer and other Scala code.

As you can see, each protobuf field becomes a member in the generated case class.

Repeated fields, by default, have a `Seq[T]` type. When they are parsed, the runtime type would be a `Vector`.

## Enums

The enum `PhoneType` is represented as a sealed abstract class, extended by a case object for each possible enum value:

```scala
sealed abstract class PhoneType(val value: Int)
    extends _root_.scalapb.GeneratedEnum {
  type EnumType = PhoneType
  def isMobile: Boolean = false
  def isHome: Boolean = false
  def isWork: Boolean = false
}

object PhoneType {
  case object MOBILE extends PhoneType(0) {
    val index = 0
    val name = "MOBILE"
    override def isMobile: Boolean = true
  }

  case object HOME extends PhoneType(1) {
    val index = 1
    val name = "HOME"
    override def isMobile: Boolean = true
  }

  // ...
}
```

Since Scala type equality is not type-safe (`a == b` will compile even when
`a` and `b` are of types that can never be equal), it is recommended to use
the various `isX` methods or pattern matching for comparison:

```scala
// Using isX:
val t = if (phoneType.isMobile) "Mobile" else "Not Mobile"

// Using pattern matching
phoneType match {
  case PhoneType.MOBILE => println("Mobile!")
  case _                => println("Not mobile!")
}
```

## Nested messages
Nested messages appear as case classes inside the companion object of
the containing message.

## Serialization

Each message case class extends a base trait called `GeneratedMessage` which provides
methods that help serialize a message:

* `def toByteArray: Array[Byte]`: serializes the message and return a byte array containing its raw bytes.
* `def writeTo(output: OutputStream): Unit`: serializes the message and writes it to an `OutputStream`.

## Parsing

The companion object of each message extends a base trait called `GeneratedMessageCompanion[A]` where `A` is the type of the message. It provides many
useful methods that helps dealing with a message in a generic way, however the primary use is parsing:

* `def parseFrom(input: InputStream): A`: reads and parses a message from an `InputStream`.
* `def parseFrom(s: Array[Byte]): A`: parses a message from the given byte array.


## Parsing from an input stream

The following example code loads binary data from a file and parses it as an `AddressBook`. If the file doesn't exist it returns an empty `AddressBook`:

```scala mdoc:passthrough:silent
S.example("AddressBookMain.scala", "readFromFile")
```

## Adding new person

The following code prompts the user to enter a person's data. It then loads
the address book from a file, adds the new person to the list, and saves it again:

```scala mdoc:passthrough:silent
S.example("AddressBookMain.scala", "addPerson")
```

## Running the example

In sbt, type `run`

> This document, "Protocol Buffer Tutorial: Scala" is a modification of ["Protocol Buffer Basics: Java"](https://developers.google.com/protocol-buffers/docs/javatutorial), which is a work created and [shared by Google](https://developers.google.com/terms/site-policies)  and used according to terms described in the [Creative Commons 4.0 Attribution License](https://creativecommons.org/licenses/by/4.0/).