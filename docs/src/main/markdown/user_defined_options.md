---
title: "User-defined Options"
layout: docs
---

# Defining Custom Options

ScalaPB allows you to define custom options you can use to annotate any
element in a proto file, and access these annotations at run time.

Learn more about [custom options](https://developers.google.com/protocol-buffers/docs/proto#customoptions).


## Define your options

In `my_opts.proto`:

```protobuf
syntax = "proto2";

package custom_options;

import "google/protobuf/descriptor.proto";

extend google.protobuf.FileOptions {
  optional string my_file_option = 50000;
}

message MyMessageOption {
  optional int32 priority = 1;
}

extend google.protobuf.MessageOptions {
  optional MyMessageOption my_message_option = 50001;
}

message Tag {
  optional string name = 1;
}

// Extensions can be defined inside messages,
// But there is no relationship between the enclosing message and the
// extension - it only uses for namespace purposes.
message Wrapper {
  extend google.protobuf.FieldOptions {
    repeated Tag tags = 50003;
  }
}
```

Use the options like this:

```protobuf
syntax = "proto2";

package custom_options;

import "custom_options/my_opts.proto";

option (my_file_option) = "hello!";

message OneMessage {
    option (my_message_option).priority = 17;

    // Field level option, with repeated field.
    optional int32 number = 1 [(Wrapper.tags) = {name: "tag1"},
                               (Wrapper.tags) = {name: "tag2"}];
}
```

Extensions that are defined at file scope are generated under the descriptor
proto (usually name `FileNameProto`). Otherwise, the extension is defined in
the companion object of the containing class.

To access the option value of an element, you need to obtain its descriptor:

```scala
assert(
  my_opts.CustomOptionsMyOptsProto.myFileOption.get(
    use_opts.CustomOptionsUseOptsProto.scalaDescriptor.getOptions) ==
      Some("hello!"))

assert(
  my_opts.CustomOptionsMyOptsProto.myMessageOption.get(
    use_opts.OneMessage.scalaDescriptor.getOptions).get ==
      my_opts.MyMessageOption().update(_.priority := 17))

val numberField = use_opts.OneMessage.descriptor.findFieldByName(
  "number").get

assert(
  my_opts.Wrapper.tags.get(
    numberField.getOptions) == Seq(
      my_opts.Tag(name = Some("tag1")),
      my_opts.Tag(name = Some("tag2"))))
```

If you prefer to start with the descriptor, you use can the `extension`
method:

```scala
assert(use_opts.CustomOptionsUseOptsProto.scalaDescriptor.getOptions.extension(
  my_opts.CustomOptionsMyOptsProto.myFileOption) == Some("hello!"))

assert(use_opts.OneMessage.scalaDescriptor.getOptions.extension(
  my_opts.CustomOptionsMyOptsProto.myMessageOption).get ==
      my_opts.MyMessageOption().update(_.priority := 17))

assert(numberField.getOptions.extension(
  my_opts.Wrapper.tags) == Seq(
      my_opts.Tag(name = Some("tag1")),
      my_opts.Tag(name = Some("tag2"))))
```

## Example code

The full source code of this example is available below:

- [Protobufs](https://github.com/scalapb/ScalaPB/tree/master/examples/src/main/protobuf/custom_options)
- [Scala](https://github.com/scalapb/ScalaPB/tree/master/examples/src/main/scala/custom_options)

