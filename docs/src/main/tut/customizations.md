---
title: "Customizations"
layout: docs
---

# ScalaPB File-level Options

ScalaPB file-level options lets you

- specify the name of the Scala package to use (the default is using the java package name).
- request that ScalaPB will not append the protofile name to the package name.
- specify Scala imports so custom base traits and custom types (see below) do
  not require the full class name.

The file-level options are not required, unless you are interested in those
customizations. If you do not want to customize the defaults, you can safely
skip this section.

# Package options

```protobuf
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  package_name: "com.example.myprotos"
  flat_package: true
  single_file: true
  import: "com.thesamet.pb.MyType"
  import: "com.thesamet.other._"
  preamble: "sealed trait BaseMessage"
  preamble: "sealed trait CommonMessage"
};
```

- `package_name` sets the Scala base package name, if this is not defined,
then it falls back to `java_package` and then to `package`.

- Setting `flat_package` to true (default is `false`) makes ScalaPB not append
the protofile base name to the package name.  You can also apply this option
globally to all files by adding it to your [ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

- The `single_file` option makes the generator output all messages and
enums to a single Scala file.

- The `preamble` is a list of strings that is output at the top of the
  generated Scala file. This option requires `single_file` to be set. It is
  commonly used to define sealed traits that are extended using
  `(scalapb.message).extends` - see custom base traits below and [this example](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/protobuf/sealed_trait.proto).

- The `object_name` option lets you customize the name of the generated class
  that contains various file-level members such as descriptors and a list of
  companion objects for the generated messages and enums. This is useful in
  case you are running into issues where the generated class name conflicts
  with other things in your project.

# Primitive wrappers

In proto 3, unlike proto 2, primitives are not wrapped in an option by default.
The standard technique to obtain an optional primitive is to wrap it inside a
message (since messages are provided inside an `Option`). Google provides
standard wrappers to the primitive types in
[wrappers.proto](https://github.com/google/protobuf/blob/master/src/google/protobuf/wrappers.proto).

`primitive_wrappers` is enabled by default for ScalaPB>=0.6.0. Whenever one
of the standard wrappers is used, it will be mapped to `Option[X]` where `X`
is a primitive type. For example:

```protobuf
syntax = "proto3";

import "google/protobuf/wrappers.proto";

message MyMessage {
  google.protobuf.Int32Value my_int32 = 5;
}
```

would generate

```scala
case class MyMessage(myInt32: Option[Int]) extends ...
```

To disable primitive wrappers in a file:

```protobuf
import "scalapb/scalapb.proto";
option (scalapb.options) = {
  no_primitive_wrappers: true
};
```

In versions of ScalaPB prior to 0.6.0, primitive wrappers had to be turned on
manually in each file:

```protobuf
import "scalapb/scalapb.proto";
option (scalapb.options) = {
  primitive_wrappers: true
};
```

# Custom base traits for messages

Note: this option is available in ScalaPB 0.6.1 and later.

ScalaPBs allows you to specify custom base traits to a generated case
class.  This is useful when you have a few messages that share common fields
and you would like to be able to access those fields through a single trait.

Example:

```protobuf
import "scalapb/scalapb.proto";

message CustomerWithPhone {
  option (scalapb.message).extends = "com.thesamet.pb.BaseCustomer";

  optional string customer_id = 1;
  optional string name = 2;
  optional string phone = 3;
}
```

In your code, define the base trait `DomainEvent` and include any subset of the fields:

```scala
package com.thesamet.pb

trait BaseCustomer {
  def customerId: Option[String]
  def name: Option[String]
}
```

You can specify any number of base traits for a message.

It is also possible to make the generated companion classes extend a class
or trait, by using the `companion_extends` option. For example:

```protobuf
message MyMessage {
  option (scalapb.message).extends = "MySuperClass";
  option (scalapb.message).companion_extends = "MySuperCompanionClass";
  int32 n = 1;
}
```

Will generate a case class that extends `MySuperClass`, and the companion
object will extend `MySuperCompanionClass`.

# Custom base traits for enums

In a similar fashion to custom base traits for messages, it is possible to
define custom base traits for enum types, for the companion objects of enum
types and even for specific values.

For example:

```protobuf
syntax = "proto2";

package enum_example;

import "scalapb/scalapb.proto";

enum MyEnum {
  option (scalapb.enum_options).extends = "example.EnumOptions.EnumBase";
  option (scalapb.enum_options).companion_extends = "example.EnumOptions.EnumCompanionBase";
  Unknown = 0;
  V1 = 1 [(scalapb.enum_value).extends = "example.EnumOptions.ValueMixin"];
  V2 = 2;
}
```

The generated code will look something like this:

```scala
sealed trait MyEnum extends GeneratedEnum 
    with example.EnumOptions.EnumBase {
  /* ... */
}

object MyEnum extends GeneratedEnumCompanion[MyEnum]
    with example.EnumOptions.EnumCompanionBase {
  case object Unknown extends MyEnum { /* ... */ }

  case object V1 extends MyEnum 
      with example.EnumOptions.ValueMixin { /* ... */ }

  case object V2 extends MyEnum { /* ... */ }

  /* ... */
}
```

# Custom types

You can customize the Scala type of any field.  One use-case for this is when
you would like to use type-safe wrappers around primitive values to enforce unit
correctness. For example, instead of using a raw integer for time fields, you can
wrap them in a `Seconds` class.

```protobuf
message Connection {
  optional int32 timeout = 1 [(scalapb.field).type = "mydomain.Seconds"];
}
```

We would like to write code like this:

```scala
val c = Connection().update(_.timeout := Seconds(5))
```

How will ScalaPB know how to convert from the original type (`Integer`) to the
custom type `Seconds`? For each custom type you need to define an implicit
`TypeMapper` that will tell ScalaPB how to convert between the custom type and
the base Scala type.  A good place to define this implicit is in the companion
class for your custom type, since the Scala compiler will look for a
typemapper there by default.  If your typemapper is defined elsewhere, you
will need to import it manually by using the `import` file-level option.

```scala
package mydomain

case class Seconds(v: Int) extends AnyVal

object Seconds {
  implicit val typeMapper = TypeMapper(Seconds.apply)(_.v)
}
```

`TypeMapper` takes two function parameters. The first converts from the original type to
the custom type. The second function converts from the custom type to the
original type.

In addition to primitive values, you can customize enums and messages as well.

For more examples, see:

- [`custom_types.proto`](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/protobuf/custom_types.proto)
- [`PersonId.scala`](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/scala/com/thesamet/pb/PersonId.scala)
- [`CustomTypesSpec.scala`](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/test/scala/CustomTypesSpec.scala)

## Custom types on maps

Since version 0.6.0 it is possible to customize the key and value types of
maps. Like the custom types described above you will need to have a `TypeMapper`
for the custom type.

Example:

```protobuf
message CustomMaps {
  // Will generate Map[String, com.thesamet.pb.Years]
  map<string, int32> string_to_year = 1 [
      (scalapb.field).value_type = "com.thesamet.pb.Years"];

  // Will generate Map[PersonId, Int]
  map<string, int32> person_to_int = 2 [
      (scalapb.field).key_type = "com.thesamet.pb.PersonId"];

  // Will generate Map[PersonId, com.thesamet.pb.Years]
  map<string, int32> person_to_year = 3 [
      (scalapb.field).key_type = "com.thesamet.pb.PersonId",
      (scalapb.field).value_type = "com.thesamet.pb.Years"];
}
```

Example: see `CustomMaps` in [maps.proto](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/protobuf/maps.proto)

# Custom collection types

By default, ScalaPB compiles repeated fields into a `Seq[T]`. When a message
is parsed from bytes, the default implementation generates a `Vector[T]`,
which is a subtype of `Seq[T]`.  You can instruct ScalaPB to use a different
collection type for a file, or just for one repeated field. If both are
defined then the field-level setting wins.

```protobuf
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  collection_type: "Set"
};

message CollTest {
    // Will generate Set[Int] due to file-level option.
    repeated int32  rep1 = 1;

    // Will generate an Array[String]
    repeated string rep2 = 2  [(scalapb.field).collection_type="Array"];

    // Will generate Seq[collection.immutable.Seq]
    repeated bool rep3 = 3  [
      (scalapb.field).collection_type="collection.immutable.Seq"];
}
```

Note: using `Array` is not supported along with Java conversions.

Note: Most Scala collections can be used with this feature. If you are trying
to implement your own collection type, it may be useful to check `MyVector`,
the simplest custom collection that is compatible with ScalaPB:
- [MyVector.scala](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/scala/com/thesamet/pb/MyVector.scala)
- [collection_types.proto](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/protobuf/collection_types.proto)

# Custom names

Sometimes it may be useful to manually specify the name of a field in the
generated code.  For example, if you have a field named `hash_code`, then the
camel-case version of it would be `hashCode`. Since that name would conflict with
the [`hashCode()` method](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#hashCode())
we inherit from Java, ScalaPB issues an error. You can tell ScalaPB to use an
alternative name by using the `scala_name` option:

```protobuf
optional string hash_code = 1 [(scalapb.field).scala_name = "myHashCode"];
```

# Adding annotations

Since ScalaPB 0.6.3, you can add annotations to the generated case classes like this:

```protobuf
message BarMessage {
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation";
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation1";
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation2";
}
```

# Adding scalapb.proto to your project

The easiest way to get `protoc` to find `scalapb/scalapb.proto` when compiling
through SBT is by adding the following to your `build.sbt`:

    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

If you are invoking `protoc` manually, you will need to ensure that the files in
[`protobuf`](https://github.com/scalapb/ScalaPB/tree/master/protobuf)
directory are available to your project.
