---
title: "ScalaPB: gRPC"
layout: page
---

# gRPC

## Project Setup

Install ScalaPB as usual. Add the following to your `build.sbt`:

    libraryDependencies ++= Seq(
        "io.grpc" % "grpc-netty" % "1.0.1",
        "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
    )

## Creating a service

When you run `sbt compile`, ScalaPB will generate code for your servers. This
includes an asynchronous client, a blocking and a base trait you can extend to
implement the server side.

For example, if your proto file in `src/main/protobuf/hello.proto` looks like
this:

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


The generated code will look like this (simplified and commented):

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

## Using the client

Creating a channel:

    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
    val request = HelloRequest(name = "World")

Blocking call:

    val blockingStub = GreeterGrpc.blockingStub(channel)
    val reply: HelloReply = blockingStub.sayHello(request)
    println(reply)

Async call:

    val blockingStub = GreeterGrpc.stub(channel)
    val f: Future[HelloReqpblockingStub.sayHello(request)

## Writing a server

Implement the service:

    private class GreeterImpl extends GreeterGrpc.Greeter {
      override def sayHello(req: HelloRequest) = {
        val reply = HelloResponse(message = "Hello " + req.name)
        Future.successful(reply)
      }
    }

See
[complete example server](https://github.com/xuwei-k/grpc-scala-sample/blob/master/grpc-scala/src/main/scala/io/grpc/examples/helloworld/HelloWorldServer.scala) here.

## Streaming clients, streaming servers, bidi

See [example
code](https://github.com/xuwei-k/grpc-scala-sample/tree/master/grpc-scala/src/main/scala/io/grpc/examples/routeguide) based on the
[route_guide gRPC example](https://github.com/grpc/grpc-java/blob/63503c2989df6d895c56e22f430f2b934e7a41d3/examples/protos/route_guide.proto).

