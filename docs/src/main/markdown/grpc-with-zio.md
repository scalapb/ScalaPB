---
title: "gRPC with ZIO"
layout: docs
---

ScalaPB provides support for writing purely functional gRPC services and clients using ZIO.

# Highlights
* Supports all types of RPCs (unary, client streaming, server streaming, bidirectional).
* Uses ZIO's `Stream` to let you easily implement streaming requests.
* Cancellable RPCs: client-side ZIO interruptions are propagated to the server to abort the request and save resources.

# Installation using SBT (Recommended)

If you are building with sbt, add the following to your `project/plugins.sbt`:

```scala
addSbtPlugin("com.thesamet" % "sbt-protoc" % "{{site.data.version.sbt_protoc}}")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "{{site.data.version.scalapb}}"

libraryDependencies += "com.thesamet.scalapb.zio-grpc" %% "zio-grpc-codegen" % "{{site.data.version.ziogrpc}}"
```

Then, add the following line to your `build.sbt`:

```scala
PB.targets in Compile := Seq(
    scalapb.gen() -> (sourceManaged in Compile).value,
    scalapb.zio_grpc.ZioCodeGenerator -> (sourceManaged in Compile).value,
)

libraryDependencies ++= Seq(
    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
    "io.grpc" % "grpc-netty" % "{{site.data.version.grpc}}"
)
```

# Manually generating code using ScalaPBC (CLI)

> See installation instructions for ScalaPBC [here](/scalapbc.html).

If you are using ScalaPBC to generate Scala code from the CLI, you can invoke the zio code generator like this:

```bash
scalapbc --plugin-artifact=com.thesamet.scalapb.zio-grpc:protoc-gen-zio:{{site.data.versions.ziogrpc}}:default,classifier=unix,ext=sh,type=jar -- e2e/src/main/protobuf/service.proto --zio_out=/tmp/out --scala_out=grpc:/tmp/out -Ie2e/src/main/protobuf -Ithird_party -Iprotobuf
```

You will need to add to your project the following libraries:
* `com.thesamet.scalapb::scalapb-runtime-grpc:{{site.data.version.scalapb}})`
* `com.thesamet.scalapb.zio-grpc::zio-grpc-core:{{site.data.version.ziogrpc}}`
* `io.grpc:grpc-netty:{{site.data.version.grpc}}`


```scala mdoc:invisible
import scalapb.docs.benchmarks.ProtoSnippet.showFile
```

# Creating a service

If you use sbt, the plugin will automatically compile protos located under `src/main/protobuf`. In this example, we will create a file named `points.proto`:

```scala mdoc:passthrough
showFile("points.proto")
```
