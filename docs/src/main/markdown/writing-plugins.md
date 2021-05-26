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
          DescriptorImplicits.fromCodeGenRequest(params, request)

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

The `registerExtensions` method is called when parsing the request and used to install protobuf extensions inside an `ExtensionRegistry`. This is useful if you are planning to add [custom protobuf options](user_defined_options.md). See the section "Adding custom options" below to learn how to add custom options to your generator.

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

## Adding custom options

This section describes how you can let your users customize the generated code
via options. To add custom options, follow this process:

1. Create a proto file with the custom options you want to add under
   `core/src/main/protobuf`. Name it something like `myplugin.proto`:

   ```protobuf
   syntax = "proto2";

   package myorg.myplugin;

   import "google/protobuf/descriptor.proto";

   extend google.protobuf.MessageOptions {
         optional MyMessageOptions myopts = 60001;
   }

   message MyMessageOptions {
         optional bool my_option = 1;
   }
   ```

   :::note The number 60001 above is just an example!
   It's important that different extensions do not use the same numbers so they do not overwrite
   each other's data. If you publish your plugin externally, [request for an
   extension number here](https://github.com/protocolbuffers/protobuf/blob/master/docs/options.md).
   :::

2. Make your `core` project generate both Java and Scala sources for the
   custom options proto by adding the following settings to the `core`
   project in `build.sbt`:
   ```scala
   Compile / PB.targets := Seq(
     PB.gens.java -> (Compile / sourceManaged).value / "scalapb",
     scalapb.gen(javaConversions = true) ->
       (Compile / sourceManaged).value / "scalapb",
   )
   ```

3. The core project will only need the Java version of the new protobuf.
   Update its settings as follows:

   ```scala
   libraryDependencies ++= Seq(
      "com.thesamet.scalapb" %% "compilerplugin" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion,
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    ),
    Compile / PB.protoSources += core.base / "src" / "main" / "protobuf",
    Compile / PB.targets := Seq(
      PB.gens.java -> (Compile / sourceManaged).value / "scalapb"
    )
   ```

   This would tell ScalaPB to compile the protobuf that's in the core project protobuf directory. We
   are adding `scalapb` as a `"protobuf"` dependency so it extracts `scalapb.proto`, and its own
   transitive dependencies which includes `google/protobuf/descriptor.proto`.

4. Register the extension in the code generator. In your code generator , under `code-gen/src/main/scala/`
   look for the `registerExtensions` method, and add a call to register your own extension:

   ```scala
   myorg.Myplugin.registerAllExtensions(registry)
   ```

5. Now you are able to extract the extension value in your generator using the standard protobuf-java
   APIs:

   ```scsala
   messageDescriptor.getOptions.getExtension(myorg.Myplugin.myopts).getMyOption
   ```

6. You can now use the new option in your e2e tests. Also the newly added proto will be automatically
   packaged with the core jar. External projects will be able to unpack it by depending on the core
   library with a `% "protobuf"` scope. To use:

   ```protobuf
   import "myplugin.proto";

   message MyMessage {
     option (myplugin.myopts).my_option = false;
   }
   ```

## Publishing the plugin

The project can be published to Maven using the “publish” command. We recommend to use the excellent [sbt-ci-release](https://github.com/olafurpg/sbt-ci-release) plugin to automatically build a snapshot on each commit, and a full release when pushing a git tag.

SBT users of your code generators will add your plugin to the build by adding it to their `project/plugins.sbt` like this:

```scala
Compile / PB.targets := Seq(
  scalapb.gen()      -> (Compile / sourceManaged).value / "scalapb",
  com.myplugin.gen() -> (Compile / sourceManaged).value / "scalapb"
)
```

The template also publishes artifacts with names ending with `unix.sh` and `windows.bat`. These are executable jars for Unix and Windows systems that contain all the classes needed to run your code generator (except of a JVM which is expected to be in `JAVA_HOME` or in the `PATH`). This is useful if your users need to use your plugin directly with protoc, or with a build tool such as maven.

## Secondary outputs

:::note
Secondary outputs were introduced in protoc-bridge 0.9.0 and are supported by sbt-protoc 1.0.0 and onwards.
:::

Secondary outputs provide a simple way for protoc plugins to pass information for other protoc plugins running after them in the same protoc invocation. The information is passed through files that are created in a temporary directory. The absolute path of that temporary directory is provided to all protoc plugins. Plugins may create new files in that directory for subsequent plugins to consume.

Conventions:
* Names of secondary output files should be in `kebab-case`, and should clearly identify the plugin producing them. For example `scalapb-validate-preprocessor`.
* The content of the file should be a serialized `google.protobuf.Any` message that packs the arbitrary payload the plugin wants to publish.

### Determining the secondary output directory location
JVM-based plugins that are executed in the same JVM that spawns protoc (like the ones described on this page), receive the location of the secondary output directory via the `CodeGeneratorRequest`. `protoc-bridge` appends to the request an unknown field carrying a message called `ExtraEnv` which contains the path to the secondary output directory.

Other plugins that are invoked directly by protoc can find the secondary output directory by inspecting the `SCALAPB_SECONDARY_OUTPUT_DIR` environment variable.

`protoc-bridge` takes care of creating the temporary directory and setting up the environment variable before invoking `protoc`. If `protoc` is ran manually (for example, through the CLI), it is the user's responsibility to create a directory for secondary outputs and pass it as an environment variable to `protoc`. It's worth noting that ScalaPB only looks for secondary output directory if a preprocessor is requested, and therefore for the most part users do not need to worry about secondary output directories.

In ScalaPB's code base, [SecondaryOutputProvider](https://github.com/scalapb/ScalaPB/blob/75ad2323b8fc35f005c40471fa714a0eca0afd6d/compiler-plugin/src/main/scala/scalapb/compiler/SecondaryOutputProvider.scala#L78-L83) provides a method to find the secondary output directory as described above.

## Preprocessors

Preprocessors are protoc plugins that provide [secondary outputs](#secondary-outputs) that are consumed by ScalaPB. ScalaPB expects the secondary output to be a `google.protobuf.Any` that encodes a [PreprocessorOutput](https://github.com/scalapb/ScalaPB/blob/75ad2323b8fc35f005c40471fa714a0eca0afd6d/protobuf/scalapb/scalapb.proto#L346-L348). The message contains a map between proto file names (as given by `FileDescriptor#getFullName()`) to additional `ScalaPbOptions` that are merged with the files options. By appending to `aux_field_options`, a preprocessor can, for example, impact the generated types of ScalaPB fields.

* ScalaPB applies the provided options to a proto file only if the original file lists the preprocessor secondary output filename in a `preprocessors` file-level option. That option can be inherited from a package-scoped option.
* To exclude a specific file from being preprocessed (if it would be otherwise impacted by a package-scoped option), add a `-NAME` entry to the list of preprocessors where `NAME` is the name of the preprocessor's secondary output.
* In case of multiple preprocessors, options of later preprocessors overrides the one of earlier processors. Options in the file are merged over the preprocessor's options. When merging, repeated fields get concatenated.
* Preprocessor plugins need to be invoked (in `PB.targets` or protoc's command line) before ScalaPB, so when ScalaPB runs their output is available.
* Plugins that depend on ScalaPB (such as scalapb-validate) rely on `DescriptorImplicits` which consume the preprocessor output and therefore also see the updated options.

## Summary

If you followed this guide all the way to here, then congratulations for creating your first protoc plugin in Scala!

If you have any questions, feel free to reach out to us on Gitter or Github.

Did you write an interesting protoc plugin? Let us know on our gitter channel or our Google group and we'd love to mention it here!
