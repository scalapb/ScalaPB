package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{MethodDescriptor, ServiceDescriptor}
import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo

final class GrpcServiceIFacePrinter(service: ServiceDescriptor, override val params: GeneratorParams) extends DescriptorPimps {
  
  private[this] def service(reqTypeParam: String, repTypeParam: String): String = s"Service[$reqTypeParam, $repTypeParam]"

  private[this] val serviceTypeAlias: String = s"type Service[Req, Rep] = Req => scala.concurrent.Future[Rep]"

  private[this] def serviceParamSignature(method: MethodDescriptor) =
    s"${method.name}: ${service(method.scalaIn, method.scalaOut)}"

  private[this] def methodAlias(method: MethodDescriptor) = s"${method.name}Alias"

  private[this] def serviceAliasDefinition(method: MethodDescriptor) =
    s"val ${methodAlias(method)}: ${service(method.scalaIn, method.scalaOut)} = ${method.name}"

  private[this] def serviceMethodSignature(method: MethodDescriptor) = 
    s"def ${method.name}(request: ${method.scalaIn}): scala.concurrent.Future[${method.scalaOut}]"

  private[this] def serviceInterfaceMethodsDefinition(method: MethodDescriptor) =
    s"${serviceMethodSignature(method)} = ${methodAlias(method)}(request)"

  private[this] def serviceImport = s"import ${service.objectName}._"

  private[this] def serviceIFaceConstructor: PrinterEndo = {
    val params: String = service.methods.map(m => serviceParamSignature(m)).mkString(",\n")

    val aliases: PrinterEndo = { p =>
      p.seq(service.methods.map(m => serviceAliasDefinition(m)))
    }

    val implementations: PrinterEndo = { p =>
      p.seq(service.methods.map(m => serviceInterfaceMethodsDefinition(m)))
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
      .add(s"object ${service.objectName}IFace {")
      .indent
      .add(serviceTypeAlias)
      .newline
      .call(serviceIFaceConstructor)
      .outdent
      .add("}")
  }
}
