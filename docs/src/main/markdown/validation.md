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

## Rule-based type customization

Starting from version 0.10.10, ScalaPB provides a way to customize its own options by writing rules that are matched against
arbitrary protobuf options. When these rules are matched, additional ScalaPB options are added to the matched entity. For example, you can create a transformation that whenever a field has a PGV-rule like `int32: { gt: 0 }}`, then it will be typemapped to a custom class `PositiveInt`.

### Installation

The features described in this section have the following version requirements:
* sbt-protoc >= `1.0.0`
* ScalaPB >= `0.10.10`
* scalapb-validate >= `0.2.0`

While field transformation is a generic ScalaPB mechanism, it is also recommended that you add
scalapb-validate's preprocessor to `PB.targets`. The preprocessor does two things:
1. Provides field transformations for `Set` and cats data types.
2. Expand your PGV-based rules such that they match repeated items, map keys and map values.

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

if you want all positive integers to be typemapped to a Scala class called `PositiveInt` you can create a proto file with the following content:

```protobuf
syntax = "proto2";

package mypackage;

import "scalapb/scalapb.proto";
import "scalapb/validate.proto";
import "validate/validate.proto";

option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
  field_transformations: [
    {
      when: {options [ [validate.rules] {int32: {gt: 0}} ]}
      set: {
        [scalapb.field] {
          type: "mypkg.PositiveInt"
        }
      }
    }
  ]
};
```

The scope of this definition is the entire protobuf file it is found in. Field tranformations can also
be used in package-scoped options so they are passed to all files within the package.

Here, `field_transformations` is a list of `FieldTransformation` messages. Each of them describes a single rule. The `when` condition is `google.protobuf.FieldDescriptorProto` message embedding a PGV `FieldRule` (defined in `validate/validate.proto`). When the rule is matched for any field in this file, the `google.protobuf.FieldOptions` options in `set` are applied to the field. Currently, only `[scalapb.field]` options  may appear in the `set` field. Multiple transformations may match a single field. The transformations from parent packages are applied first in descending order, that is from the outermost package to the package where the field resides. Within each package, the transformations are applied in the order they appear in the file. Options defined locally at the field-level are applied last.

There are three matching modes available:
* `CONTAINS` is the default matching mode. In this mode, the preprocessor checks that all the options in the `when` pattern are defined on the field and having the same value. Additional options may be defined on the field besides the ones on the `when` pattern.
* `EXACT` is a strict equality comparison between the `when` pattern and the field's rules.
* `PRESENCE` checks whether every field that is present on the `when` pattern is also present on the field's rules. The specific value the option has is not compared. This allows matching on any value. For example, `{int32: {gt: 1}}` would match for any number assigned to `int32.gt`.

Example syntax:

The following rule with match whenever there is a `gt` field set, no matter to which value:
```protobuf
option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
  field_transformations: [
    {
      when: {options { [validate.rules] {int32: {gt: 0}}}}
      match_type: PRESENCE
      set: {
        [scalapb.field] {
          type: "mypkg.GreaterThanAnything"
        }
      }
    }
  ]
};
```

Since the `when` clause is `FieldDescriptorProto`, it is possible to match on `type` and `label`. For example,
the following will match only when the field is in a `repeated int32`:

```protobuf
option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
  field_transformations: [
    {
      when: {
        type: TYPE_INT32
        label: LABEL_REPEATED
        options { [validate.rules] {int32: {gt: 0}}}
      }
      match_type: CONTAINS
      set: {
        [scalapb.field] {
          type: "mypkg.PositiveIntInRepeated"
        }
      }
    }
  ]
};
```

#### What does the preprocessor do?

The preprocessor scans for `field_transformations` with `when` fields that contain `[validate.rules]` extensions. Whenever a `[validate.rules]` does contain a `repeated` or `map` validation rules it is assumed to be applied to a singleton type, so we add a copy of the rule for repeated, map-key and map-value context. For example, for this `PositiveInt` rule:

```protobuf
{
  when: {[validate.rules] {int32: {gt: 0}}}
  set: {
    type: "mypkg.PositiveInt"
  }
}
```

the following field transformations will be automatically added by the preprocessor:
```
    {
      when: {options{[validate.rules] {repeated: {int32: {gt: 0}}}}}
      set: {
        [scalapb.field] {
          type: "mypkg.PositiveInt"
        }
      }
    },
    {
      when: {options{[validate.rules] {map: {keys: {int32: {gt: 0}}}}}}
      set: {
        [scalapb.field] {
          key_type: "mypkg.PositiveInt"
        }
      }
    },
    {
      when: {options{[validate.rules] {map: {values: {int32: {gt: 0}}}}}}
      set: {
        [scalapb.field] {
          value_type: "mypkg.PositiveInt"
        }
      }
    }
```

This saves you from writing those rules manually so the type transformation is applied in repeated fields or maps. Note that the rewrite mechanism rewrites the `type` in the original `set` field, into `key_type` or `value_type`.

### Cats non-empty collections

Using rules like the ones defined above, it is possible to detect when a list or a map are non-empty (via. `{repeated: { min_items: 1}}` or `{map: {min_pairs: 1}}`, and map them to corresponding non-empty collections.  Cats collections require some additional adaptation to ScalaPB since their API is different enough from standard Scala collections. ScalaPB-validate comes with support to automatically map non-empty collections to `NonEmptyMap`, `NonEmptySet` and `NonEmptyList`. To enable, add the following to a proto file. The scope of the settings will be for the entire file. You can turn the setting on for the
entire package by adding `scope: PACKAGE`.

```protobuf

syntax = "proto2";

package mypackage;

import "scalapb/scalapb.proto";
import "scalapb/validate.proto";

option (scalapb.options) = {
  preprocessors: ["scalapb-validate-preprocessor"]
  [scalapb.validate.file] {
    validate_at_construction : true
    cats_transforms : true
    unique_to_set : true
  }
};
```

As stated above, you will need to have `scalapb-validate-cats` listed in
`libraryDependencies`. The setting `unique_to_set` can be used independently
of cats to transform a repeated with `unique: true` rule to a set.

### Unboxing required fields

If you use `validate.message.required` you can apply a transformation that
would set the `scalapb.field.required` option. As a result, the field will
not be boxed in an `Option` and parsing will throw an exception if the field
is missing. To set this transformation add the following to ScalaPB-validate's options:

```protobuf
option (scalapb.options) = {
    field_transformations: [
        {
            when: {options: {[validate.rules] {message: {required: true}}}}
            set: {
              [scalapb.field] {
                required: true
              }
            }
        }
    ]
};
```

### Referencing rules values

It is possible to reference values in the rules and use them
on the `set` part. Whenever there is a singular string field  in Scala options, the preprocessor would replace tokens in the format `$(p)` with the value of the field's option at the path `p`, relative to the
`FieldDescriptorProto` of the field. To reference extension fields, wrap the extension full name in brackets (`[]`). For example, `$(options.[validate.rules].int32.gt)` would be substituted with the value of that option on the field. If the option is not set on the field, a default value will be replaced (0 for numeric types, empty string, and so on).

The paths that are referenced don't have to appear on the `when` pattern. While referencing rule values is useful when the matching mode is `PRESENCE`, it is supported to reference rule values in all matching modes.

A possible application for this is in conjunction with [refined types](https://github.com/fthomas/refined). For example, you can define the following field transformations:

```protobuf
syntax = "proto3";

package refined_test;

import "validate/validate.proto";
import "scalapb/validate.proto";
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  preprocessors : [ "scalapb-validate-preprocessor" ]
  import : "eu.timepit.refined.api.Refined"
  import : "eu.timepit.refined.numeric._"
  import : "eu.timepit.refined.generic._"
  import : "shapeless.{Witness => W}"

  field_transformations : [ {
    when : {options: {[validate.rules] {int32 : {gt : 1}}}}  // <-- 1 can be replaced with any number
    set : {type : "Int Refined Greater[$(options.[validate.rules].int32.gt)]"}
    match_type : PRESENCE
  } ]
};

message Test {
  int32 gt_test = 1 [ (validate.rules).int32 = {gt : 5} ];  // transformed to: Int Refined Greater[5]
}
```

For this to work, a typemapper for refined types need to be either put in a package object in the same package where the code is generated, or be manually imported through `import` options.

The typemapper used in scalapb-validate tests is [here](https://github.com/scalapb/scalapb-validate/blob/0.2.x-preview/e2e/src/main/scala/scalapb/transforms/refined/package.scala).

Additional resources:
* [test proto files (refined.proto)](https://github.com/scalapb/scalapb-validate/blob/0.2.x-preview/e2e/src/main/protobuf/transforms/refined/refined.proto): note it uses `shapeless.Witness` since ScalaPB-validate is cross-tested for Scala 2.12 and Scala 2.13.
* [end-to-end tests](https://github.com/scalapb/scalapb-validate/blob/0.2.x-preview/e2e/src/test/scala/scalapb/validate/transforms/refined/RefinedSpec.scala) demonstrating compile-time validation.

