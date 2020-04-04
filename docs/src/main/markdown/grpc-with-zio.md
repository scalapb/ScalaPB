---
title: "gRPC with ZIO"
layout: docs
---

ScalaPB provides support for writing purely functional gRPC services and clients using ZIO.

# Highlights
* Supports all types of RPCs (unary, client streaming, server streaming, bidirectional).
* Uses ZIO's `Stream` to let you easily implement streaming requests.
* Cancellable RPCs: client-side ZIO interruptions are propagated to the server to abort the request and save resources.

# Installation using SBT

If you are building with sbt, add the following to your `project/plugins.sbt`:

```scala
val zioGrpcVersion =

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

# Generating code from the command-line