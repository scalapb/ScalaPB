---
title: "ScalaPB: File Options"
layout: page
---

# ScalaPB File-level Options

ScalaPB file-level options lets you

- specify the Scala package to use, independently of the Java package option.
- request that ScalaPB will not append the protofile name to the package name.

The file-level options are not required, unless you are interested in those
customizations. If you do not want to customize the defaults, you can safely
skip this section.

## Supported options

{% highlight proto %}
import "scalapb/scalapb.proto";

option (scalapb.options) = {
  package_name: "com.example.myprotos"
  flat_package: true
};
{% endhighlight %}

- `package_name` sets the Scala base package name, if this is not defined,
then it falls back to `java_package` and then to `package`. 

- `flat_package` settings makes ScalaPB not append the protofile base name
to the package name.  You can also apply this option globally to all files
by adding it to your [ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

## Adding scalapb.proto to your project

The easiest way to get `protoc` to find `scalapb/scalapb.proto` when compiling
through SBT is by adding the following to your `build.sbt`:

    libraryDepenencies += "com.trueaccord.scalapb" %% "scalapb-runtime" % "{{site.data.version.scalapb}}" % PB.protobufConfig

If you are invoking `protoc` manually, you will need to ensure that the files in
[`protobuf`](https://github.com/trueaccord/ScalaPB/tree/master/protobuf)
directory are available to your project.
