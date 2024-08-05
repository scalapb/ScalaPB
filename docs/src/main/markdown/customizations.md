---
title: "Customizations"
layout: docs
---

## Getting started with scalapb.proto

ScalaPB's code generator provides supports many different customizations. To
get access to these customizations, you need to import `scalapb/scala.proto`
in the proto files you want to customize. You can also have the options apply
to an entire proto3 package by using package-scoped options (see below).

To have `scalapb/scalapb.proto` available to be imported in your project, add
the following SBT setting in your `build.sbt`:

    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

If you are invoking `protoc` manually, you will need to ensure that the files in
[`protobuf`](https://github.com/scalapb/ScalaPB/tree/master/protobuf)
directory are available to your project.


## ScalaPB File-level Options

ScalaPB file-level options lets you

- specify the name of the Scala package to use (the default is using the java package name).
- request that ScalaPB will not append the protofile name to the package name.
- specify Scala imports so custom base traits and custom types (see below) do
  not require the full class name.

The file-level options are not required, unless you are interested in those
customizations. If you do not want to customize the defaults, you can safely
skip this section.

## File-level options

```protobuf
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  scope: FILE
  package_name: "com.example.myprotos"
  flat_package: true
  single_file: true
  java_conversions: false
  import: "com.thesamet.pb.MyType"
  import: "com.thesamet.other._"
  preamble: "sealed trait BaseMessage"
  preamble: "sealed trait CommonMessage"
  lenses: true
  getters: true
  retain_source_code_info: false
  no_default_values_in_constructor: false
  preserve_unknown_fields: false
  enum_value_naming: CAMEL_CASE
  enum_strip_prefix: false
  bytes_type: "scodec.bits.ByteVector"
  scala3_sources: false
  public_constructor_parameters: false
};
```

- `scope` controls whether the specified options apply only for this proto
  files or for the entire package. Default is `FILE`. See [package-scoped options](#package-scoped-options)
  for more details.

- `package_name` sets the Scala base package name, if this is not defined,
then it falls back to the `java_package` option. If the `java_package` option
is also not the found, then the package name from file's `package` statement
is used.

- Setting `flat_package` to true (default is `false`) makes ScalaPB not append
the protofile base name to the package name.  You can also apply this option
globally to all files by adding it to your [ScalaPB SBT Settings](sbt-settings.md).

- The `single_file` option makes the generator output all messages and
enums to a single Scala file.

- The `java_conversions` options tells ScalaPB to generate converters to the
  corresponding Java messages in this file. It does not automatically trigger
  Java source code generation for the messages. If you need to generate source code
  in Java, include `PB.gens.java` in the list of targets in sbt-protoc.

- The `preamble` is a list of strings that is output at the top of the
  generated Scala file. This option requires `single_file` to be set. It is
  commonly used to define sealed traits that are extended using
  `(scalapb.message).extends` - see custom base traits below and [this example](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/main/protobuf/nocode/sealed_trait.proto).

- The `object_name` option lets you customize the name of the generated class
  that contains various file-level members such as descriptors and a list of
  companion objects for the generated messages and enums. This is useful in
  case you are running into issues where the generated class name conflicts
  with other things in your project.

- Setting `lenses` to `false` inhibits generation of lenses (default is `true`).
- Setting `getters` to `false` inhibits generation of getters (default is `true`).

- Setting `retain_source_code_info` to `true` retains information in the descriptor that
  can be used to retrieve source code information from the descriptor at
  runtime (such as comments and source code locations). This option is turned
  off by default to conserve source size and memory at runtime. When this
  option is enabled, use the `location` method on various descriptors to
  access source code information.

- By default, all non-required fields have default values in the constructor of the generated
  case classes. When setting `no_default_values_in_constructor` to `true` no
  default values will be generated for all fields. There is also a
  message-level `no_default_values_in_constructor` and field-level
  `no_default_value_in_constructor`. If the field-level setting is set, it
  overrides the message-level. If the message-level setting is set, it overrides
  the file-level setting.

- Typically, enum values appear in UPPER_CASE in proto files, and ScalaPB generates case objects
  with exactly the same name in Scala. If you would like ScalaPB to transform the names into CamelCase, set `enum_value_naming` to `CAMEL_CASE`.

- It is a common practice in protobufs to prefix each enum value name with the name of the enum.
  For example, an enum name `Size` may have values named `SIZE_SMALL` and `SIZE_LARGE`. When you set `enum_strip_prefix`
  to `true`, ScalaPB will strip the enum's name from each value name, and they would become `SMALL` and `LARGE`.
  Then the name can be transformed to camel-case according to `enum_value_naming`. Note that the prefix that is removed
  is the all-caps version of the enum name followed by an underscore.

- By default, during deserialization only known fields are retained.
  When setting `preserve_unknown_fields` to `true`, all generated messages in this file will preserve unknown fields.
  This is default behaviour in java for Proto3 messages since 3.5.0.
  In ScalaPB 0.10.0: the default of this field became `true` for consistency
  with Java.

- Use `bytes_type` to customize the Scala type used for the `bytes` field
  type. You will need to have an implicit
  `TypeMapper[com.google.protobuf.ByteString, YourType]` instance so ScalaPb
  can convert back and forth to the type of your choice. That implicit will be
  found if it is defined under `YourType` companion object, or on a package
  object that matches the generated code (or any of its parent packages).

- By default, ScalaPB generates Scala sources that are compatible with both Scala 2 and Scala 3. To generate sources that can be compiled error-free with `-source feature` on Scala 3 or with `-Xsource:3` on Scala 2.13, set `scala3_sources` to `true` or pass the `scala3_sources` generator parameter.

- Use `public_constructor_parameters` to make constructor parameters public, including defaults and TypeMappers. 
  This is helpful for automated schema derivation with e.g. `magnolia` when trying to also derive default fields by 
  using the compiler flag `-Yretain-trees`. Without this flag, the companion object's `_typemapper_*` fields are private.

## Package-scoped options

Note: this option is available in ScalaPB 0.8.2 and later.

Sometimes you want to have the same file-level options applied to all
the proto files in your project.  To accomplish that, add a `package.proto`
file (the name does not matter) next to your proto files that looks like this:

```
import "scalapb/scalapb.proto";

package com.mypackage;

option (scalapb.options) = {
  scope: PACKAGE
  flat_package: true
};
```

All the options in this file will be applied to all proto files in the
package `com.mypackage` and its sub-packages.

There is no need to explicitly import this file from other protos. If you are
using `sbt-protoc` and the file is in the proto source directory (default is
`src/main/protobuf`) then the file will be found and the options applied. If
you are invoking protoc in another way, you need to ensure that this
file is passed to protoc together with the rest of the files.

If you are generating Scala code for proto files that you don't own, you can
use this feature to customize code generation by creating a `package.proto`
file for that third-party package and include it within your proto source
directory.

The following rules are applied when validating package-scoped options:

- At most one file in each package may provide package-scoped options.
- Sub-packages may override package-scoped options provided by their parent
  packages. The options are merged using the Protocol Buffers `mergeFrom`
  semantics. Specifically, this implies that repeated fields such as `import`
  and `preamble` are concatenated.
- Proto files get the most specific package-scoped options for the package
  they are in. File-level options defined in a proto file get merged with the
  package-level options using `mergeFrom`.
- Proto files with package-scoped options must have a `package` statement.
  This is to prevent the possibility of options applied globally. Standard
  classes that are shipped with ScalaPB already assume certain options, so
  overriding options globally may lead to compilation errors.

### Publishing package-scoped options

If you are publishing a library that includes protos with package-scoped
options, you need to make sure your library users source the package-scoped
option proto file so the customizations are applied when they generate code.

Your users can simply import your package-scoped options from any proto file
in their project to have the settings applied (a single import of
the package-scoped options file would apply it globally for the code generator).
However, since ScalaPB 0.10.11 and sbt-protoc 1.0.1, sbt-protoc provides a way to automate this with no
need to manually import the package-scoped options file. This is accomplished
by including a special attribute in the manifest of the library you publish. Add the following to
your library's settings:

```scala
Compile / packageBin / packageOptions += (
    Package.ManifestAttributes("ScalaPB-Options-Proto" -> "path/to/package.proto")
)
```

The path above is relative to the root directory of the published JAR (so `src/main/protobuf` is not needed). Users add your library to their projects like this:

```scala
libraryDependencies ++= Seq(
    "com.example" %% "your-library" % "0.1.0",
    "com.example" %% "your-library" % "0.1.0" % "protobuf"
)
```

The first dependency provides the precompiled class files. The second dependency makes it possible
for users to import the protos in the jar file. `sbt-protoc` will look for the
`ScalaPB-Options-Proto` attribute in the jar's manifest and automatically add the package scoped options file
to the protoc command line.

:::note
Since the package-scoped options file is used as a source file in multiple
projects, it should not define any types (messages, enums, services).
This ensures that the package-scoped proto file does not generate any code on its own so we don't
end up with duplicate class files.
:::

### Disabling package-scoped options processing

As a consumer of third-party dependencies that come with options proto, you
can disable the behavior of automatically adding the options proto to protoc
by setting

```scala
Compile / PB.manifestProcessing := false
```

in sbt. In that case, it is your responsibility to either manually `import` the option protos in one
of your own project source files so it gets applied, or ensure that the
generator settigs used in your project are consistent with the ones used to
generate the dependency. Differences in settings can lead to generated code
that does not compile.

## Auxiliary options

In some situations, you may want to set some options in a proto file, but without
modifying the original proto file or adding anything ScalaPB-specific to it. To accomplish
that, you can define auxiliary options under package-scoped options.

For example, if you are given this proto file:

```protobuf
syntax = "proto3";
package a.b.c;
message Foo {
  string hello = 1;
}
```

You can add a file `package.proto` with the following content:
```protobuf
syntax = "proto3";
package a.b.c;
import "scalapb/scalapb.proto";
option (scalapb.options) = {
    scope: PACKAGE
    aux_message_options: [
        {
            target: "a.b.c.Foo"
            options: {
                extends: "com.myexample.SomeTrait"
            }
        }
    ]
    aux_field_options: [
        {
            target: "a.b.c.Foo.hello"
            options: {
                scala_name: "goodbye"
            }
        }
    ]
};
```

The list `aux_message_options` contains options targeted at different messages define under the same proto package of the package-scoped options. The `target` name needs to be fully-qualified message name in the protobuf namespace. Similar to `aux_message_options`, we also have `aux_enum_options`, `aux_enum_value_options` and `aux_field_options`. See [example usage here](https://github.com/scalapb/ScalaPB/tree/master/e2e/src/main/protobuf/scoped). If the target is set `*` then the options will be
applied to all the entities in the file or package (depending on the `scope` option).

## Primitive wrappers

In proto 3, unlike proto 2, primitives are not wrapped in an option by default.
The standard technique to obtain an optional primitive is to wrap it inside a
message (since messages are provided inside an `Option`). Google provides
standard wrappers to the primitive types in
[wrappers.proto](https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf/wrappers.proto).

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

## Custom base traits for messages

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

In your code, define the base trait `BaseCustomer` and include any subset of the fields:

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

## Custom base traits for sealed oneofs

Since 0.9.0, you can use `sealed_one_extends` to define one or more base traits for a generated SealedOneof.

Since 0.11.16, you can also add base traits to the empty case object using `sealed_oneof_empty_extends`.

Use the following options to
```protobuf
message MyEither {
  option (scalapb.message).sealed_oneof_extends = "MyBaseTrait";
  option (scalapb.message).sealed_oneof_empty_extends = "MyEmptyTrait";

  oneof sealed_value {
    Left left = 1;
    Right right = 2;
  }
}
```

As of ScalaPB 0.11.11 you may also use following option to make the generated sealed oneof
trait [universal](https://docs.scala-lang.org/overviews/core/value-classes.html).
It may be useful when your sealed oneof variants are value-classes (e.g. extends `AnyVal`)

```scala
trait MyBaseUniversalTrait extends Any
```

```protobuf
message Left {
  option (scalapb.message).extends = "AnyVal";
  string error = 1;
}

message Right {
  option (scalapb.message).extends = "AnyVal";
  int32 value = 1;
}

message MyEither {
  option (scalapb.message) = {
    sealed_oneof_extends: ["Any", "MyBaseUniversalTrait"]
  };

  oneof sealed_value {
    Left left = 1;
    Right right = 2;
  }
}
```

## Custom base traits for sealed oneofs companion objects

> Note: this option is available in ScalaPB 0.11.11 and later.

Use the following option to define one or more base traits for a generated SealedOneof companion object:

```protobuf
message MyEither {
  option (scalapb.message).sealed_oneof_companion_extends = "MyBaseTrait";
  oneof sealed_value { /* ... */ }
}
```

## Custom base traits for enums

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

## Custom types

You can customize the Scala type of any field.  One use-case for this is when
you would like to use type-safe wrappers around primitive values to enforce unit
correctness. For example, instead of using a raw integer for time fields, you can
wrap them in a `Seconds` class.

```protobuf
import "scalapb/scalapb.proto";

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
  implicit val typeMapper: TypeMapper[Int, Seconds] = TypeMapper(Seconds.apply)(_.v)
}
```

`TypeMapper` takes two function parameters. The first converts from the original type to
the custom type. The second function converts from the custom type to the
original type.

In addition to primitive values, you can customize enums and messages as well.

For more examples, see:

- [`custom_types.proto`](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/protobuf/custom_types.proto)
- [`PersonId.scala`](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/scala/com/thesamet/pb/PersonId.scala)
- [`CustomTypesSpec.scala`](https://github.com/scalapb/ScalaPB/blob/master/e2e/src/test/scala/CustomTypesSpec.scala)

If you have a TypeMapper that maps a generated type into a type you don't own
(such as `String`, or a third-party class) then you don't have access to the
companion object to define the typemapper in. Instead, you can place the
typemapper in one of the parent package objects of the generated code. For
example, if you want to map an enum to a string, and the message containing it
goes into the `a.b.c` package, you can define the type mapper like this:

```scala
// src/main/scala/a/b/c/package.scala:
package a.b

package object c {
  implicit val segmentType: TypeMapper[SegmentType, String] =
    TypeMapper[SegmentType, String](_.name)(SegmentType.fromName(_).get)
}
```

## Message-level custom type and boxing

In the previous section you saw how to customize the type generated for a
specific field. ScalaPB also lets you specify a custom type at the message
level. When `type` is set at the message level, that type is used for all the
fields that use that message. This eliminates the need to specify `type` on each field
of this type.

```protobuf
// duration.proto
syntax = "proto3";

package mytypes;

import "scalapb/scalapb.proto";

message Duration {
    option (scalapb.message).type = "mytypes.MyDurationClass";
    int32 seconds = 1;
}
```

In a Scala file define an implicit mapper:

```scala mdoc
import scalapb.TypeMapper
import mytypes.duration.Duration

case class MyDurationClass(seconds: Int)

object MyDurationClass {
    implicit val tm: TypeMapper[Duration, MyDurationClass] = TypeMapper[Duration, MyDurationClass] {
        d: Duration => MyDurationClass(d.seconds) } {
        m: MyDurationClass => Duration(m.seconds)
    }
}
```

Now, each time you reference `Duration` in a proto file, the generated field in Scala code
will be of type `MyDuration`:

```protobuf
syntax = "proto3";

package mytypes;

import "scalapb/scalapb.proto";
import "duration.proto";

message Usage {
    Duration dd = 1;           // will become dd: Option[MyDuration]
    repeated Duration ds = 2;  // will become ds: Seq[MyDuration]

    // You can eliminate the boxing of an optional field in an Option by using
    // no_box
    Duration dd_nobox = 3 [(scalapb.field).no_box = true];  // will become ddNoBox: MyDuration
}
```

If you do not want any instance of your message to be boxed (regardless if it
has a custom type), you can set `no_box` at the message-level:

```protobuf
// duration_nobox.proto
syntax = "proto3";

package mytypes;

import "scalapb/scalapb.proto";

message Duration {
    option (scalapb.message).type = "mytypes.MyDurationType";
    option (scalapb.message).no_box = true;  // do not wrap in Option
    int32 seconds = 1;
}
```

Then when this message is used, it will not be wrapped in an `Option`. If
`no_box` is specified at the field level, it overrides the value specified at
the message level.

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

Example: see `CustomMaps` in [maps.proto](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/protobuf/maps.proto)

You can also customize the collection type used for a map. See the next
section for details.

## Custom collection types

By default, ScalaPB compiles repeated fields into a `Seq[T]`. When a message
is parsed from bytes, the default implementation instantiates a `Vector[T]`,
which is a subtype of `Seq[T]`.  You can instruct ScalaPB to use a different
collection type for one field by specifying the `collection_type` option. You
can also specify a `collection_type` for the entire proto file by specifying a
`collection_type` at the file-level.

If both are defined then the field-level setting wins.

Similar to `collection_type`, we have `map_type` for map types. By default,
ScalaPB generates `scala.collection.immutable.Map` for maps, and you can
customize it at the field level, or file-level by specifying a `map_type`
option.

`map_type` was introduced in ScalaPB 0.8.5.

```protobuf
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  collection_type: "Set"
};

message CollTest {
    // Will generate Set[Int] due to file-level option.
    repeated int32  rep1 = 1;

    // Will generate an Array[String]
    repeated string rep2 = 2 [
      (scalapb.field).collection_type="Array"];

    // Will generate Seq[collection.immutable.Seq]
    repeated bool rep3 = 3 [
      (scalapb.field).collection_type="collection.immutable.Seq"];

    map<int32, string> my_map = 4 [
      (scalapb.field).map_type="collection.mutable.Map"];
}
```

Note on mutable collection: ScalaPB assumes that all data is immutable. For example, the result
of `serializedSize` is cached in a private field. When choosing mutable collections, you must be
careful not to mutate any collection after it has been passed to any message, or you might get some
surprising results!

Note: using `Array` is not supported along with Java conversions.

Note: Most Scala collections can be used with this feature. If you are trying
to implement your own collection type, it may be useful to check `MyVector`
and `MyMap`, the simplest custom collection that is compatible with ScalaPB:

- [MyVector.scala](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/scala-2.13/com/thesamet/pb/MyVector.scala)
- [MyMap.scala](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/scala-2.13/com/thesamet/pb/MyMap.scala)
- [collection_types.proto](https://github.com/scalapb/ScalaPB/blob/master/e2e-withjava/src/main/protobuf/collection_types.proto)

## Custom names

Sometimes it may be useful to manually specify the name of a field in the
generated code.  For example, if you have a field named `hash_code`, then the
camel-case version of it would be `hashCode`. Since that name would conflict with
the [`hashCode()` method](https://docs.oracle.com/javase/7/docs/api/java/lang/Object.html#hashCode())
we inherit from Java, ScalaPB issues an error. You can tell ScalaPB to use an
alternative name by using the `scala_name` option:

```protobuf
optional string hash_code = 1 [(scalapb.field).scala_name = "myHashCode"];
```

It is also possible to customize the Scala name of an enum value:

```protobuf
enum MyEnum {
  DEFAULT = 0;
  FOO = 1 [(scalapb.enum_value).scala_name = "Bar"];
}
```

The same customization can be applied to `oneof` fields:

```protobuf
oneof notify {
  option (scalapb.oneof).scala_name = "myNotify";

  string foo = 1;
  int32 bar = 2;
}
```

## Adding annotations

Since ScalaPB 0.6.3, you can add annotations to the generated case classes like this:

```protobuf
message BarMessage {
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation";
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation1";
  option (scalapb.message).annotations = "@mypackage.CustomAnnotation2";
}
```

In ScalaPB 0.7.0, you can add annotations to the companion object of a
message and to individual fields:

```protobuf
message BarMessage {
  option (scalapb.message).companion_annotations = "@mypackage.AnotherAnnotation2";

  optional string x = 1 [
      (scalapb.field).annotations = '@deprecated("Will be gone", "1.0")'
  ];
}
```


In ScalaPB 0.10.9, you can also add annotations to the auto generated unknownFields field:

```protobuf
message BarMessage {
  option (scalapb.message).unknown_field_annotations = "@annotation1";
}
```

In ScalaPB 0.11.4, you can also add annotations to the enum values and the `Unrecognized` case class:

```protobuf
enum BarEnum {
  option (scalapb.enum_options) = "@annotation"
}

enum BarEnum {
  // every value will have the annotation added.
  option (scalapb.enum_options).base_annotations = "@annotation1";
  // only known values (case objects) will have the annotation added.
  option (scalapb.enum_options).recognized_annotations = "@annotation2";
  // only the unrecognized case class will have the annotation added.
  option (scalapb.enum_options).unrecognized_annotations = "@annotation3";
  // only this value (case object) will have the annotation added.
  BarValue = 1 [(scalapb.enum_value).annotations = "@annotation4"];
}
```

## Adding derives clause

In ScalaPB 0.11.14, it is possible to add a `derives` clause to generated messages and
sealed oneofs:

```protobuf
message Foo {
  option (scalapb.message).derives = "yourpkg.Show";
  ...
}}}

message Expr {
  option (scalapb.message).sealed_oneof_derives = "yourpkg.Show";
  oneof sealed_value {
    ...
  }
}
```
