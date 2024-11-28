package scalapb.compiler

import com.google.protobuf.DescriptorProtos.MethodOptions.IdempotencyLevel
import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.compiler.ProtobufGenerator.asScalaDocBlock

import scala.jdk.CollectionConverters._

final class GrpcServicePrinter(service: ServiceDescriptor, implicits: DescriptorImplicits) {
  import implicits._
  private[this] def observer(typeParam: String): String = s"$streamObserver[$typeParam]"

  private[this] def serviceMethodSignature(method: MethodDescriptor, overrideSig: Boolean) = {
    val overrideStr = if (overrideSig) "override " else ""
    s"${method.deprecatedAnnotation}${overrideStr}def ${method.name}" + (method.streamType match {
      case StreamType.Unary =>
        s"(request: ${method.inputType.scalaType}): scala.concurrent.Future[${method.outputType.scalaType}]"
      case StreamType.ClientStreaming =>
        s"(responseObserver: ${observer(method.outputType.scalaType)}): ${observer(method.inputType.scalaType)}"
      case StreamType.ServerStreaming =>
        s"(request: ${method.inputType.scalaType}, responseObserver: ${observer(method.outputType.scalaType)}): _root_.scala.Unit"
      case StreamType.Bidirectional =>
        s"(responseObserver: ${observer(method.outputType.scalaType)}): ${observer(method.inputType.scalaType)}"
    })
  }

  private[this] def blockingMethodSignature(method: MethodDescriptor, overrideSig: Boolean) = {
    val overrideStr = if (overrideSig) "override " else ""
    s"${method.deprecatedAnnotation}${overrideStr}def ${method.name}" + (method.streamType match {
      case StreamType.Unary =>
        s"(request: ${method.inputType.scalaType}): ${method.outputType.scalaType}"
      case StreamType.ServerStreaming =>
        s"(request: ${method.inputType.scalaType}): scala.collection.Iterator[${method.outputType.scalaType}]"
      case _ => throw new IllegalArgumentException("Invalid method type.")
    })
  }

  private[this] def serviceTrait: PrinterEndo = { p =>
    p.call(generateScalaDoc(service))
      .add(s"trait ${service.name} extends _root_.scalapb.grpc.AbstractService {")
      .indent
      .add(
        s"override def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[${service.name}] = ${service.name}"
      )
      .print(service.methods) { case (p, method) =>
        p.call(generateScalaDoc(method)).add(serviceMethodSignature(method, overrideSig = false))
      }
      .outdent
      .add("}")
  }

  private[this] def serviceTraitCompanion: PrinterEndo = { p =>
    p.add(
      s"object ${service.name} extends _root_.scalapb.grpc.ServiceCompanion[${service.name}] {"
    ).indent
      .add(
        s"implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[${service.name}] = this"
      )
      .add(
        s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ${service.javaDescriptorSource}"
      )
      .add(
        s"def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = ${service.scalaDescriptorSource}"
      )
      .call(bindService)
      .outdent
      .add("}")
  }

  private[this] def blockingClientTrait: PrinterEndo = { p =>
    p.call(generateScalaDoc(service))
      .add(s"trait ${service.blockingClient} {")
      .indent
      .add(
        s"def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[${service.name}] = ${service.name}"
      )
      .print(service.methods.filter(_.canBeBlocking)) { case (p, method) =>
        p.call(generateScalaDoc(method)).add(blockingMethodSignature(method, overrideSig = false))
      }
      .outdent
      .add("}")
  }

  private[this] val channel     = "_root_.io.grpc.Channel"
  private[this] val callOptions = "_root_.io.grpc.CallOptions"

  private[this] val abstractStub   = "_root_.io.grpc.stub.AbstractStub"
  private[this] val stubFactory    = "_root_.io.grpc.stub.AbstractStub.StubFactory"
  private[this] val streamObserver = "_root_.io.grpc.stub.StreamObserver"

  private[this] val serverCalls = "_root_.io.grpc.stub.ServerCalls"
  private[this] val clientCalls = "_root_.scalapb.grpc.ClientCalls"

  private[this] val serverServiceDef = "_root_.io.grpc.ServerServiceDefinition"
  private[this] val executionContext = "executionContext"

  private[this] def clientMethodImpl(m: MethodDescriptor, blocking: Boolean) =
    PrinterEndo { p =>
      val sig =
        if (blocking) blockingMethodSignature(m, overrideSig = true) + " = {"
        else serviceMethodSignature(m, overrideSig = true) + " = {"

      val prefix = if (blocking) "blocking" else "async"

      val methodName = prefix + (m.streamType match {
        case StreamType.Unary           => "UnaryCall"
        case StreamType.ServerStreaming => "ServerStreamingCall"
        case StreamType.ClientStreaming => "ClientStreamingCall"
        case StreamType.Bidirectional   => "BidiStreamingCall"
      })

      val args = Seq("channel", m.grpcDescriptor.nameSymbol, "options") ++
        (if (m.isClientStreaming) Seq() else Seq("request")) ++
        (if ((m.isClientStreaming || m.isServerStreaming) && !blocking) Seq("responseObserver")
         else Seq())

      val body = s"${clientCalls}.${methodName}(${args.mkString(", ")})"
      p.call(generateScalaDoc(m)).add(sig).addIndented(body).add("}").newline
    }

  private def stubImplementation(
      className: String,
      baseClass: String,
      methods: Seq[PrinterEndo]
  ): PrinterEndo = { p =>
    val build =
      s"override def build(channel: $channel, options: $callOptions): ${className} = new $className(channel, options)"
    p.add(
      s"class $className(channel: $channel, options: $callOptions = $callOptions.DEFAULT) extends $abstractStub[$className](channel, options) with $baseClass {"
    ).indent
      .call(methods: _*)
      .add(build)
      .outdent
      .add("}")
  }

  private def stubCompanion(
      className: String
  ): PrinterEndo = { p =>
    val newStub =
      s"override def newStub(channel: $channel, options: $callOptions): $className = new $className(channel, options)"

    val implicitStub =
      s"implicit val stubFactory: $stubFactory[$className] = this"

    p.add(
      s"object $className extends $stubFactory[$className] {"
    ).indent
      .add(newStub)
      .newline
      .add(implicitStub)
      .outdent
      .add("}")
  }

  private[this] val blockingStub: PrinterEndo = {
    val methods = service.methods.filter(_.canBeBlocking).map(clientMethodImpl(_, true))
    stubImplementation(service.blockingStub, service.blockingClient, methods)
  }

  private[this] val stub: PrinterEndo = {
    val methods = service.getMethods.asScala.map(clientMethodImpl(_, false)).toSeq
    stubImplementation(service.stub, service.name, methods)
  }

  private[this] def methodDescriptor(method: MethodDescriptor) = PrinterEndo { p =>
    def marshaller(t: ExtendedMethodDescriptor#MethodTypeWrapper) =
      if (t.customScalaType.isDefined)
        s"_root_.scalapb.grpc.Marshaller.forTypeMappedType[${t.baseScalaType}, ${t.scalaType}]"
      else
        s"_root_.scalapb.grpc.Marshaller.forMessage[${t.scalaType}]"

    val methodType = method.streamType match {
      case StreamType.Unary           => "UNARY"
      case StreamType.ClientStreaming => "CLIENT_STREAMING"
      case StreamType.ServerStreaming => "SERVER_STREAMING"
      case StreamType.Bidirectional   => "BIDI_STREAMING"
    }

    val grpcMethodDescriptor = "_root_.io.grpc.MethodDescriptor"

    val idempotencyLevel = method.getOptions.getIdempotencyLevel
    val safe             = idempotencyLevel == IdempotencyLevel.NO_SIDE_EFFECTS
    val idempotent       = idempotencyLevel == IdempotencyLevel.IDEMPOTENT

    p.add(
      s"""${method.deprecatedAnnotation}val ${method.grpcDescriptor.nameSymbol}: $grpcMethodDescriptor[${method.inputType.scalaType}, ${method.outputType.scalaType}] =
         |  $grpcMethodDescriptor.newBuilder()
         |    .setType($grpcMethodDescriptor.MethodType.$methodType)
         |    .setFullMethodName($grpcMethodDescriptor.generateFullMethodName("${service.getFullName}", "${method.getName}"))
         |    .setSampledToLocalTracing(true)
         |    .setSafe($safe)
         |    .setIdempotent($idempotent)
         |    .setRequestMarshaller(${marshaller(method.inputType)})
         |    .setResponseMarshaller(${marshaller(method.outputType)})
         |    .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(${method.javaDescriptorSource}))
         |    .build()
         |""".stripMargin
    )
  }

  private[this] def serviceDescriptor(service: ServiceDescriptor) = {
    val grpcServiceDescriptor = "_root_.io.grpc.ServiceDescriptor"

    PrinterEndo(
      _.add(s"val ${service.grpcDescriptor.nameSymbol}: $grpcServiceDescriptor =").indent
        .add(s"""$grpcServiceDescriptor.newBuilder("${service.getFullName}")""")
        .indent
        .add(
          s""".setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(${service.getFile.fileDescriptorObject.fullName}.javaDescriptor))"""
        )
        .print(service.methods) { case (p, method) =>
          p.add(s".addMethod(${method.grpcDescriptor.nameSymbol})")
        }
        .add(".build()")
        .outdent
        .outdent
        .newline
    )
  }

  private[this] def addMethodImplementation(method: MethodDescriptor): PrinterEndo = PrinterEndo {
    _.add(".addMethod(")
      .add(s"  ${method.grpcDescriptor.nameSymbol},")
      .indent
      .call(PrinterEndo { p =>
        val call = method.streamType match {
          case StreamType.Unary           => s"$serverCalls.asyncUnaryCall"
          case StreamType.ClientStreaming => s"$serverCalls.asyncClientStreamingCall"
          case StreamType.ServerStreaming => s"$serverCalls.asyncServerStreamingCall"
          case StreamType.Bidirectional   => s"$serverCalls.asyncBidiStreamingCall"
        }

        val serviceImpl = "serviceImpl"

        method.streamType match {
          case StreamType.Unary =>
            p.add(
              s"""$call((request: ${method.inputType.scalaType}, observer: $streamObserver[${method.outputType.scalaType}]) => {
                 |  $serviceImpl.${method.name}(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
                 |    $executionContext)
                 |}))""".stripMargin
            )
          case StreamType.ServerStreaming =>
            p.add(
              s"""$call((request: ${method.inputType.scalaType}, observer: $streamObserver[${method.outputType.scalaType}]) => {
                 |  $serviceImpl.${method.name}(request, observer)
                 |}))""".stripMargin
            )
          case _ =>
            p.add(
              s"""$call((observer: $streamObserver[${method.outputType.scalaType}]) => {
                 |  $serviceImpl.${method.name}(observer)
                 |}))""".stripMargin
            )
        }
      })
      .outdent
  }

  private[this] val bindService = {
    val methods = service.methods.map(addMethodImplementation)

    PrinterEndo(
      _.add(
        s"""def bindService(serviceImpl: ${service.name}, $executionContext: scala.concurrent.ExecutionContext): $serverServiceDef ="""
      ).indent
        .add(s"""$serverServiceDef.builder(${service.grpcDescriptor.nameSymbol})""")
        .call(methods: _*)
        .add(".build()")
        .outdent
    )
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(
        "",
        s"${service.deprecatedAnnotation}object ${service.companionObject.nameSymbol} {"
      )
      .indent
      .call(service.methods.map(methodDescriptor): _*)
      .call(serviceDescriptor(service))
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
      .call(stubCompanion(service.stub))
      .newline
      .add(
        s"""def bindService(serviceImpl: ${service.name}, $executionContext: scala.concurrent.ExecutionContext): $serverServiceDef = ${service.name}.bindService(serviceImpl, executionContext)"""
      )
      .newline
      .add(
        s"def blockingStub(channel: $channel): ${service.blockingStub} = new ${service.blockingStub}(channel)"
      )
      .newline
      .add(s"def stub(channel: $channel): ${service.stub} = new ${service.stub}(channel)")
      .newline
      .add(
        s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = ${service.javaDescriptorSource}"
      )
      .newline
      .outdent
      .add("}")
  }

  def generateScalaDoc(service: ServiceDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(service.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty))
    fp.add(lines: _*)
  }

  def generateScalaDoc(method: MethodDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(method.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty))
    fp.add(lines: _*)
  }
}
