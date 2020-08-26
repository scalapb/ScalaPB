---
title: "Sealed oneofs"
---

**Note: sealed oneofs are available only in ScalaPB 0.8 or later. The API and rules are considered experimental. See note at the bottom. **

Sealed oneofs are a subset of oneofs for which ScalaPB generates idiomatic data types for.

## Example

```protobuf
message Expr {
    oneof sealed_value {
        Literal lit = 1;
        Add add = 2;
        Mul mul = 3;
    }
}

message Literal {
    int32 value = 1;
}

message Add {
    Expr left = 1;
    Expr right = 2;
}

message Mul {
    Expr left = 1;
    Expr right = 2;
}

message Program {
    repeated Expr exprs = 1;
}
```

Note that the name of the oneof name is `sealed_value`. This is what makes ScalaPB treat `Expr` as a sealed oneof and generate code similar to the following:

```scala
sealed trait Expr {
    def isEmpty: Boolean
    def isDefined: Boolean
    def asMessage: ExprMessage  // converts to the standard representation
}

object Expr {
    case object Empty extends Expr
}

case class Literal(value: Int) extends Expr with GeneratedMessage

case class Add(left: Expr, right: Expr) extends Expr with GeneratedMessage

case class Mul(left: Expr, right: Expr) extends Expr with GeneratedMessage

case class Programs(exprs: Seq[Expr]) extends GeneratedMessage

// Standard case class for the Expr message, containing the
// default style of the code that gets generated for oneofs.
case class ExprMessage(sealedValue: ExprMessage.SealedValue)
```

The above Scala representation of the protocol buffer is much nicer to work with than the default oneof representation, as there are no wrapper types around each individual case of the oneof. Also note that optional fields of type `Expr` are not boxed inside an `Option` since we have `Expr.Empty` that represents the empty case.

## Sealed oneof rules

A sealed oneof is detected when a message (denoted below as the *containing message*) contains a oneof named `sealed_value`. The code generator checks the following rules for each sealed oneof. If any of these rules fail, then the code generator aborts.

1. The oneof named `sealed_value` must be the only oneof in the containing message.

2. No additional fields (except those inside `sealed_values`) may be defined inside the containing message.

3. No nested messages or enums are allowed to be defined in the containing message.

4. The containing message must be a top-level message.

5. All the oneof cases must be distinct top-level message types that are defined in the same file as the sealed oneof.

6. A message type can appear in at most one sealed oneof.

## Experimental Status

Some of the rules above are inherently required (for example, that the message types need to be distinct). Other rules, such as the one requesting that all involved messages need to be top-level, were added to make the implementation simpler. That particular rule helps ensuring that all the cases can be generated into a single Scala source file without changing too much the existing way the code generator works. It is possible that some of the rules will change over time, though most likely they are only going to become less restrictive so existing code does not break.

Currently, sealed oneofs are implemented as a custom type defined over the old-style container message. This implementation detail is exposed through `asMessage` which returns the underlying message representing the sealed oneof.  It is possible that in a future version, sealed oneofs would have a direct implementation, and therefore `asMessage` and its return type should be considered an experimental API.

## Optional sealed oneof

This is a variant of _Sealed oneof_, where the optionality is expressed differently.
The `Empty` case is not generated, but instead the sealed trait is put in other classes as `Option[_]`, not directly.
To create an optional sealed oneof, name the oneof sealed_value_optional as in the example below:

```protobuf
message Expr {
    oneof sealed_value_optional {
        Literal lit = 1;
        Add add = 2;
        Mul mul = 3;
    }
}
```

```scala
sealed trait Expr {
    def isEmpty: Boolean
    def isDefined: Boolean
    def asMessage: ExprMessage  // converts to the standard representation
}

case class Literal(value: Int) extends Expr with GeneratedMessage

case class Add(left: Option[Expr], right: Option[Expr]) extends Expr with GeneratedMessage

case class Mul(left: Option[Expr], right: Option[Expr]) extends Expr with GeneratedMessage

case class Programs(exprs: Seq[Option[Expr]]) extends GeneratedMessage
```
