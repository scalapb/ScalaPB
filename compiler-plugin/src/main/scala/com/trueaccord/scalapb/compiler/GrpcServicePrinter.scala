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
            s"""  ${m.scalaOut}.fromJavaProto(_root_.io.grpc.stub.ClientCalls.blockingUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), ${m.scalaIn}.toJavaProto(request)))""",
            "}"
          )
        } else {
          p.add(
            "override " + methodSignature(m, "scala.concurrent.Future[" + _ + "]") + " = {"
          ).add(
            s"""  $guavaFuture2ScalaFuture($futureUnaryCall(channel.newCall(${methodDescriptorName(m)}, options), ${m.scalaIn}.toJavaProto(request)))(${m.scalaOut}.fromJavaProto(_))""",
            "}"
          )
        }
      case StreamType.ServerStreaming =>
        p.add(
          "override " + methodSignature(m) + " = {"
        ).addI(
          s"${clientCalls.serverStreaming}(channel.newCall(${methodDescriptorName(m)}, options), ${m.scalaIn}.toJavaProto(request), $contramapObserver(observer)(${m.scalaOut}.fromJavaProto))"
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
          s"$contramapObserver("
        ).indent.add(
          s"$call(channel.newCall(${methodDescriptorName(m)}, options), $contramapObserver(observer)(${m.scalaOut}.fromJavaProto)))(${m.scalaIn}.toJavaProto"
        ).outdent.add(")").outdent.add("}")
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

  private[this] val contramapObserver = "contramapObserver"
  private[this] val contramapObserverImpl = s"""private[this] def $contramapObserver[A, B](observer: ${observer("A")})(f: B => A): ${observer("B")} =
    new ${observer("B")} {
      override def onNext(value: B): Unit = observer.onNext(f(value))
      override def onError(t: Throwable): Unit = observer.onError(t)
      override def onCompleted(): Unit = observer.onCompleted()
    }"""

  private[this] val guavaFuture2ScalaFuture = "guavaFuture2ScalaFuture"

  private[this] val guavaFuture2ScalaFutureImpl = {
    s"""private[this] def $guavaFuture2ScalaFuture[A, B](guavaFuture: _root_.com.google.common.util.concurrent.ListenableFuture[A])(converter: A => B): scala.concurrent.Future[B] = {
    val p = scala.concurrent.Promise[B]()
    _root_.com.google.common.util.concurrent.Futures.addCallback(guavaFuture, new _root_.com.google.common.util.concurrent.FutureCallback[A] {
      override def onFailure(t: Throwable) = p.failure(t)
      override def onSuccess(a: A) = p.success(converter(a))
    })
    p.future
  }"""
  }

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
    val inJava = method.getInputType.javaTypeName
    val outJava = method.getOutputType.javaTypeName

    def marshaller(typeName: String) =
      s"_root_.io.grpc.protobuf.ProtoUtils.marshaller($typeName.getDefaultInstance)"

    val methodType = method.streamType match {
      case StreamType.Unary => "UNARY"
      case StreamType.ClientStreaming => "CLIENT_STREAMING"
      case StreamType.ServerStreaming => "SERVER_STREAMING"
      case StreamType.Bidirectional => "BIDI_STREAMING"
    }

    val grpcMethodDescriptor = "_root_.io.grpc.MethodDescriptor"

s"""  private[this] val ${methodDescriptorName(method)}: $grpcMethodDescriptor[$inJava, $outJava] =
    $grpcMethodDescriptor.create(
      $grpcMethodDescriptor.MethodType.$methodType,
      $grpcMethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"),
      ${marshaller(inJava)},
      ${marshaller(outJava)}
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
    val javaIn = method.getInputType.javaTypeName
    val javaOut = method.getOutputType.javaTypeName
    val executionContext = "executionContext"
    val name = callMethodName(method)
    val serviceImpl = "serviceImpl"
    method.streamType match {
      case StreamType.Unary =>
        val serverMethod = s"_root_.io.grpc.stub.ServerCalls.UnaryMethod[$javaIn, $javaOut]"
s"""  def ${name}($serviceImpl: $serviceFuture, $executionContext: scala.concurrent.ExecutionContext): $serverMethod = {
    new $serverMethod {
      override def invoke(request: $javaIn, observer: $streamObserver[$javaOut]): Unit =
        $serviceImpl.${methodName(method)}(${method.scalaIn}.fromJavaProto(request)).onComplete {
          case scala.util.Success(value) =>
            observer.onNext(${method.scalaOut}.toJavaProto(value))
            observer.onCompleted()
          case scala.util.Failure(error) =>
            observer.onError(error)
            observer.onCompleted()
        }($executionContext)
    }
  }"""
      case StreamType.ServerStreaming =>
        val serverMethod = s"_root_.io.grpc.stub.ServerCalls.ServerStreamingMethod[$javaIn, $javaOut]"

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(request: $javaIn, observer: $streamObserver[$javaOut]): Unit =
        $serviceImpl.${methodName0(method)}(${method.scalaIn}.fromJavaProto(request), $contramapObserver(observer)(${method.scalaOut}.toJavaProto))
    }
  }"""
      case _ =>
        val serverMethod = if(method.streamType == StreamType.ClientStreaming) {
          s"_root_.io.grpc.stub.ServerCalls.ClientStreamingMethod[$javaIn, $javaOut]"
        } else {
          s"_root_.io.grpc.stub.ServerCalls.BidiStreamingMethod[$javaIn, $javaOut]"
        }

        s"""  def ${name}($serviceImpl: $serviceFuture): $serverMethod = {
    new $serverMethod {
      override def invoke(observer: $streamObserver[$javaOut]): $streamObserver[$javaIn] =
        $contramapObserver($serviceImpl.${methodName0(method)}($contramapObserver(observer)(${method.scalaOut}.toJavaProto)))(${method.scalaIn}.fromJavaProto)
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
      guavaFuture2ScalaFutureImpl,
      contramapObserverImpl,
      s"def blockingClient(channel: $channel): $serviceBlocking = new $blockingClientName(channel)",
      s"def futureClient(channel: $channel): $serviceFuture = new $asyncClientName(channel)"
    ).add(
      ""
    ).outdent.add("}")
  }
}
