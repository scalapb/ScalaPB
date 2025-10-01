---
title: "Frequently Asked Questions"
sidebar_label: "FAQ"
layout: docs
---

## How do I use ScalaPB from the command line?

Check out [ScalaPBC](scalapbc.md).

## How do I use ScalaPB with Maven?

ScalaPB code generator can be invoked in your Maven build through the protobuf-maven-plugin. See [example project](https://github.com/thesamet/scalapb-maven-example).

The relevant parts are marked with "Add protobuf-maven-plugin..."

## How do I get grpc, java conversions, flat packages, etc with Maven?

The example maven project invokes ScalaPBC. To get these ScalaPB features, you need to pass a
generator parameter to ScalaPBC. See the supported generator parameters and how to use them in
[ScalaPBC](scalapbc.md) documentation.

## I am getting "Import was not found or had errors"

If you are using sbt-protoc and importing protos like `scalapb/scalapb.proto`,
or common protocol buffers like `google/protobuf/wrappers.proto`:

Add the following to your `build.sbt`:

    libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

This tells `sbt-protoc` to extract protos from this jar (and all its
dependencies, which includes Google's common protos), and make them available
in the include path that is passed to protoc.

If you are not using sbt (for example, spbc), then you need to make those
files available on the file system.

## How do I generate Scala code for protos from another jar?

For some proto packages, we may already provide pre-compiled generated code in
 [ScalaPB's Common Protos project](https://github.com/scalapb/common-protos).
 If what you are looking for is not there, consider adding it by following the
 instructions on the project's page.

To build it yourself using SBT: include the jar as a `protobuf` dependency in your libraryDependencies:

```scala
libraryDependencies += "com.somepackage" %% "that-has-jar" % "1.0" % "protobuf-src" intransitive()
```

This will tell sbt-protoc to extract the protos from that jar into
`target/scala-2.vv/protobuf_external_src` and add them both to the import
search path and the set of sources to generate code for (`PB.protoSources`).

The `intransitive` modifier makes it not unpack the dependencies of this
library to the same directory since you may not want to generate classes for them
as well. Many common libraries depend on `protobuf-java`, and generated
classes for them are already shipped with ScalaPB's `scalapb-runtime`. When
using `intransitive()`, you should ensure all the dependencies are provided,
either as `protobuf-src` or `protobuf` (depending if you want to compile them
or just import them).

You may find other protos under `protobuf_external_src` that you do not wish to
compile. You can exclude them by adding an `excludeFilter`:

    PB.generate / excludeFilter := new SimpleFileFilter(
      (f: File) => f.getParent.endsWith("com/thesamet/protos"))

See [full example here](https://github.com/thesamet/sbt-protoc/tree/master/examples/multi-with-external-jar).

## Why message fields are wrapped in an `Option` in proto3?

For a proto like this:
```protobuf
syntax = "proto3";

message A {}

message B {
    A a = 1;
}
```

The generated case class for `B` will have a field `a: Option[A]`. The reason
is that in the proto3 format, it is valid for an encoded message of type `B` to not
contain a value for the field `a`. Using the `Option[A]` type lets us distinguish
between the case where a value for `A` was not provided and the case where `A`
was explicitly set to a certain value (even if that value is the default value
for `A`). The case where `a` is not set, and the case that `A` is set to its
default value have two distinct binary representations (in both proto2 and
proto3).

You can set a certain message type or a field to not be wrapped in an `Option`
using the [`no_box` option](customizations.md#message-level-custom-type-and-boxing).

## How do I represent `Option[T]` in proto3 for scalar fields?

Scalar fields are the various numeric types, `bool`, `string`, `byte` and `enum` -
everything except of messages. In the proto2 wire format, there is a distinction between
not setting a value (`None`), or setting it to its default value (`Some(0)` or
`Some("")`).

In proto3, this distinction has been removed in the wire format. Whenever the value
of a scalar type is zero, it does not get serialized. Therefore, the parser is not
able to distinguish between `Some(0)` or `None`. The semantics is that a [zero
has been received](https://developers.google.com/protocol-buffers/docs/proto3#default).

Optional message types are still wrapped in `Option[]` since there is a
distinction in the wire format between leaving out a message (which is
represent by `None` or sending one, even if all its fields are unset (or
assigned default values). If you wish to have `Option[]` around scalar
types in proto3, you can use this fact to your advantage by using [primitive wrappers](customizations.md#primitive-wrappers)

## Why a certain customization is not available as a global generator parameter?

It turns out that global generator parameters that affect source code compatibility are something that we would like to avoid, as it is creating issues that are tricky to resolve. For example, if:

- package A provides proto files and generate source code with one value of a generator parameter,
- package B is compiled separately with a different value of this generator parameter, and imports protos from package A, as well as its generated classes.

then the code generator for B has no way of knowing that package A has code generated with a different parameter, and that it needs to account for that in the way it references it. This leads to compilation errors in ScalaPB or in user-provided code generators.

In version 0.8.2, we introduced [package-scoped options](customizations.md#package-scoped-options) which let you set file-level options for an entire package at once.

## How do I write my own Scala code generator for protocol buffers?

Easy! Check out [giter8 template for writing new code generators](https://github.com/scalapb/scalapb-plugin-template.g8).

## How do I mark a generated case class private?

Easy! See this example:

```protobuf
syntax = "proto3";
import "scalapb/scalapb.proto";

message MyPrivateMessage {
    option (scalapb.message).annotations = "private[com.mypkg]";
    option (scalapb.message).companion_annotations = "private[com.mypkg]";
}
```

## How do I define custom field scope?

Easy! See this example:

```protobuf
syntax = "proto3";
import "scalapb/scalapb.proto";

message MyMessage {
  string privateField        = 1 [(scalapb.field).annotations = 'private val'];
  string protectedField      = 2 [(scalapb.field).annotations = 'protected val'];
  string packagePrivateField = 3 [(scalapb.field).annotations = 'private[proto] val'];
}
```

Generated code would be:

```scala
final case class MyMessage(
    private val privateField: String = "",
    protected val protectedField: String = "",
    private[proto] val packagePrivateField: String = ""
) extends ...
```

## How do I customize third-party types?

You can use field-transformations to match on third-party types and apply arbitrary field-level options. See [example here](transformations.md#example-customizing-third-party-types).

## Using Spark, I am getting `No encoder found for ...` or `Unable to find encoder for type`

When using ScalaPB case classes with Spark datasets or dataframes,
you need to be using sparksql-scalapb:

1. Make sure the version of ScalaPB, sparksql-scalapb and ScalaPB match according to [this table](sparksql.md#setting-up-your-project).

2. In the scope where the exception occurs, make sure you import `scalapb.spark.Implicits._`. **Do not import `spark.implicits._`**.

3. If you use an interactive notebook such as Databricks or spark-shell, there is a default import of `spark.implicits._` that is executed prior to your own code. There is currently no way to disable this behavior. The workaround is to manually pass the encoder
   explicitly in the locations where an encoder is not found, for example:
   ```scala
   import scalapb.spark.Implicits.typedEncoderToEncoder

   df.as[MyMessageType](typedEncoderToEncoder[MyMessageType])

   spark.createDataset(myList, typedEncoderToEncoder[MyMessageType])
   ```

This error is very common for newcomers to ScalaPB and Spark. If the guidance above did not resolve your issue and you would like to get support over Gitter, Github or Stackoverflow, please clearly indicate that you have read this FAQ, and provide the protos and code that triggers the exception, including the relevant imports in scope. Ideally, provide a reproducible example. The easiest way to do it is to fork [this repo](https://github.com/thesamet/sparksql-scalapb-test), adjust so it reproduces the issue you have and provide a link to your fork in your problem report.


## How do I use ScalaPB with Gradle?

You can use ScalaPB with the official [Protobuf Plugin for Gradle](https://github.com/google/protobuf-gradle-plugin).

In your `build.gradle` the `protobuf` section should look like this:

```gradle
ext {
  scalapbVersion = '@scalapb@'
}

dependencies {
  compile "com.thesamet.scalapb:scalapb-runtime_2.12:${scalapbVersion}"
}

protobuf {
  protoc {
    artifact = 'com.google.protobuf:protoc:@protoc@'
  }
  plugins {
    scalapb {
      artifact = (org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem().isWindows()) ?
          "com.thesamet.scalapb:protoc-gen-scala:${scalapbVersion}:windows@bat" :
          "com.thesamet.scalapb:protoc-gen-scala:${scalapbVersion}:unix@sh"
    }
  }

  generateProtoTasks {
    all().each { task ->
      task.builtins {
          // if you don't want java code to be generated.
          remove java
      }
      task.plugins {
          scalapb {
            // add any ScalaPB generator options here. See: https://scalapb.github.io/docs/scalapbc/#passing-generator-parameters
            // option 'flat_package'
          }
      }
    }
  }
}

// Add geneated Scala code as a source directory
sourceSets {
  main {
    scala {
        srcDirs "${protobuf.generatedFilesBaseDir}/main/scalapb"
    }
  }
}
```

See [full example here](https://github.com/scalapb/gradle-demo).

## Does ScalaPB work on Apple M1? I am getting "Bad CPU type".

Yes. Currently the official protoc ships with x86_64 binary, and you will be enable to run
it seamlessly if you have Rosetta enabled. If you don't already have Rosetta installed on your system
, you can install it by running:

```
softwareupdate --install-rosetta
```

Use a recent version of sbt-protoc (at least 1.0.6), which defaults to a
compatible version of protoc (3.19.2).
