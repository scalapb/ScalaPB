---
title: "Generated Code"
---

This page describes the code generated by ScalaPB and how to use it.

## Default Package Structure

The generator will create a Scala file for each top-level message and enum
in your proto file. Using multiple files results in a better incremental
compilation performance.

The Scala package of the generated files will be determined as follows:

- If the `java_package` option is defined, then the Scala package will be
  `java_package.base_name` where `base_name` is the name of the proto file
  without the `.proto` extension.
- If the `java_package` is undefined, but the file specifies a package name
  via the `package` keyword then the Scala package will be
  `package.base_name`.
- If neither `java_package` nor `package` are specified, the Scala package
  will be just `base_name`.

From version 0.4.2, there is a way to customize the package name by using
[file-level options](customizations.md).

## Messages

Each message corresponds to a final case class and a companion object.
Optional fields are wrapped in an `Option[]`, repeated fields are given as
`Seq[]`, and required fields (which you should not use, see "Required is
forever" in the [Language
Guide](https://developers.google.com/protocol-buffers/docs/proto#simple) are
normal members.

Note that in proto3, scalar (non-message) fields are not wrapped in `Option`,

For example, if your protocol buffer looks like this:

```protobuf
message Person {
    optional string name = 1;
    optional int32 age = 2;

    repeated Address addresses = 3;
}

message Address {
  optional string street = 1;
  optional string city = 2;
}
```

...then the compiler will generate code that looks like this:

```scala
final case class Person(
    name: Option[String] = None,
    age: Option[Int] = None,
    addresses: Seq[Address] = Nil, ...) extends GeneratedMessage {

    def toByteArray: Array[Byte] = { ... }

    // more stuff...
}

object Person extends GeneratedMessageCompanion[Person] {
    def parseFrom(bytes: Array[Byte]): Person = { ... }

    // more stuff...
}

// similar stuff for Address...
```

The case class contains various methods for serialization, and the companion
object contains method for parsing. [See the source code for
`GeneratedMessage` and
`GeneratedMessageCompanion`](https://github.com/scalapb/ScalaPB/blob/master/scalapb-runtime/src/main/scala/scalapb/GeneratedMessageCompanion.scala)
to see what methods are available

### Building New Messages

Create a new instance of a message by calling the constructor (as you normally
would for a case class):

```scala
val p1 = Person()

val p2 = Person(name = Some("John"))
```

When constructing messages, it is advised to use named arguments
Person(**name** = x, **age** = y) to ensure your code does not rely on the
order of the fields in the protocol buffer definition.

### Updating Messages

Messages are immutables: once you created a message instance it can not be
changed. Messages are thread-safe: you can access the same message instance
from multiple threads.

When you want to modify a message, simply create a new one based on the
original. You can use the `copy()` method Scala provides for all case classes,
however ScalaPB provides additional methods to make it even easier.

The first method is using a `withX()` method, where `X` is a name of a field.
For example

```scala
val p = Person().withName("John").withAge(29)
```

Note that when using the `withX()` method on an optional field, you do not
need to provide the `Some()`.

Another way to update a message is using the `update()` method:

```scala
val p = Person().update(
  _.name := "John",
  _.age := 29
)
```

The `update()` method takes "mutations" (like the assignments above), and
applies them on the object.  Using the `update()` method, as we will see
below, usually results in a more concise code, especially when your fields are
message types.

## Optional Fields

Optional fields are wrapped inside an `Option`.  The compiler will generate a
`getX()` method that will return the option's value if it is set, or a
default value for the field if it is unset (that is, if it is `None`)

There are two ways to update an optional field using the `update()` method:

```scala
val p = Person().update(
  // Pass the value directly:
  _.name := "John",

  // Use the optionalX prefix and pass an Option:
  _.optionalAge := Some(37)  // ...or None
)
```

The first way sets the field with a value, the second way lets you pass an
`Option[]` so it is possible to set the field to `None`.

For each optional field `X`, the compiler also generates a `clearX()` method
that returns a new instance of the message which is identical to the original
one except that the field is assigned the value `None`.

## Required Fields

Required fields have no default value in the generated constructor, so you
must specify them when you instantiate a new message. Differences from
optional fields:

- The value of the field is not wrapped inside an `Option`.
- There is no `getX` method (you can always use `.x`)
- There is no `clearX` method (since that will result in an invalid message)
- There is no `_.optionalX` lens for the `update()` method.

## Repeated Fields

Repeated fields are provided as a `Seq[T]`. The compiler will generate the
following methods:

- `addFoo(f1, [f2, f3, ...]: Foo)`: returns a copy with the given elements added to the
  original list.
- `addAllFoo(fs: Seq[Foo])`: returns a copy with the given sequence of
  elements added to the original list.
- `withX(otherList)`: replace the sequence with another.
- `clearX`: replace with the sequence with an empty one.

Using `update()` is especially fun with repeated fields:

```scala
val p = Person().update(
  // Override the addresses
  _.addresses := newListOfAddress,

  // Add one address:
  _.addresses :+= address1,

  // Add a list of addresses:
  _.addresses :++= Seq(address1, address2),

  // Modify an address in the list by index!
  _.addresses(1).street := "Townsend St.",

  // Modify all addresses:
  _.addresses.foreach(_.city := "San Francisco"),

  // Apply a transformation to all addresses (this is
  // just for showing off, it is not specific for
  // repeatables - it happens in the nested mutations)
  _.addresses.foreach(_.city.modify(_.trim))
)
```

## Oneof Fields

Oneofs are great when you model a message that has multiple disjoint cases. An
example use case would be:

```protobuf
// Represent a payment by credit card
message CreditCardPayment {
    optional string last4 = 1;
    optional int32 expiration_month = 2;
    optional int32 expiration_year = 3;
}

// Represent a payment by bank transfer
message BankTransferPayment {
    optional string routing_number = 1;
    optional string account_number = 2;
}

// Represents an order placed by a customer:
message Order {
    optional int32 amount = 1;
    optional string customer_id = 2;

    // How did we get paid? At most one option must be set.
    oneof payment_type {
        CreditCardPayment credit_card = 3;
        BankTransferPayment bank = 4;
    }
}

```

The compiler will generate code that looks like this:

```scala
final case class CreditCardPayment { ... }
final case class BankTransfer { ... }

case class Order(..., paymentType: Payment.PaymentType) {
    // Set the payment type to a specific case:
    def withCreditCard(v: CreditCardPayment): Order
    def withBank(v: BankTransferPayment): Order

    // Sets the entire payment type to a new value:
    def withPaymentType(v: PaymentType): Order

    // Sets the PaymentType to Empty
    def clearPaymentType: Order
}

object Order {
    sealed trait PaymentType {
        def isEmpty: Boolean
        def isDefined: Boolean
        def isCreditCard: Boolean
        def isBank: Boolean
        def creditCard: Option[CreditCardPayment]
        def bank: Option[BankTransferPayment]
    }

    case object Empty extends PaymentType

    case class CreditCard(v: CreditCardPayment) extends PaymentType

    case class Bank(v: CreditCardPayment) extends PaymentType
}
```

This enables writing coding like this:

```scala
val o1 = Order()
  .withCreditCard(CreditCardPayment(last4 = Some("4848")))

// which is equivalent to:
val o2 = Order().update(
  _.creditCard.last4 := "4848")

// This changes the payment type to a bank, so the credit card data is
// not reachable any more through o3.
val o3 = o1.update(_.bank.routingNumber := "333")

if (o3.paymentType.isBank) {
  // Do something useful.
}

// Pattern matching:
import Order.PaymentType
o3.paymentType match {
    case PaymentType.CreditCard(cc) =>  // handle cc
    case PaymentType.Bank(b) =>  // handle b
    case PaymentType.Empty =>  // handle exceptional case...
}

// The one of values are available as Option too:
val maybeRoutingNumber: Option[String] = o3.paymentType.bank.map {
    b => b.routingNumber
}
```

## Enumerations

Enumerations are implemented using sealed traits that extend `GeneratedEnum`.
This approach, rather than using Scala's standard Enumeration type, allows
getting a warning from the Scala compiler when a pattern match is incomplete.

For a definition like:

```protobuf
enum Weather {
    SUNNY = 1;
    PARTLY_CLOUDY = 2;
    RAIN = 3;
}

message Forecast {
    optional Weather weather = 1;
}
```

The compiler will generate:

```scala
sealed trait Weather extends GeneratedEnum {
    def isSunny: Boolean
    def isPartlyCloudy: Boolean
    def isRain: Boolean
}

object Weather extends GeneratedEnumCompanion[Weather] {
    case object SUNNY extends Weather {
        val value = 1
        val name = "SUNNY"
    }

    // Similarly for the other enum values...
    case object PARTLY_CLOUDY extends Weather { ... }
    case object RAIN extends Weather { ... }

    // In ScalaPB >= 0.5.x, this captures unknown value that are received
    // from the wire format.  Earlier versions throw a MatchError when
    // this happens.
    case class Unrecognized(value: Int) extends Weather { ... }

    // And a list of all possible values:
    lazy val values = Seq(SUNNY, PARTLY_CLOUDY, RAIN)
}

case class Forecast(weather: Option[Weather]) { ... }
```

And we can write:

```scala
val f = Forecast().update(_.weather := Weather.PARTLY_CLOUDY)

assert(f.weather == Some(Weather.PARTLY_CLOUDY)

if (f.getWeather.isRain) {
    // take an umbrella
}

// Pattern matching:
f.getWeather match {
    case Weather.RAIN =>
    case Weather.SUNNY =>
    case _ =>
}

```

## ASCII Representation

Each message case-class has `toProtoString` method that returns a string
representation of the message in an ASCII format. The ASCII format can be
parsed back by the `fromAscii()` method available on the companion object.

That format is not officially documented, but at least the standard Python,
Java and C++ implementations of protobuf attempt to generate (and be able to parse)
compatible ASCII representations. ScalaPB's `toString()` and `fromAscii`
follow the Java implementation.

The format looks like this:

```protobuf
int_field: 17
string_field: "foo"
repeated_string_field: "foo"
repeated_string_field: "bar"
message_field {
  field1: "value1"
  color_enum: BLUE
}
```

This format can be useful for debugging or for transient data processing, but
beware of persisting these ASCII representations: unknown fields throw an
exception, and unlike the binary format, the ASCII format is senstitive to
renames.

## Java Conversions

If you are dealing with legacy Java protocol buffer code, while still wanting
to write new code using ScalaPB, it can be useful to generate converters
to/from the Java protocol buffers. To do this, set `Compile / PB.targets`
like this in your `build.sbt`:

```scala
Compile / PB.targets := Seq(
  PB.gens.java -> (Compile / sourceManaged).value,
  scalapb.gen(javaConversions=true) -> (Compile / sourceManaged).value
)
```


This will result in the following changes:

- The companion object for each message will have `fromJavaProto` and
  `toJavaProto` methods.
- The companion object for enums will have `fromJavaValue` and
  `toJavaValue` methods.
