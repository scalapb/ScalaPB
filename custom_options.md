---
title: "ScalaPB: Custom Options"
layout: page
---

# Defining Custom Options

ScalaPB allows you to define custom options you can use to annotate any
element in a proto file, and access these annotations at run time.

Learn more about [custom options](https://developers.google.com/protocol-buffers/docs/proto#customoptions).


## Define your options

In `my_opts.proto`:

{% highlight proto %}
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
{% endhighlight %}

Use the options like this:

{% highlight proto %}
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
{% endhighlight %}

Extensions that are defined at file scope are generated under the descriptor
proto (usually name `FileNameProto`). Otherwise, the extension is defined in
the companion object of the containing class.

To access the option value of an element, you need to obtain its descriptor:

{% highlight scala %}
assert(
  my_opts.CustomOptionsMyOptsProto.myFileOption.get(
    use_opts.CustomOptionsUseOptsProto.descriptor.getOptions) == Some("hello!"))

assert(
  my_opts.CustomOptionsMyOptsProto.myMessageOption.get(
    use_opts.OneMessage.descriptor.getOptions).get ==
      my_opts.MyMessageOption().update(_.priority := 17))

val numberField = use_opts.OneMessage.descriptor.findFieldByName("number")
assert(
  my_opts.Wrapper.tags.get(
    numberField.getOptions) == Seq(
      my_opts.Tag(name = Some("tag1")),
      my_opts.Tag(name = Some("tag2"))))
{% endhighlight %}

If you prefer to start with the descriptor, you use can the `extension`
method available through implicit conversion:

{% highlight scala %}
import com.trueaccord.scalapb.Implicits._

assert(use_opts.CustomOptionsUseOptsProto.descriptor.getOptions.extension(
  my_opts.CustomOptionsMyOptsProto.myFileOption) == Some("hello!"))

assert(use_opts.OneMessage.descriptor.getOptions.extension(
  my_opts.CustomOptionsMyOptsProto.myMessageOption).get ==
      my_opts.MyMessageOption().update(_.priority := 17))

assert(numberField.getOptions.extension(
  my_opts.Wrapper.tags) == Seq(
      my_opts.Tag(name = Some("tag1")),
      my_opts.Tag(name = Some("tag2"))))

{% endhighlight %}

## Example code

The full source code of this example is available below:

- [Protobufs](https://github.com/trueaccord/ScalaPB/tree/master/examples/src/main/protobuf/custom_options)

- [Scala](https://github.com/trueaccord/ScalaPB/tree/master/examples/src/main/scala/custom_options)

