package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import com.trueaccord.scalapb.compiler._, FunctionalPrinter.PrinterEndo

final class GrpcServiceIFacePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends GrpcServicePrinterCommons(params) {
  
  private[this] def unaryService(reqTypeParam: String, repTypeParam: String): String = s"Service[$reqTypeParam, $repTypeParam]"

  private[this] def clientStreamingService(reqTypeParam: String, repTypeParam: String): String = s"ClientStreamingService[$repTypeParam, $reqTypeParam]"

  private[this] def serverStreamingService(reqTypeParam: String, reqTypeParam2: String): String = s"ServerStreamingService[$reqTypeParam, $reqTypeParam2]"

  private[this] def bidirectionalService(reqTypeParam: String, repTypeParam: String): String = s"BidirectionalService[$repTypeParam, $reqTypeParam]"

  private[this] val unaryServiceTypeAlias: String = s"type Service[Req, Rep] = Req => scala.concurrent.Future[Rep]"

  private[this] val clientStreamingServiceTypeAlias: String = "type ClientStreamingService[Req, Rep] = _root_.io.grpc.stub.StreamObserver[Req] => _root_.io.grpc.stub.StreamObserver[Rep]"

  private[this] val serverStreamingServiceTypeAlias: String = "type ServerStreamingService[Req, Req2] = (Req, _root_.io.grpc.stub.StreamObserver[Req2]) => Unit"

  private[this] val bidirectionalServiceTypeAlias: String = "type BidirectionalService[Req, Rep] = _root_.io.grpc.stub.StreamObserver[Req] => _root_.io.grpc.stub.StreamObserver[Rep]"

  private[this] def functionTypeSig(from: String, to: String): String = s"$from => $to"

  private[this] def serviceParam(method: MethodDescriptor) = s"${method.name}: ${serviceTypeSignature(method)}"

  private[this] def serviceTypeSignature(method: MethodDescriptor) = 
    (method.streamType match {
      case StreamType.Unary =>
        s"${unaryService(method.scalaIn, method.scalaOut)}"
      case StreamType.ClientStreaming =>
        s"${clientStreamingService(method.scalaIn, method.scalaOut)}"
      case StreamType.ServerStreaming =>
        s"${serverStreamingService(method.scalaIn, method.scalaOut)}"
      case StreamType.Bidirectional =>
        s"${bidirectionalService(method.scalaIn, method.scalaOut)}"
    })
  private[this] def methodAlias(method: MethodDescriptor) = s"${method.name}Alias"
  
  private[this] def serviceAliasDefinition(method: MethodDescriptor) =
    s"val ${methodAlias(method)}: ${serviceTypeSignature(method)} = ${method.name}"

  private[this] def serviceInterfaceMethodDefinition(method: MethodDescriptor) = 
    s"${serviceMethodSignature(method)} = " + (method.streamType match {
      case StreamType.Unary =>
        s"${methodAlias(method)}(request)"
      case StreamType.ClientStreaming =>
         s"${methodAlias(method)}(responseObserver)"
      case StreamType.ServerStreaming =>
         s"${methodAlias(method)}(request, responseObserver)"
      case StreamType.Bidirectional =>
         s"${methodAlias(method)}(responseObserver)"
    })

  private[this] def serviceImport = s"import ${service.objectName}._"

  private[this] def serviceIFaceConstructor: PrinterEndo = {
    val params: String = service.methods.map(m => serviceParam(m)).mkString(",\n")

    val aliases: PrinterEndo = { p =>
      p.seq(service.methods.map(m => serviceAliasDefinition(m)))
    }

    val implementations: PrinterEndo = { p =>
      p.seq(service.methods.map(m => serviceInterfaceMethodDefinition(m)))
    }
    p =>
    p
      .add(s"def apply(")
      .indent
      .add(params)
      .outdent
      .add(s") = {")
      .indent
      .call(aliases)
      .add(s"new ${service.name} {")
      .indent
      .call(implementations)
      .outdent
      .add("}")
      .outdent
      .add("}")
  }

  def printService(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add("package " + service.getFile.scalaPackageName)
      .add(serviceImport)
      .newline
      .add(s"object ${service.objectName}ServiceI {")
      .indent
      .add(unaryServiceTypeAlias)
      .add(clientStreamingServiceTypeAlias)
      .add(serverStreamingServiceTypeAlias)
      .add(bidirectionalServiceTypeAlias)
      .newline
      .call(serviceIFaceConstructor)
      .outdent
      .add("}")
  }

}
