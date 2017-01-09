package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo
import scala.collection.JavaConverters._

final class GrpcServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {
  private[this] def observer(typeParam: String): String = s"$streamObserver[$typeParam]"

  private[this] def serviceMethodSignature(method: MethodDescriptor) = {
    s"def ${method.name}" + (method.streamType match {
      case StreamType.Unary =>
        s"(request: ${method.scalaIn}): scala.concurrent.Future[${method.scalaOut}]"
      case StreamType.ClientStreaming =>
        s"(responseObserver: ${observer(method.scalaOut)}): ${observer(method.scalaIn)}"
      case StreamType.ServerStreaming =>
        s"(request: ${method.scalaIn}, responseObserver: ${observer(method.scalaOut)}): Unit"
      case StreamType.Bidirectional =>
        s"(responseObserver: ${observer(method.scalaOut)}): ${observer(method.scalaIn)}"
    })
  }

  private[this] def blockingMethodSignature(method: MethodDescriptor) = {
    s"def ${method.name}" + (method.streamType match {
      case StreamType.Unary =>
        s"(request: ${method.scalaIn}): ${method.scalaOut}"
      case StreamType.ServerStreaming =>
        s"(request: ${method.scalaIn}): scala.collection.Iterator[${method.scalaOut}]"
      case _ => throw new IllegalArgumentException("Invalid method type.")
    })
  }

  private[this] def serviceTrait: PrinterEndo = {
    val endos: PrinterEndo = { p =>
      p.seq(service.methods.map(m => serviceMethodSignature(m)))
    }

    p =>
      p
        .add(s"trait ${service.name} extends _root_.com.trueaccord.scalapb.grpc.AbstractService {")
        .indent
        .add(s"override def serviceCompanion = ${service.name}")
        .call(endos)
        .outdent
        .add("}")
  }

  private[this] def serviceTraitCompanion: PrinterEndo = {
    p =>
      p
        .add(s"object ${service.name} extends _root_.com.trueaccord.scalapb.grpc.ServiceCompanion[${service.name}] {")
        .indent
        .add(s"implicit def serviceCompanion: _root_.com.trueaccord.scalapb.grpc.ServiceCompanion[${service.name}] = this")
        .add(s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ${service.getFile.fileDescriptorObjectFullName}.javaDescriptor.getServices().get(${service.getIndex})")
        .outdent
        .add("}")
  }

  private[this] def blockingClientTrait: PrinterEndo = {
    val endos: PrinterEndo = { p =>
      p.seq(service.methods.filter(_.canBeBlocking).map(m => blockingMethodSignature(m)))
    }

    p =>
      p
        .add(s"trait ${service.blockingClient} {")
        .indent
        .add(s"def serviceCompanion = ${service.name}")
        .call(endos)
        .outdent
        .add("}")
  }

  private[this] val channel = "_root_.io.grpc.Channel"
  private[this] val callOptions = "_root_.io.grpc.CallOptions"

  private[this] val abstractStub = "_root_.io.grpc.stub.AbstractStub"
  private[this] val streamObserver = "_root_.io.grpc.stub.StreamObserver"

  private[this] val serverCalls = "_root_.io.grpc.stub.ServerCalls"
  private[this] val clientCalls = "_root_.io.grpc.stub.ClientCalls"

  private[this] val guavaFuture2ScalaFuture = "com.trueaccord.scalapb.grpc.Grpc.guavaFuture2ScalaFuture"

  private[this] def clientMethodImpl(m: MethodDescriptor, blocking: Boolean) = PrinterEndo{ p =>
    m.streamType match {
      case StreamType.Unary =>
        if(blocking) {
          p.add(
            "override " + blockingMethodSignature(m) + " = {"
          ).add(
            s"""  $clientCalls.blockingUnaryCall(channel.newCall(${m.descriptorName}, options), request)""",
            "}"
          )
        } else {
          p.add(
            "override " + serviceMethodSignature(m) + " = {"
          ).add(
            s"""  $guavaFuture2ScalaFuture($clientCalls.futureUnaryCall(channel.newCall(${m.descriptorName}, options), request))""",
            "}"
          )
        }
      case StreamType.ServerStreaming =>
        if (blocking) {
          p.add(
            "override " + blockingMethodSignature(m) + " = {"
          ).addIndented(
            s"scala.collection.JavaConversions.asScalaIterator($clientCalls.blockingServerStreamingCall(channel.newCall(${m.descriptorName}, options), request))"
          ).add("}")
        } else {
          p.add(
            "override " + serviceMethodSignature(m) + " = {"
          ).addIndented(
            s"$clientCalls.asyncServerStreamingCall(channel.newCall(${m.descriptorName}, options), request, responseObserver)"
          ).add("}")
        }
      case streamType =>
        require(!blocking)
        val call = if (streamType == StreamType.ClientStreaming) {
          s"$clientCalls.asyncClientStreamingCall"
        } else {
          s"$clientCalls.asyncBidiStreamingCall"
        }

        p.add(
          "override " + serviceMethodSignature(m) + " = {"
        ).indent.add(
          s"$call(channel.newCall(${m.descriptorName}, options), responseObserver)"
        ).outdent.add("}")
    }
  } andThen PrinterEndo { _.newline }

  private def stubImplementation(className: String, baseClass: String, methods: Seq[PrinterEndo]): PrinterEndo = {
    p =>
      val build =
        s"override def build(channel: $channel, options: $callOptions): ${className} = new $className(channel, options)"
      p.add(
        s"class $className(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$className](channel, options) with $baseClass {"
      ).indent
        .call(methods: _*)
        .add(build)
        .outdent.add("}")
  }

  private[this] val blockingStub: PrinterEndo = {
    val methods = service.methods.filter(_.canBeBlocking).map(clientMethodImpl(_, true))
    stubImplementation(service.blockingStub, service.blockingClient, methods)
  }

  private[this] val stub: PrinterEndo = {
    val methods = service.getMethods.asScala.map(clientMethodImpl(_, false))
    stubImplementation(service.stub, service.name, methods)
  }

  private[this] def methodDescriptor(method: MethodDescriptor) = PrinterEndo { p =>
    def marshaller(typeName: String) =
      s"new com.trueaccord.scalapb.grpc.Marshaller($typeName)"

    val methodType = method.streamType match {
      case StreamType.Unary => "UNARY"
      case StreamType.ClientStreaming => "CLIENT_STREAMING"
      case StreamType.ServerStreaming => "SERVER_STREAMING"
      case StreamType.Bidirectional => "BIDI_STREAMING"
    }

    val grpcMethodDescriptor = "_root_.io.grpc.MethodDescriptor"

    p.addStringMargin(
      s"""val ${method.descriptorName}: $grpcMethodDescriptor[${method.scalaIn}, ${method.scalaOut}] =
          |  $grpcMethodDescriptor.create(
          |    $grpcMethodDescriptor.MethodType.$methodType,
          |    $grpcMethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"),
          |    ${marshaller(method.scalaIn)},
          |    ${marshaller(method.scalaOut)})
          |""")
  }

  private[this] def addMethodImplementation(method: MethodDescriptor): PrinterEndo = PrinterEndo {
    _.add(".addMethod(")
      .add(s"  ${method.descriptorName},")
      .indent
      .call(PrinterEndo {
        p =>
          val call = method.streamType match {
            case StreamType.Unary => s"$serverCalls.asyncUnaryCall"
            case StreamType.ClientStreaming => s"$serverCalls.asyncClientStreamingCall"
            case StreamType.ServerStreaming => s"$serverCalls.asyncServerStreamingCall"
            case StreamType.Bidirectional => s"$serverCalls.asyncBidiStreamingCall"
          }

          val executionContext = "executionContext"
          val serviceImpl = "serviceImpl"

          method.streamType match {
            case StreamType.Unary =>
              val serverMethod = s"$serverCalls.UnaryMethod[${method.scalaIn}, ${method.scalaOut}]"
              p.addStringMargin(
                s"""$call(new $serverMethod {
                   |  override def invoke(request: ${method.scalaIn}, observer: $streamObserver[${method.scalaOut}]): Unit =
                   |    $serviceImpl.${method.name}(request).onComplete(com.trueaccord.scalapb.grpc.Grpc.completeObserver(observer))(
                   |      $executionContext)
                   |}))""")
            case StreamType.ServerStreaming =>
              val serverMethod = s"$serverCalls.ServerStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"
              p.addStringMargin(
                s"""$call(new $serverMethod {
                   |  override def invoke(request: ${method.scalaIn}, observer: $streamObserver[${method.scalaOut}]): Unit =
                   |    $serviceImpl.${method.name}(request, observer)
                   |}))""")
            case _ =>
              val serverMethod = if (method.streamType == StreamType.ClientStreaming) {
                s"$serverCalls.ClientStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"
              } else {
                s"$serverCalls.BidiStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"
              }
              p.addStringMargin(
                s"""$call(new $serverMethod {
                   |  override def invoke(observer: $streamObserver[${method.scalaOut}]): $streamObserver[${method.scalaIn}] =
                   |    $serviceImpl.${method.name}(observer)
                   |}))""")
          }
      })
      .outdent
  }

  private[this] val bindService = {
    val executionContext = "executionContext"
    val methods = service.methods.map(addMethodImplementation)
    val serverServiceDef = "_root_.io.grpc.ServerServiceDefinition"

    PrinterEndo(
      _.add(s"""def bindService(serviceImpl: ${service.name}, $executionContext: scala.concurrent.ExecutionContext): $serverServiceDef =""")
        .indent
        .add(s"""$serverServiceDef.builder("${service.getFullName}")""")
        .call(methods: _*)
        .add(".build()")
        .outdent)
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(
      "package " + service.getFile.scalaPackageName,
      "",
      s"object ${service.objectName} {")
    .indent
    .call(service.methods.map(methodDescriptor): _*)
    .call(serviceTrait)
    .newline
    .call(serviceTraitCompanion)
    .newline
    .call(blockingClientTrait)
    .newline
    .call(blockingStub)
    .newline
    .call(stub)
    .newline
    .call(bindService)
    .newline
    .add(s"def blockingStub(channel: $channel): ${service.blockingStub} = new ${service.blockingStub}(channel)")
    .newline
    .add(s"def stub(channel: $channel): ${service.stub} = new ${service.stub}(channel)")
    .newline
    .add(s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ${service.getFile.fileDescriptorObjectFullName}.javaDescriptor.getServices().get(${service.getIndex})")
    .newline
    .outdent
    .add("}")
  }
}
