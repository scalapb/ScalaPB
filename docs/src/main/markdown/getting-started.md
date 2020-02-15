---
title: "Getting Started"
layout: docs
---

# Protocol Buffer Basics: Scala

This tutorial provides a basic Java programmer's introduction to working with protocol buffers. By walking through creating a simple example application, it shows you how to

* Define message formats in a .proto file.

* Use SBT to generate Scala case classes from proto files.

* Use ScalaPB API to write and read messages.

This isn't a comprehensive guide to using protocol buffers in Scala. For more detailed reference information, see the [Protocol Buffer Language Guide](https://developers.google.com/protocol-buffers/docs/proto), the [Generated Code page]({{site.baseurl}}/generated-code.html).
), and the [Encoding Reference](https://developers.google.com/protocol-buffers/docs/encoding).

# Why Use Protocol Buffers?￼

The example we're going to use is a very simple "address book" application that can read and write people's contact details to and from a file. Each person in the address book has a name, an ID, an email address, and a contact phone number.

How do you serialize and retrieve structured data like this? There are a few ways to solve this problem:

* Use Java Serialization. This is the default approach since it's built into the language, but it has a host of well-known problems (see Effective Java, by Josh Bloch pp. 213), and also doesn't work very well if you need to share data with applications written in C++ or Python.
* You can invent an ad-hoc way to encode the data items into a single string – such as encoding 4 ints as "12:3:-23:67". This is a simple and flexible approach, although it does require writing one-off encoding and parsing code, and the parsing imposes a small run-time cost. This works best for encoding very simple data.
* Serialize the data to XML (or JSON). This approach can be very attractive since XML and JSON are (sort of) human readable and there are binding libraries for lots of languages. This can be a good choice if you want to share data with other applications/projects. However, XML is notoriously space intensive, and encoding/decoding it can impose a huge performance penalty on applications. Also, navigating an XML DOM tree is considerably more complicated than navigating simple fields in a class normally would be.

Protocol buffers are the flexible, efficient, automated solution to solve exactly this problem. With protocol buffers, you write a `.proto` description of the data structure you wish to store. From that, the protocol buffer compiler creates a case class that implements automatic encoding and parsing of the protocol buffer data with an efficient binary format. Importantly, the protocol buffer format supports the idea of extending the format over time in such a way that the code can still read data encoded with the old format.

# Where to Find the Example Code￼
The example code is included in the source code zip files, under the `src/main/scala/examples/" directory. [Download it here](https://github.com/scalapb/ScalaPB/releases).

# Defining Your Protocol Format

```protobuf
syntax = "proto2";

package tutorial;

option java_package = "com.example.tutorial";

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

The `.proto` file starts with a package declaration, which helps to prevent naming conflicts between different projects. In Scala, the package name followed by the file name is used as the Scala package unless you have either explicitly specified a java_package, as we have here, or specified a [scala package option]({{site.baseurl}}/customizations.html). Even if you do provide a `java_package`, you should still define a normal package as well to avoid name collisions in the Protocol Buffers name space as well as in other languages.

After the package declaration, you can see the `java_package` option. This option was originally introduced for the Java code generator, however ScalaPB concatenates to that the proto file name to determine the Scala package name. This prevents namespace collisions in projects that have both Scala and Java generated code.

Next, you have your message definitions. A message is just an aggregate containing a set of typed fields. Many standard simple data types are available as field types, including `bool`, `int32`, `float`, `double`, and `string`. You can also add further structure to your messages by using other message types as field types – in the above example the `Person` message contains `PhoneNumber` messages, while the `AddressBook` message contains `Person` messages. You can even define message types nested inside other messages – as you can see, the `PhoneNumber` type is defined inside `Person`. You can also define `enum` types if you want one of your fields to have one of a predefined list of values – here you want to specify that a phone number can be one of `MOBILE`, `HOME`, or `WORK`.

The `" = 1"`, `" = 2"` markers on each element identify the unique "tag" that field uses in the binary encoding. Tag numbers 1-15 require one less byte to encode than higher numbers, so as an optimization you can decide to use those tags for the commonly used or repeated elements, leaving tags 16 and higher for less-commonly used optional elements. Each element in a repeated field requires re-encoding the tag number, so repeated fields are particularly good candidates for this optimization.

Each field must be annotated with one of the following modifiers:

* `required`: a value for the field must be provided when constructing a message case class. Parsing a message that misses a required field will throw an `InvalidProtocolBufferException`. Other than this, a required field behaves exactly like an optional field.
* `optional`: the field may or may not be set. If an optional field value isn't set, a default value is used. For simple types, you can specify your own default value, as we've done for the phone number type in the example. Otherwise, a system default is used: zero for numeric types, the empty string for strings, false for bools. For embedded messages, the default value is always the "default instance" or "prototype" of the message, which has none of its fields set. Calling the accessor to get the value of an optional (or required) field which has not been explicitly set always returns that field's default value. In proto2, optional fields are represented as `Option[]`. In proto3, optional primitives are not wrapped in `Option[]`, but messages are.
`repeated`: the field may be repeated any number of times (including zero). The order of the repeated values will be preserved in the protocol buffer. Think of repeated fields as dynamically sized arrays. They are represented in Scala as `Seq`s.

> *Required Is Forever*. You should be very careful about marking fields as required. If at some point you wish to stop writing or sending a required field, it will be problematic to change the field to an optional field – old readers will consider messages without this field to be incomplete and may reject or drop them unintentionally. You should consider writing application-specific custom validation routines for your buffers instead. Some engineers at Google have come to the conclusion that using required does more harm than good; they prefer to use only optional and repeated. However, this view is not universal.

You'll find a complete guide to writing `.proto` files – including all the possible field types – in the [Protocol Buffer Language Guide](https://developers.google.com/protocol-buffers/docs/proto). Don't go looking for facilities similar to class inheritance, though – protocol buffers don't do that.

> This document, "Protocol Buffer Basics: Scala" is a modification of ["Protocol Buffer Basics: Java"](https://developers.google.com/protocol-buffers/docs/javatutorial), which is a work created and [shared by Google](https://developers.google.com/terms/site-policies)  and used according to terms described in the [Creative Commons 4.0 Attribution License](https://creativecommons.org/licenses/by/4.0/).