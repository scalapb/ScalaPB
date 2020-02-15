---
title: "FAQ"
layout: docs
---

# Frequently Asked Questions

## IntelliJ complains on duplicate files ("class is already defined")

If you are using sbt-protoc this should not happen. Please file a bug.

If you are still using sbt-scalapb, please switch to sbt-protoc as described
in the installation instruction.

sbt-protobuf which sbt-scalapb relies on defaults to generating the case
classes in `target/src_managed/compiled_protobuf/`.  This leads to a situation
where both `target/src_managed/compiled_protobuf/` and its parent, `target/src_managed/`,
are considered source directories and the source files are seen twice. To
eliminate this problem, let's tell sbt-protobuf to generate the sources into
the parent directory. Add this to your `build.sbt`:

```scala
scalaSource in PB.protobufConfig := sourceManaged.value
```

If you generate Java sources, add,

```scala
javaSource in PB.protobufConfig := sourceManaged.value
```

## How do I use ScalaPB from the command line?

Check out [ScalaPBC]({{site.baseurl}}/scalapbc.html).

## How do I use ScalaPB with Maven?

ScalaPB code generator can be invoked in your Maven build through the protobuf-maven-plugin. See [example project](https://github.com/thesamet/scalapb-maven-example).

The relevant parts are marked with "Add protobuf-maven-plugin..."

## How do I get grpc, java conversions, flat packages, etc with Maven?

The example maven project invokes ScalaPBC. To get these ScalaPB features, you need to pass a
generator parameter to ScalaPBC. See the supported generator parameters and how to use them in
[ScalaPBC]({{site.baseurl}}/scalapbc.html) documentation.

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

Include the jar as a `protobuf` dependency in your libraryDependencies:

```scala
libraryDependencies += "com.somepackage" %% "that-has-jar" % "1.0" % "protobuf"
```

This will tell sbt-protoc to extract the protos from that jar into
`target/scala-2.vv/protobuf-external`. This makes it possible to `import`
those protos from your local protos. sbt-protoc looks for protocol buffers to
compile in the directories listed in `PB.protoSources`. There you need to
add a line like this to your `build.sbt`:

```scala
PB.protoSources in Compile += target.value / "protobuf_external"
```

You may find other protos under `protobuf_external` that you do not wish to
compile. You can exclude them by adding an `includeFilter`:

    includeFilter in PB.generate := new SimpleFileFilter(
      (f: File) =>  f.getParent.endsWith("com/thesamet/protos"))

See [full example here](https://github.com/thesamet/sbt-protoc/tree/master/examples/multi-with-external-jar).

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
types in proto3, you can use this fact to your advantage by using [primitive wrappers]({{site.baseurl}}/customizations.html#primitive-wrappers)

## Why a certain customization is not available as a global generator parameter?

It turns out that global generator parameters that affect source code compatibility are something that we would like to avoid, as it is creating issues that are tricky to resolve. For example, if:

- package A provides proto files and generate source code with one value of a generator parameter,
- package B is compiled separately with a different value of this generator parameter, and imports protos from package A, as well as its generated classes.

then the code generator for B has no way of knowing that package A has code generated with a different parameter, and that it needs to account for that in the way it references it. This leads to compilation errors in ScalaPB or in user-provided code generators.

In version 0.8.2, we introduced [package-scoped options]({{site.baseurl}}/customizations.html#package-scoped-options) which let you set file-level options for an entire package at once.

## How do I write my own Scala code generator for protocol buffers?

Easy! Check out [giter8 template for writing new code generators](https://github.com/scalapb/scalapb-plugin-template.g8).

## How do I use ScalaPB with Gradle?

You can use ScalaPB with the official [Protobuf Plugin for Gradle](https://github.com/google/protobuf-gradle-plugin).

In your `build.gradle` the `protobuf` section should look like this:

```gradle
ext {
  scalapbVersion = '{{site.data.version.scalapb}}'
}

dependencies {
  compile "com.thesamet.scalapb:scalapb-runtime_2.12:${scalapbVersion}"
}

protobuf {
  protoc {
    artifact = 'com.google.protobuf:protoc:{{site.data.version.protoc}}'
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
            // add any ScalaPB generator options here. See: https://scalapb.github.io/scalapbc.html#passing-generator-parameters
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
