---
title: "Validating Protobufs"
sidebar_label: "Validation"
layout: docs
---

`scalapb-validate` is a code generator that generates validation methods for your messages based on rules and constraints defined in the proto. It uses the same validation rules provided by [protoc-gen-validate](https://github.com/envoyproxy/protoc-gen-validate).

## Introduction

In many situations, you may want to add validation rules for your messages. For example, you might want to enforce that a certain string field is an email address, or that a repeated field has at least one element.

Such rules and constraints can be defined using custom options defined in `validate/validate.proto`. Here is an example taken from [protoc-gen-validate's documentation](https://github.com/envoyproxy/protoc-gen-validate/blob/master/README.md).

```protobuf
syntax = "proto3";

package examplepb;

import "validate/validate.proto";

message Person {
  uint64 id    = 1 [(validate.rules).uint64.gt    = 999];

  string email = 2 [(validate.rules).string.email = true];

  string name  = 3 [(validate.rules).string = {
                      pattern:   "^[^[0-9]A-Za-z]+( [^[0-9]A-Za-z]+)*$",
                      max_bytes: 256,
                   }];

  Location home = 4 [(validate.rules).message.required = true];

  message Location {
    double lat = 1 [(validate.rules).double = { gte: -90,  lte: 90 }];
    double lng = 2 [(validate.rules).double = { gte: -180, lte: 180 }];
  }
}
```

scalapb-validate supports all the rules available in protoc-gen-validate and is tested with the same test harness containing over 900 test cases.

## Installation

Add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "@sbt_protoc@")

libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "compilerplugin"           % "@scalapb@",
    "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "@scalapb_validate@"
)
```

Change your `PB.targets` to generate the validation code. The output directory must be the same as the one used for `scalapb.gen`:

```scala
PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb",
  scalapb.validate.gen() -> (sourceManaged in Compile).value / "scalapb"
)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf"
)
```

Note that we are adding `scalapb-validate-core` as a `protobuf` dependency. This makes it possible to import `validate/validate.proto` from your own protos.

## Using the generated code

Generated code for both ScalaPB and scalapb-validate is generated at compilation time. In `sbt`, just type `compile`.

In addition to the standard ScalaPB generated files, scalapb-validate will generate a validator object for each message based on its protoc-gen-validate rules. For a message named `Msg` the validator object will be named `MsgValidator` and will extend `scalapb.validate.Validator[Msg]`. An implicit instance of the validator is added to the companion object of each message, which makes it possible to write `Validator[Msg]` to obtain an instance of the validator.

## Validators

The validator object is an object with a `validate` method that takes an instance of a message and returns the validation result: The `Validator[T]` is defined as follows:

```scala
trait Validator[T] {
  def validate(t: T): Result
}
```

where `Result` is a data structure that can be either a `Success` or a `Failure`:

```scala
sealed trait Result {
  def isSuccess: Boolean
  def isFailure: Boolean
}

case object Success extends Result { ... }

case class Failure(violations: List[ValidationException]) extends Result { ... }
```

Therefore, the validation for the test person casn be run like this:

```scala
Validator[Person].validate(personInstance) match {
  case Success             => println("Success!")
  case Failure(violations) => println(violations)
}
```