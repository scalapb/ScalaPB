---
title: "Writing protoc plugins in Scala"
sidebar_label: "Writing protoc plugins"
layout: docs
---

This guide will show you how to write Protoc Plugins in Scala so you can write your own custom code generators for protocol buffers.

## Introduction: What is a protoc plugin?

A protoc plugin is a program that gets invoked by protoc (the protobuf compiler) and generates output files based on a set of input protocol buffers. The plugins are programs that read a `CodeGeneratorRequest` via their standard input and write a `CodeGeneratorResponse` to their standard output.


`CodeGeneratorRequest` is a protobuf that describes the protocol buffers being compiled, along with all their transitive imports. `CodeGeneratorResponse` is a protobuf that contains a list of output filenames along with their content to be written to the file system by protoc. See [plugin.proto](https://github.com/protocolbuffers/protobuf/blob/master/src/google/protobuf/compiler/plugin.proto) for the definitions of these messages.


## When to write a protoc plugin?

Generally, write a protoc plugin whenever you want to generate code that corresponds to the structure of protobufs. For example, ScalaPB’s code protoc plugin generates case classes for each protobuf message. The protoc plugins shipped with [akka-grpc](https://doc.akka.io/docs/akka-grpc/current/), [fs2-grpc](https://github.com/fiadliel/fs2-grpc) and [zio-grpc](https://scalapb.github.io/zio-grpc/) generate Scala traits with methods that correspond to protobuf service methods.

Plugins can also be used to generate code that validates messages (see  [scalapb-validate](https://github.com/scalapb/scalapb-validate)), or convert protobufs to a different format.

:::note Some use cases don’t require a plugin.

Using Descriptors you can inspect the structure of protocol buffers at runtime, extract values of arbitrary fields from message instances and even create new instances of messages. You can look into the source of [scalapb-json4s](https://github.com/scalapb/scalapb-json4s/blob/master/src/main/scala/scalapb/json4s/JsonFormat.scala) to see how conversion to and from JSON can be done without code generation. In contrast, the RPC libraries mentioned above create traits with methods that correspond to methods in the proto which would be impossible to accomplish at runtime (at least, in a statically typed manner).
:::

## Getting Started

As plugins are just programs that read a `CodeGeneratorRequest` and write a `CodeGeneratorResponse`, they are fairly simple to code. However, as you start going, you will want:

* **Rapidly test changes** in the generator over a sample protobuf, so you don't have to manually publish the plugin each time you want to try the generated code.
* **Access ScalaPB's `DescriptorImplicits`** which give you access to the Scala types and names used by ScalaPB for the different protobuf entities. So your code doesn’t have to guess.
* **Publish your plugin** in different formats for users of SBT and for users of other build tools (CLI, maven, etc)

To let you do all of the above, and to get you off to a great start with a streamlined development setup that uses the current best practices, we have prepared a project template. To create your plugin:

1. Open a terminal and change to your development directory. The project will be generated into a subdirectory of this directory.

2. Create your project:
   ```
   sbt new scalapb/protoc-gen-template.g8
   ```

3. The template will prompt you for the name of your plugin and what package name to use. The answers for those questions will be used extensively in the generated project.

## Look around

The project that is generated is an sbt multiproject with the following directory structure:

* `code-gen`: the actual code generator.
* `core`: is an optional Scala library that the generated code can depend on. For example, if you find that the generator code is producing a large block of code, you might want to move it to this library, and call it from there.
* `e2e`: an integration test for your plugin. The `e2e` project contains a test protobuf in `src/main/protobuf`, and you should add some more based on what needs to be tested for your plugin. The project also has an munit test suite to exercise the generated code. Each time you run the tests, the code generator will be recompiled, and code for the protobufs will be regenerated and compiled. This flow results in very productive edit-test iterations.

Now, start `sbt` and type `projects`. You will something like this:
```
[info] In file:/tmp/my-cool-plugin/
[info]     codeGenJVM2_12
[info]     codeGenJVM2_13
[info]     coreJVM2_12
[info]     coreJVM2_13
[info]     e2eJVM2_12
[info]     e2eJVM2_13
[info]     protoc-gen-my-cool-plugin
[info]     protoc-gen-my-cool-plugin-unix
[info]     protoc-gen-my-cool-plugin-windows
[info]   * root
```

:::note
You might wonder why we have different synthetic sub-projects for different versions of Scala. We are using [sbt-projectmatrix](https://github.com/sbt/sbt-projectmatrix) here, instead of SBT’s built-in cross-version support to facilitate the use of the code generator by e2e. The root cause is that SBT itself is built in Scala 2.12.  When you run the e2e tests for Scala 2.13, we want to be able to compile and execute the Scala 2.12 version of the code generator so it can load quickly into the same JVM used by SBT. This is not currently possible with SBT `crossScalaVersions`.
:::


The `protoc-gen-*` projects are used for publishing artifcats and will be described in a later section.

## Running the tests

To run the end-to-end tests for Scala 2.12 and Scala 2.13, inside SBT type:

```
e2eJVM2_12/test
```

and
```
e2eJVM2_13/test
```

This will compile the code generator (for Scala 2.12 in both cases), generate the code for the protos in `e2e/src/main/protobuf`, compile and run the tests in e2e for the corresponding Scala version.

Now, find the generated code under `e2e/target/jvm-2.12/src_managed/main/scalapb/com/myplugin/test/TestMessageFieldNums.scala`. The path might differ based on the package name you chose when creating the project.


## Understanding the code generator

Look for `CodeGenerator.scala` under the code-gen directory. There you will find an object like this:

```scala
object CodeGenerator extends CodeGenApp {
  override def registerExtensions(registry: ExtensionRegistry): Unit = {
    Scalapb.registerAllExtensions(registry)
  }

  // When your code generator will be invoked from SBT via sbt-protoc,
  // this will add the following artifact to your users build whenver

  // the generator is used in `PB.targets`:
  override def suggestedDependencies: Seq[Artifact] =
    Seq(
      Artifact(
        BuildInfo.organization,
        "my-cool-plugin-core",
        BuildInfo.version,
        crossVersion = true
      )
    )

  // This is called by CodeGenApp after the request is parsed.
  def process(request: CodeGenRequest): CodeGenResponse =
    ProtobufGenerator.parseParameters(request.parameter) match {
      case Right(params) =>
        // Implicits gives you extension methods that provide ScalaPB
        // names and types for protobuf entities.
        val implicits =
          new DescriptorImplicits(params, request.allProtos)

        // Process each top-level message in each file.
        // This can be customized if you want to traverse
        // the input in a different way.
        CodeGenResponse.succeed(
          for {
            file <- request.filesToGenerate
            message <- file.getMessageTypes().asScala
          } yield new MessagePrinter(message, implicits).result
        )
      case Left(error)   =>
        CodeGenResponse.fail(error)
    }
}
```


The object extends the `CodeGenApp` trait. This trait provides our application a `main` method so it can be used as a standalone protoc plugin. That trait extends another trait named `ProtocCodeGenerator` which facilitates the integration with `sbt-protoc`. `ProtocCodeGenerator` provides for us the method `suggestedDependencies` that let us specify which libraries we want to append to the `libraryDependencies` of our users. Normally, we want to add our `core` library. If you don't need to change the user's library dependencies you can remove this method as the default implementations return an empty list of artifacts.

The `registerExtensions` method is called when parsing the request and used to install protobuf extensions inside an `ExtensionRegistry`. This is useful if you are planning to add [custom protobuf options](user_defined_options.md).


The main action happens at the `process` method that takes a `CodeGenRequest` and returns a `CodeGenResponse`. These classes are simple wrappers around  the Java based protobufs `CodeGeneratorRequest` and `CodeGeneratorResponse` and are provided by a helper project called [protocgen](https://github.com/scalapb/protoc-bridge/tree/master/protoc-gen/src/main/scala/protocgen). This is the place you would normally start to customize from.  The template starts by parsing the parameters given in the request, then it creates a `DescriptorImplicits` object that provides us with ScalaPB-specific information about the protobuf entities such as the names of generated Scala types.

It is important to pass ScalaPB's parameters to DescriptorImplicits rather than the default since parameters such as `flat_package` change the package name and thus the generated code may not compile due to trying to use a symbol that doesn't exist.

The code instantiates a `MessagePrinter` for each message. We use a class rather than a method here so we only import the implicits in a single place:

```scala
class MessagePrinter(message: Descriptor, implicits: DescriptorImplicits) {
  import implicits._

  private val MessageObject =
    message.scalaType.sibling(message.scalaType.name + "FieldNums")

  def scalaFileName =
    MessageObject.fullName.replace('.', '/') + ".scala"

  def result: CodeGeneratorResponse.File = {
    val b = CodeGeneratorResponse.File.newBuilder()
    b.setName(scalaFileName)
    b.setContent(content)
    b.build()
  }

  def printObject(fp: FunctionalPrinter): FunctionalPrinter =
    fp
      .add(s"object ${MessageObject.name} {")
      .indented(
        _.print(message.getFields().asScala){ (fp, fd) => printField(fp, fd) }
        .add("")
        .print(message.getNestedTypes().asScala) {
          (fp, m) => new MessagePrinter(m, implicits).printObject(fp)
        }
      )
      .add("}")

  def printField(fp: FunctionalPrinter, fd: FieldDescriptor): FunctionalPrinter =
    fp.add(s"val ${fd.getName} = ${fd.getNumber}")

  def content: String = {
    val fp = new FunctionalPrinter()
    .add(
      s"package ${message.getFile.scalaPackage.fullName}",
      "",
    ).call(printObject)
    fp.result
  }
}
```

## Changing the generated code

Let's make a simple change for the generated code. For example, try changing the suffix of the generated classes from `FieldNums` to `FieldNumbers`:

Before:
```scala
private val MessageObject =
  message.scalaType.sibling(message.scalaType.name + "FieldNums")
```

After:
```scala
private val MessageObject =
  message.scalaType.sibling(message.scalaType.name + "FieldNumbers")
```

 Then run `e2eJVM2_12/test`. The code in `e2e` will be regenerated, and you’ll see a compilation error, since the tests still use the old names. You can open the generated code under `target/scala_2.12` directory to see the modified generated code. To finish this exercise on a positive note, make the tests in `e2e/src/test/scala` pass by updating the reference to the new class name.
Publishing the code generator

## Publishing the plugin

The project can be published to Maven using the “publish” command. We recommend to use the excellent [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release) plugin to automatically build a snapshot on each commit, and a full release when pushing a git tag.

SBT users of your code generators will add your plugin to the build by adding it to their `project/plugins.sbt` like this:

```scala
PB.targets in Compile := Seq(
  scalapb.gen()      -> (sourceManaged in Compile).value / "scalapb",
  com.myplugin.gen() -> (sourceManaged in Compile).value / "scalapb"
)
```

The template also publishes artifacts with names ending with `unix.sh` and `windows.bat`. These are executable jars for Unix and Windows systems that contain all the classes needed to run your code generator (except of a JVM which is expected to be in `JAVA_HOME` or in the `PATH`). This is useful if your users need to use your plugin directly with protoc, or with a build tool such as maven.

## Summary

If you followed this guide all the way to here, then congratulations for creating your first protoc plugin in Scala!

If you have any questions, feel free to reach out to us on Gitter or Github.

Did you write an interesting protoc plugin? Let us know on our gitter channel or our Google group and we'd love to mention it here!
