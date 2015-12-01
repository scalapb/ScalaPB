package com.trueaccord.scalapb.compiler

import java.util.Locale

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo
import scala.collection.JavaConverters._

final class GrpcServicePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {

  private[this] def methodName0(method: MethodDescriptor): String = snakeCaseToCamelCase(method.getName)
  private[this] def methodName(method: MethodDescriptor): String = methodName0(method).asSymbol
  private[this] def observer(typeParam: String): String = s"$streamObserver[$typeParam]"

  private[this] def methodSignature(method: MethodDescriptor, t: String => String = identity[String]): String = {
    method.streamType match {
      case StreamType.Unary =>
        s"def ${methodName(method)}(request: ${method.scalaIn}): ${t(method.scalaOut)}"
      case StreamType.ServerStreaming =>
        s"def ${methodName(method)}(request: ${method.scalaIn}, observer: ${observer(method.scalaOut)}): Unit"
      case StreamType.ClientStreaming | StreamType.Bidirectional =>
        s"def ${methodName(method)}(observer: ${observer(method.scalaOut)}): ${observer(method.scalaIn)}"
    }
  }

  private[this] def base: PrinterEndo = {
    val F = "F[" + (_: String) + "]"

    val methods: PrinterEndo = { p =>
      p.seq(service.getMethods.asScala.map(methodSignature(_, F)))
    }

    { p =>
      p.add(s"trait ${serviceName(F("_"))} {").withIndent(methods).add("}")
    }
  }

  private[this] val channel = "_root_.io.grpc.Channel"
  private[this] val callOptions = "_root_.io.grpc.CallOptions"

  private[this] def serviceName0 = service.getName.asSymbol
  private[this] def serviceName(p: String) = serviceName0 + "[" + p + "]"
  private[this] val serviceBlocking = serviceName("({type l[a] = a})#l")
  private[this] val serviceFuture = serviceName("scala.concurrent.Future")

  private[this] val futureUnaryCall = "_root_.io.grpc.stub.ClientCalls.futureUnaryCall"
  private[this] val abstractStub = "_root_.io.grpc.stub.AbstractStub"
  private[this] val streamObserver = "_root_.io.grpc.stub.StreamObserver"

  private[this] object serverCalls {
    val unary = "_root_.io.grpc.stub.ServerCalls.asyncUnaryCall"
    val clientStreaming = "_root_.io.grpc.stub.ServerCalls.asyncClientStreamingCall"
    val serverStreaming = "_root_.io.grpc.stub.ServerCalls.asyncServerStreamingCall"
    val bidiStreaming = "_root_.io.grpc.stub.ServerCalls.asyncBidiStreamingCall"
  }

  private[this] object clientCalls {
    val clientStreaming = "_root_.io.grpc.stub.ClientCalls.asyncClientStreamingCall"
    val serverStreaming = "_root_.io.grpc.stub.ClientCalls.asyncServerStreamingCall"
    val bidiStreaming = "_root_.io.grpc.stub.ClientCalls.asyncBidiStreamingCall"
  }

  private[this] val blockingClientName: String = service.getName + "BlockingClientImpl"

  private[this] def clientMethodImpl(m: MethodDescriptor, blocking: Boolean) = PrinterEndo{ p =>
    m.streamType match {
      case StreamType.Unary =>
        if(blocking) {
          p.add(
            "override " + methodSignature(m, identity) + " = {"
          ).add(
            s"""  _root_.io.grpc.stub.ClientCalls.blockingUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), request)""",
            "}"
          )
        } else {
          p.add(
            "override " + methodSignature(m, "scala.concurrent.Future[" + _ + "]") + " = {"
          ).add(
            s"""  $guavaFuture2ScalaFuture($futureUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), request))""",
            "}"
          )
        }
      case StreamType.ServerStreaming =>
        p.add(
          "override " + methodSignature(m) + " = {"
        ).addI(
          s"${clientCalls.serverStreaming}(channel.newCall(${methodDescriptorName(m)}, options), request, observer)"
        ).add("}")
      case streamType =>
        val call = if (streamType == StreamType.ClientStreaming) {
          clientCalls.clientStreaming
        } else {
          clientCalls.bidiStreaming
        }

        p.add(
          "override " + methodSignature(m) + " = {"
        ).indent.add(
          s"$call(channel.newCall(${methodDescriptorName(m)}, options), observer)"
        ).outdent.add("}")
    }
  }

  private[this] val blockingClientImpl: PrinterEndo = { p =>
    val methods = service.getMethods.asScala.map(clientMethodImpl(_, true))

    val build =
      s"  override def build(channel: $channel, options: $callOptions): $blockingClientName = new $blockingClientName(channel, options)"

    p.add(
      s"class $blockingClientName(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$blockingClientName](channel, options) with $serviceBlocking {"
    ).withIndent(
      methods : _*
    ).add(
      build
    ).add(
      "}"
    )
  }

  private[this] val guavaFuture2ScalaFuture = "com.trueaccord.scalapb.grpc.Grpc.guavaFuture2ScalaFuture"

  private[this] val asyncClientName = service.getName + "AsyncClientImpl"

  private[this] val asyncClientImpl: PrinterEndo = { p =>
    val methods = service.getMethods.asScala.map(clientMethodImpl(_, false))

    val build =
      s"  override def build(channel: $channel, options: $callOptions): $asyncClientName = new $asyncClientName(channel, options)"

    p.add(
      s"class $asyncClientName(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$asyncClientName](channel, options) with $serviceFuture {"
    ).withIndent(
      methods : _*
    ).add(
      build
    ).add(
      "}"
    )
  }

  private[this] def methodDescriptorName(method: MethodDescriptor): String =
    "METHOD_" + method.getName.toUpperCase(Locale.ENGLISH)

  private[this] def methodDescriptor(method: MethodDescriptor) = {
    def marshaller(typeName: String) =
      s"new _root_.com.trueaccord.scalapb.grpc.Marshaller($typeName)"

    val methodType = method.streamType match {
      case StreamType.Unary => "UNARY"
      case StreamType.ClientStreaming => "CLIENT_STREAMING"
      case StreamType.ServerStreaming => "SERVER_STREAMING"
      case StreamType.Bidirectional => "BIDI_STREAMING"
    }

    val grpcMethodDescriptor = "_root_.io.grpc.MethodDescriptor"

s"""  private[this] val ${methodDescriptorName(method)}: $grpcMethodDescriptor[${method.scalaIn}, ${method.scalaOut}] =
    $grpcMethodDescriptor.create(
      $grpcMethodDescriptor.MethodType.$methodType,
      $grpcMethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"),
      ${marshaller(method.scalaIn)},
      ${marshaller(method.scalaOut)}
    )"""
  }

  private[this] val methodDescriptors: Seq[String] = service.getMethods.asScala.map(methodDescriptor)

  private[this] def callMethodName(method: MethodDescriptor) =
    methodName0(method) + "Method"

  private[this] def callMethod(method: MethodDescriptor) =
    method.streamType match {
      case StreamType.Unary =>
        s"${callMethodName(method)}(service, executionContext)"
      case _ =>
        s"${callMethodName(method)}(service)"
    }

  private[this] def createMethod(method: MethodDescriptor): String = {
    val executionContext = "executionContext"
    val name = callMethodName(method)
    val serviceImpl = "serviceImpl"
    method.streamType match {
      case StreamType.Unary =>
        val serverMethod = s"_root_.io.grpc.stub.ServerCalls.UnaryMethod[${method.scalaIn}, ${method.scalaOut}]"
s"""  def ${name}($serviceImpl: $serviceFuture, $executionContext: scala.concurrent.ExecutionContext): $serverMethod = {
    new $serverMethod {
      override def invoke(request: ${method.scalaIn}, observer: $streamObserver[${method.scalaOut}]): Unit =
        $serviceImpl.${methodName(method)}(request).onComplete {
          case scala.util.Success(value) =>
            observer.onNext(value)
            observer.onCompleted()
          case scala.util.Failure(error) =>
            observer.onError(error)
            observer.onCompleted()
        }($executionContext)
    }
  }"""
      case StreamType.ServerStreaming =>
        val serverMethod = s"_root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(request: ${method.scalaIn}, observer: $streamObserver[${method.scalaOut}]): Unit =
        $serviceImpl.${methodName0(method)}(request, observer)
    }
  }"""
      case _ =>
        val serverMethod = if(method.streamType == StreamType.ClientStreaming) {
          s"_root_.io.grpc.stub.ServerCalls.ClientStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"
        } else {
          s"_root_.io.grpc.stub.ServerCalls.BidiStreamingMethod[${method.scalaIn}, ${method.scalaOut}]"
        }

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(observer: $streamObserver[${method.scalaOut}]): $streamObserver[${method.scalaIn}] =
        $serviceImpl.${methodName0(method)}(observer)
    }
  }"""
    }
  }

  private[this] val bindService = {
    val executionContext = "executionContext"
    val methods = service.getMethods.asScala.map { m =>

      val call = m.streamType match {
        case StreamType.Unary => serverCalls.unary
        case StreamType.ClientStreaming => serverCalls.clientStreaming
        case StreamType.ServerStreaming => serverCalls.serverStreaming
        case StreamType.Bidirectional => serverCalls.bidiStreaming
      }

s""".addMethod(
      ${methodDescriptorName(m)},
      $call(
        ${callMethod(m)}
      )
    )"""
    }.mkString

    val serverServiceDef = "_root_.io.grpc.ServerServiceDefinition"

s"""def bindService(service: $serviceFuture, $executionContext: scala.concurrent.ExecutionContext): $serverServiceDef =
    $serverServiceDef.builder("${service.getFullName}")$methods.build()
  """
  }

  val objectName = service.getName + "Grpc"

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.add(
      "package " + service.getFile.scalaPackageName,
      "",
      "import scala.language.higherKinds",
      "",
      s"object $objectName {"
    ).seq(
      service.getMethods.asScala.map(createMethod)
    ).seq(
      methodDescriptors
    ).newline.withIndent(
      base,
      FunctionalPrinter.newline,
      blockingClientImpl,
      FunctionalPrinter.newline,
      asyncClientImpl
    ).newline.addI(
      bindService,
      s"def blockingClient(channel: $channel): $serviceBlocking = new $blockingClientName(channel)",
      s"def futureClient(channel: $channel): $serviceFuture = new $asyncClientName(channel)"
    ).add(
      ""
    ).outdent.add("}")
  }
}
