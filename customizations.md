---
title: "ScalaPB: Customizations"
layout: page
redirect_from: "/file-options.html"
---

# ScalaPB File-level Options

ScalaPB file-level options lets you

- specify the Scala package to use, independently of the Java package option.
- request that ScalaPB will not append the protofile name to the package name.
- specify Scala imports so custom base traits and custom types (see below) do
  not require the full class name.

The file-level options are not required, unless you are interested in those
customizations. If you do not want to customize the defaults, you can safely
skip this section.

## Supported options

{% highlight proto %}
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  package_name: "com.example.myprotos"
  flat_package: true
  import: "com.trueaccord.pb.MyType"
  import: "com.trueaccord.other._"
};
{% endhighlight %}

- `package_name` sets the Scala base package name, if this is not defined,
then it falls back to `java_package` and then to `package`. 

- Setting `flat_package` to true (default is `false`) makes ScalaPB not append
the protofile base name to the package name.  You can also apply this option
globally to all files by adding it to your [ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

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

# Custom types

You can customize the Scala type of any field.  One use case for this is when
you want to wrap a primitive value in a class that enforce unit correctness in
 a type-safe way. For example:

{% highlight proto %}
message Weather {
  optional float temprature = 1 [(scalapb.field).type = "mydomain.Fahrenheit"];
  optional int32 wind_speed = 2 [(scalapb.field).type = "mydomain.Mph"];
}
{% endhighlight %}

For each custom type you need to define an implicit `TypeMapper` that will tell
ScalaPB how to convert between the custom type to the base Scala type.  A good
place to define this implicit is in the companion class for your custom type,
since the Scala compiler will look for a typemapper there by default.  If your typemapper is defined elsewhere, you will need to import it manually by using the `import` file-level option.

{% highlight scala %}
package mydomain

case class Fahrenheit(v: Float) extends AnyVal

case class Mph(speed: Int) extends AnyVal

object Fahrenheight {
  implicit val typeMapper = TypeMapper(Fahrenheit.apply)(_.v)
}

object Mph {
  implicit val typeMapper = TypeMapper(Mph.apply)(_.speed)
}
{% endhighlight %}

In addition to privite values, you can customize enums and messages as well.

For more examples, see:
- https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/main/protobuf/custom_types.proto
- https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/main/scala/com/trueaccord/pb/PersonId.scala
- https://github.com/trueaccord/ScalaPB/blob/master/e2e/src/test/scala/CustomTypesSpec.scala

# Adding scalapb.proto to your project

The easiest way to get `protoc` to find `scalapb/scalapb.proto` when compiling
through SBT is by adding the following to your `build.sbt`:

    libraryDepenencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "{{site.data.version.scalapb}}" % PB.protobufConfig

If you are invoking `protoc` manually, you will need to ensure that the files in
[`protobuf`](https://github.com/trueaccord/ScalaPB/tree/master/protobuf)
directory are available to your project.
