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

// For ScalaPB 0.11.x:
libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "compilerplugin"           % "@scalapb@",
    "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "@scalapb_validate@"
)

// For ScalaPB 0.10.x:
libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "compilerplugin"           % "0.10.11",
    "com.thesamet.scalapb" %% "scalapb-validate-codegen" % "0.2.2"
)
```

Change your `PB.targets` to generate the validation code. The output directory must be the same as the one used for `scalapb.gen`:

```scala
Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb",
  scalapb.validate.gen() -> (Compile / sourceManaged).value / "scalapb"
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
      skip: false
  }
};
```

- `validate_at_construction` when true, a check for validity is added to the message class body, so construction of invalid messages results in a validation exception. Default: `false`.
- `insert_validator_instance` when true, implicit instance of a `Validator` is added to the companion object of the message. This enables writing `Validator[MyMsg].validate(instance)`. Default: `true`.
- `skip` when true, skips gnerating validators for messages defined in this file. This can be set for a third-party package to work around the problem that there are no validators for it. See [this testcase](https://github.com/scalapb/scalapb-validate/tree/master/e2e/src/main/protobuf/skip) for a usage example.

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
Compile / PB.targets := Seq(
  scalapb.validate.preprocessor() -> (Compile / sourceManaged).value / "scalapb",
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb",
  scalapb.validate.gen() -> (Compile / sourceManaged).value / "scalapb"
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

If you want all positive integers to be typemapped to a Scala class called `PositiveInt` you can
create a proto file with the following content:

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
      when: {options { [validate.rules] {int32: {gt: 0}} }}
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

You can learn more about [field transformations in this page](transformations.md).

More examples of field transformations usage:

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

### Using with refined

As explained in ["referencing rule values"](transformations.md#referencing-rules-values), it is possible
to reference field descriptor values in the `set` part of field transformation.
A use case for this is with [refined types](https://github.com/fthomas/refined). For example, you can
 define the following field transformations:

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
    set : {[scalapb.field] {type : "Int Refined Greater[$(options.[validate.rules].int32.gt)]"}}
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

