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

If [ScalaPB generator parameters](sbt-settings.md#additional-options-to-the-generator) are passed via `scalapb.gen(options: GeneratorOption*)`, the same parameters must be passed to `scalapb.validate.gen(options: GeneratorOption*)`.

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

## Rule-based type customization

:::note
The functionality described in the remainder of this page is available in a preview release of ScalaPB. It is still being refined and is subject to change without notice.
:::note

Starting from version 0.10.10, ScalaPB-validate provides a way to customize the generated ScalaPB types by writing rules. When these rules are matched, additional ScalaPB options are added to the matched entity. For example, you can create a transformation that whenever a field has a PGV-rule like `int32: { gt: 0 }}`, then it will be typemapped to a custom class `PositiveInt`.

### Installation

The minimum required versions of sbt-protoc is `1.0.0-RC6`. You will also need a preview version of
ScalaPB and ScalaPB-validate. Your `project/plugins.sbt` should have something like this:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.0-RC6")

libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "compilerplugin"           % "0.10.10-preview4",
    "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "0.2.0-preview4"
)
```

The key ingredient for type transformations is to have a *preprocessor* plugin running before ScalaPB,
letting it know which types to use. Your build.sbt should set `PB.targets in Compile` like this:

```scala
PB.targets in Compile := Seq(
  scalapb.validate.preprocessor() -> (sourceManaged in Compile).value / "scalapb",
  scalapb.gen() -> (sourceManaged in Compile).value / "scalapb",
  scalapb.validate.gen() -> (sourceManaged in Compile).value / "scalapb"
)

libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-validate-core" % scalapb.validate.compiler.BuildInfo.version % "protobuf",

  // If you are using Cats transformations:
  "com.thesamet.scalapb" %% "scalapb-validate-cats" % scalapb.validate.compiler.BuildInfo.version,
  "org.typelevel" %% "cats-core" % "2.3.0"
)
```

There is an example project [available on github](https://github.com/scalapb/scalapb-validate-demo).

### Field transformations

if you want all your positive integers to be typemapped to a Scala class called `PositiveInt` you can create a proto file with the following content:

```protobuf
syntax = "proto2";

package mypackage;

import "scalapb/scalapb.proto";
import "scalapb/validate.proto";
import "validate/validate.proto";

option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
};

option (scalapb.validate.package) = {
  field_transformations: [
    {
      when: {int32: {gt: 0}}
      set: {
        type: "mypkg.PositiveInt"
      }
    }
  ]
};
```

The scope of this definition is the entire protobuf package it is found in. Here, `field_transformations` is a list of `FieldTransformation` messages. Each of them describes a single rule. The `when` condition is a PGV `FieldRule` (defined in `validate/validate.proto`). When it is matched for any field in this package, the `scalapb.FieldOption` options in `set` are applied to the field.  Multiple transformations may match a single field. The order this happens is subject to change and should not be assumed.

The default matching mode is called `CONTAINS`. In this mode, the actual PGV rules for the field may contain additional additional options besides the one being matched on. Another matching mode is available and is called `EXACT`.  In this mode, the `when` pattern must match exactly the field's rules. Example syntax:

```protobuf
option (scalapb.validate.package) = {
  field_transformations: [
    {
      when: {int32: {gt: 0}}
      match_type: EXACT
      set: {
        type: "mypkg.PositiveInt"
      }
    }
  ]
};
```

## Cats non-empty collections

Using rules like the ones defined above, it is possible to detect when a list or a map are non-empty (via. `{repeated: { min_items: 1}}` or `{map: {min_pairs: 1}}`, and map them to corresponding non-empty collections.  Cats collections require some additional adaptation to ScalaPB since their API is different enough from standard Scala collections. ScalaPB comes with support to automatically map non-empty collections to `NonEmptyMap`, `NonEmptySet` and `NonEmptyList`. To enable, add the following to a proto file. The scope of the settings will be for the entire proto package:

```protobuf

syntax = "proto2";

package mypackage;

import "scalapb/scalapb.proto";
import "scalapb/validate.proto";
import "validate/validate.proto";

option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
};

option (scalapb.validate.package) = {
  cats_transforms: true
  unique_to_set: true
};
```

As stated above, you will need to have `scalapb-validate-cats` listed in
`libraryDependencies`. The setting `unique_to_set` can be used independently
of cats to transform a repeated with `unique: true` rule to a set.

## Package-scoped extension options

ScalaPB-validate further extends ScalaPB's package-scoped options to achieve additional customization:

```protobuf
syntax = "proto2";

package mypkg;

import "scalapb/scalapb.proto";
import "scalapb/validate.proto";

option (scalapb.options) = {
  scope: PACKAGE
  [scalapb.validate.file] {
      validate_at_construction: true
      insert_validator_instance: true
  }
};
```

- `validate_at_construction` when true, a check for validity is added to the message class body, so construction of invalid messages results in a validation exception. Default: `false`.
- `insert_validator_instance` when true, implicit instance of a `Validator` is added to the companion object of the message. This enables writing `Validator[MyMsg].validate(instance)`. Default: `true`.
