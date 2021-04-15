---
title: "Transformations"
layout: docs
---

## Introduction

Field transformations were introduced in ScalaPB 0.10.10 and allow you to automatically apply ScalaPB field-level options when a given field match certain conditions. In the future, we expect to have transformations for additional protobuf entities.

This document assumes that you are already familiar with:
* [Protocol Buffer custom options](https://developers.google.com/protocol-buffers/docs/proto#customoptions).
* [ScalaPB customizations](customizations.md) and type mappers.
* Protobuf Descriptors, and specifically [FieldDescriptorProto](https://github.com/protocolbuffers/protobuf/blob/48234f5f012582843bb476ee3afef36cda94cb66/src/google/protobuf/descriptor.proto#L138-L239).

## Example use-case

Assume that your project uses a custom option called `sensitive`, and whenever this option is set to `true` on a `string` field, you would ScalaPB to use a custom type, `SensitiveString` instead of a standard `String`.

The custom option definition could look like this:

```protobuf
// opts.proto
syntax = "proto2";

package mypkg;

import "google/protobuf/descriptor.proto";

extend google.protobuf.FieldOptions {
  optional MyCustomOptions opts = 50001;
}

message MyCustomOptions {
  optional bool  sensitive = 1;
  optional int32 num       = 2;
}
```

Example usage of the custom options:

```protobuf
// usage.proto

syntax = "proto3";

package mypkg;

import "opts.proto";

message User {
  string secret = 1 [(mypkg.opts).sensitive = true];
}
```

We want the type of `secret` in the case class to be `SensitiveString`. We could manually set
`(scalapb.field).type` to `SensitiveString`:

```protobuf
message User {
  string secret = 1 [(mypkg.opts).sensitive = true,
                     (scalapb.field).type = "mypkg.SensitiveString"];
}
```

but it would be nice if there was a way to automatically apply `(scalapb.field).type` automatically whenever `sensitive` was set to true on a `string` field. This is the problem field transformations are set to solve:

```protobuf
// Usage.proto:
syntax = "proto3";

import "scalapb/scalapb.proto";
import "opts.proto";

option (scalapb.options) = {
  field_transformations : [
    {
      when : {
        options {
          [mypkg.opts]{sensitive : true}
        }
        type: TYPE_STRING
      }
      set : {[scalapb.field] {type : 'mypkg.SensitiveString'}}
    }
}

message User {
  string secret = 1 [(mypkg.opts).sensitive = true];
}
```

The transformation above matches when the custom option `senstive` is true, and the field type
is `string`. When it matches, it sets the ScalaPB option `type`.

## Syntax

FieldTransformations are defined in [scalapb.proto](https://github.com/scalapb/ScalaPB/blob/master/protobuf/scalapb/scalapb.proto):

```protobuf
enum MatchType {
  CONTAINS = 0;
  EXACT = 1;
  PRESENCE = 2;
}

message FieldTransformation {
  optional google.protobuf.FieldDescriptorProto when = 1;
  optional MatchType match_type = 2 [default=CONTAINS];
  optional google.protobuf.FieldOptions set = 3;
}
```

ScalaPB has a file-level option `field_transformations` which is a `repeated FieldTransformation`. The scope
of the field transformations is the same proto-file, and can be passed down to the entire package as a package-scoped
option (when `scope: PACKAGE` option is set).

A field transformation matches on the `when` condition which a [FieldDescriptorProto](https://github.com/protocolbuffers/protobuf/blob/48234f5f012582843bb476ee3afef36cda94cb66/src/google/protobuf/descriptor.proto#L138-L239). This allows it to match on the field's type, or label (`LABEL_REPEATED`, `LABEL_OPTIONAL`, `LABEL_REQUIRED`), as well as on custom options like in the previous example. There are few matching modes that are described below and can be selected using `match_type`. The `set` field tells ScalaPB what options to apply to the field if the rule conditions match. Currently, only `[scalapb.field]` options  may appear in the `set` field.

There are three matching modes available:
* `CONTAINS` is the default matching mode. In this mode, ScalaPB checks that all the options in the `when` pattern are defined on the field descriptor and having the same value. Additional fields may be defined on the field besides the ones on the `when` pattern.
* `EXACT` is a strict equality comparison between the `when` pattern and the field descriptor.
* `PRESENCE` checks whether every field that is present on the `when` pattern is also present on the field's rules. The specific value the option has is not compared. This allows matching on any value. For example, `{int32: {gt: 1}}` would match for any number assigned to `int32.gt`.

### Referencing rules values

It is possible to reference values in the rules and use them on the `set` part. Whenever there is a singular string field under the field descriptor, ScalaPB would replace tokens in the format `$(p)` with the value of the field's option at the path `p`, relative to the `FieldDescriptorProto` of the field. To reference extension fields, wrap the extension full name in brackets (`[]`). For example, `$(options.[pkg.opts].num)` would be substituted with the value of that option on the field. If the option is not set on the field, a default value will be replaced (0 for numeric types, empty string, and so on).

The paths that are referenced don't have to appear on the `when` pattern. While referencing rule values is useful when the matching mode is `PRESENCE`, it is supported to reference rule values in all matching modes.

One application for this is in conjunction with [refined types](https://github.com/fthomas/refined). See example in [ScalaPB validate documentation](validation.md#using-with-refined).

## Example: customizing third-party types

When you want to customize your own messages, ScalaPB lets you add [custom
options](customizations.md#message-level-custom-type-and-boxing) within the message is defined. You may also want to apply customizations to types defined in third-party protos which you can not change. To accomplish that, we can use field transformations. In the following example, we match on `google.protobuf.Timestamp` and map it to a custom type. In `src/main/protobuf/myexample/options.proto`:

```protobuf
syntax = "proto2";

package com.e;

import "scalapb/scalapb.proto";

option (scalapb.options) = {
  scope: PACKAGE
  field_transformations : [
    {
      when : {
        type: TYPE_MESSAGE
        type_name: ".google.protobuf.Timestamp"
      }
      set : {[scalapb.field] {type : 'com.myexample.MyType' }}
    }
  ]
};
```

:::note
Note the `.` (dot) prefix in the `type_name` field above. It is needed as [explained here](https://github.com/protocolbuffers/protobuf/blob/68cb69ea68822d96eee6d6104463edf85e70d689/src/google/protobuf/descriptor.proto#L187-L192). In this example we assume the user's package is not named `google` or `google.protobuf` since then `type_name` could be relative and would not match.
:::

Now, we need to make sure there is an implicit typemapper converting between `google.protobuf.timestamp.Timestamp` and `com.myexample.MyType`. The typemapper can be defined in the companion object of `MyType` as exampled in [custom types](customizations.md#custom-types).
