---
title: "gRPC"
layout: docs
---

# gRPC

## gRPC Libraries for ScalaPB

This page covers ScalaPB's gRPC support. This support is a thin wrapper around
grpc-java, and provides you with an interface that is based on Scala's
standard library `Feature`, while streaming is based on the Observer pattern.

There are additional gRPC libraries built on top of ScalaPB that provide integration with other concurrency frameworks and effect systems:

* [ZIO gRPC](https://scalapb.github.io/zio-grpc/) enables you to build gRPC
  servers and clients using ZIO.
* [fs2-grpc](https://github.com/fiadliel/fs2-grpc) provides gGRPC support for FS2 and Cats Effect.
* [Akka gRPC](https://doc.akka.io/docs/akka-grpc/current/index.html) provides support for building streaming gRPC servers and clients on top of Akka Streams and Akka HTTP.

## Project Setup

Install ScalaPB as usual. Add the following to your `build.sbt`:

    libraryDependencies ++= Seq(
        "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion,
        "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    )

## Creating a service

When you run `sbt compile`, ScalaPB will generate code for your servers. This
includes an asynchronous client, a blocking and a base trait you can extend to
implement the server side.

For example, if your proto file in `src/main/protobuf/hello.proto` looks like
this:

```protobuf
syntax = "proto3";

package com.example.protos;

// The greeting service definition.
service Greeter {
  // Sends a greeting
  rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// The request message containing the user's name.
message HelloRequest {
  string name = 1;
}

// The response message containing the greetings
message HelloReply {
  string message = 1;
}
```

The generated code will look like this (simplified and commented):

```scala
object GreeterGrpc {

  // Base trait for an asynchronous client and server.
  trait Greeter extends AbstractService {
    def serviceCompanion = Greeter
    def sayHello(request: HelloRequest): Future[HelloReply]
  }

  object Greeter extend ServiceCompanion[Greeter] {
      def descriptor = ...
  }

  // Abstract trait for a blocking client:
  trait GreeterBlockingClient {
    def serviceCompanion = Greeter
    def sayHello(request: HelloRequest): HelloReply
  }

  // Concrete blocking client:
  class GreeterBlockingStub(channel, options) extends [...] with GreeterBlockingClient {
      // omitted
  }

  // Concrete asynchronous client:
  class GreeterStub(channel, options) extends io.grpc.stub.AbstractStub[GreeterStub](channel, options) with Greeter {
      // omitted
  }

  // Creates a new blocking client.
  def blockingStub(channel: io.grpc.Channel): GreeterBlockingStub = new GreeterBlockingStub(channel)

  // Creates a new asynchronous client.
  def stub(channel: io.grpc.Channel): GreeterStub = new GreeterStub(channel)
}
```

## Using the client

Creating a channel:

```scala
val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
val request = HelloRequest(name = "World")
```

Blocking call:

```scala
val blockingStub = GreeterGrpc.blockingStub(channel)
val reply: HelloReply = blockingStub.sayHello(request)
println(reply)
```

Async call:

```
val stub = GreeterGrpc.stub(channel)
val f: Future[HelloReply] = stub.sayHello(request)
f onComplete println
```

## Writing a server

Implement the service:

```scala
private class GreeterImpl extends GreeterGrpc.Greeter {
  override def sayHello(req: HelloRequest) = {
    val reply = HelloReply(message = "Hello " + req.name)
    Future.successful(reply)
  }
}
```

See
[complete example server](https://github.com/xuwei-k/grpc-scala-sample/blob/master/grpc-scala/src/main/scala/io/grpc/examples/helloworld/HelloWorldServer.scala) here.

## Streaming clients, streaming servers, bidi

Scalapb-grpc supports both client and server streaming. The Scala API follows
closely the offical `grpc-java` API. Example project coming soon.

## grpc-netty issues

In certain situations (for example when you have a fat jar), you may see the
following exception:

    Exception in thread "main" io.grpc.ManagedChannelProvider$ProviderNotFoundException: No functional server found. Try adding a dependency on the grpc-netty artifact

To work around this issue, create a `NettyServer` explicitly using
`io.grpc.netty.NettyServerBuilder`.
