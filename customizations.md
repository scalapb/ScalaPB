---
title: "ScalaPB: Customizations"
layout: page
redirect_from: "/file-options.html"
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

{% highlight proto %}
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  package_name: "com.example.myprotos"
  flat_package: true
  single_file: true
  import: "com.trueaccord.pb.MyType"
  import: "com.trueaccord.other._"
  preamble: "sealed trait BaseMessage"
  preamble: "sealed trait CommonMessage"
};
{% endhighlight %}

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
  `(scalapb.message).extends` - see custom base traits below and [this example](https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/main/protobuf/sealed_trait.proto).

# Primitive wrappers

The `primitive_wrappers` option is useful when you want to have optional
primitives in Proto 3, for example `Option[Int]`. In proto 3, unlike proto 2,
primitives are not wrapped in an option by default. The standard technique to
obtain an optional primitive is to wrap it inside a message (since messages
are provided inside an `Option`). Google provides standard wrappers to the
primitive types in
[wrappers.proto](https://github.com/google/protobuf/blob/master/src/google/protobuf/wrappers.proto).

If `primitive_wrappers` is enabled, then whenever one of the standard wrappers
is used, it will be mapped to `Option[X]` where `X` is a primitive type. For
example:

{% highlight proto %}
syntax = "proto3";

import "scalapb/scalapb.proto";
import "google/protobuf/wrappers.proto";

option (scalapb.options) = {
  primitive_wrappers: true
};

message MyMessage {
  google.protobuf.Int32Value my_int32 = 5;
}
{% endhighlight %}

would generate

{% highlight scala %}
case class MyMessage(myInt32: Option[Int]) extends ...
{% endhighlight %}

# Custom base traits

ScalaPBs allows you to specify custom base traits to a generated case
class.  This is useful when you have a few messages that share common fields
and you would like to be able to access those fields through a single trait. 

Example:

{% highlight proto %}
import "scalapb/scalapb.proto";

message CustomerWithPhone {
  option (scalapb.message).extends = "com.trueaccord.pb.BaseCustomer";

  optional string customer_id = 1;
  optional string name = 2;
  optional string phone = 3;
}
{% endhighlight %}

In your code, define the base trait `DomainEvent` and include any subset of the fields:

{% highlight scala %}
package com.trueaccord.pb

trait BaseCustomer {
  def customerId: Option[String]
  def name: Option[String]
}
{% endhighlight %}

You can specify any number of base traits for a message.

It is also possible to make the generated companion classes extend a class
or trait, by using the `companion_extends` option. For example:

{% highlight proto %}
message MyMessage {
  option (scalapb.message).extends = "MySuperClass";
  option (scalapb.message).companion_extends = "MySuperCompanionClass";
  int32 n = 1;
}
{% endhighlight %}

Will generate a case class that extends `MySuperClass`, and the companion
object will extend `MySuperCompanionClass`.

# Custom types

You can customize the Scala type of any field.  One use-case for this is when
you would like to use type-safe wrappers around primitive values to enforce unit
correctness. For example, instead of using a raw integer for time fields, you can
wrap them in a `Seconds` class.

{% highlight proto %}
message Connection {
  optional int32 timeout = 1 [(scalapb.field).type = "mydomain.Seconds"];
}
{% endhighlight %}

We would like to write code like this:

{% highlight scala %}
val c = Connection().update(_.timeout := Seconds(5))
{% endhighlight %}

How will ScalaPB know how to convert from the original type (`Integer`) to the
custom type `Seconds`? For each custom type you need to define an implicit
`TypeMapper` that will tell ScalaPB how to convert between the custom type and
the base Scala type.  A good place to define this implicit is in the companion
class for your custom type, since the Scala compiler will look for a
typemapper there by default.  If your typemapper is defined elsewhere, you
will need to import it manually by using the `import` file-level option.

{% highlight scala %}
package mydomain

case class Seconds(v: Int) extends AnyVal

object Seconds {
  implicit val typeMapper = TypeMapper(Seconds.apply)(_.v)
}
{% endhighlight %}

`TypeMapper` takes two function parameters. The first converts from the original type to
the custom type. The second function converts from the custom type to the
original type. 

In addition to primitive values, you can customize enums and messages as well.

For more examples, see:

- [`custom_types.proto`](https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/main/protobuf/custom_types.proto)
- [`PersonId.scala`](https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/main/scala/com/trueaccord/pb/PersonId.scala)
- [`CustomTypesSpec.scala`](https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/test/scala/CustomTypesSpec.scala)

# Adding scalapb.proto to your project

The easiest way to get `protoc` to find `scalapb/scalapb.proto` when compiling
through SBT is by adding the following to your `build.sbt`:

    libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"

If you are invoking `protoc` manually, you will need to ensure that the files in
[`protobuf`](https://github.com/trueaccord/ScalaPB/tree/master/protobuf)
directory are available to your project.
