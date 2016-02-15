---
title: "ScalaPB: GRPC"
layout: page
---

# GRPC

## Project Setup

To use GRPC you must depend ScalaPB>=0.5.21 and protoc>=3.0.0-beta-1. For using
with protoc-jar:

    addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "{{site.data.verfsion.sbt_scalapb5}}")

    libraryDependencies ++= Seq(
      "com.github.os72" % "protoc-jar" % "3.0.0-b2"
    )

In build.sbt, include the following:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

    PB.protobufSettings

    PB.runProtoc in PB.protobufConfig := (args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

    version in PB.protobufConfig := "3.0.0-beta-2"

    libraryDependencies ++= Seq(
        "io.grpc" % "grpc-all" % "0.9.0",
        "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % (PB.scalapbVersion in PB.protobufConfig).value
    )

## Creating a service

When you run `sbt compile`, ScalaPB will generate code for your servers. This
includes an asynchronous client, a blocking and a base trait you can extend to
implement the server side.

For example, if your proto file in `src/main/protobuf/hello.proto` looks like
this:

    syntax = "proto3";

    packagee = "com.example.protos";

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
      trait Greeter {
        def sayHello(request: HelloRequest): Future[HelloReply]
      }

      // Abstract trait for a blocking client:
      trait GreeterBlockingClient {
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
[https://github.com/xuwei-k/grpc-scala-sample/blob/master/grpc-scala/src/main/scala/io/grpc/examples/helloworld/HelloWorldServer.scala](complete example server) here.

## Streaming clients, streaming servers, bidi

See [https://github.com/xuwei-k/grpc-scala-sample/tree/master/grpc-scala/src/main/scala/io/grpc/examples/routeguide](example
code) based on the
[https://github.com/grpc/grpc-java/blob/63503c2989df6d895c56e22f430f2b934e7a41d3/examples/protos/route_guide.proto](route_guide
GRPC example).

