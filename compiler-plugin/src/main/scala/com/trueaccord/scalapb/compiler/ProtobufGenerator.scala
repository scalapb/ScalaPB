package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.{Descriptor, EnumDescriptor, FieldDescriptor, FileDescriptor}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scala.collection.JavaConversions._

object ProtobufGenerator {
  def handleCodeGeneratorRequest(request: CodeGeneratorRequest): CodeGeneratorResponse = {
    val fileProtosByName = request.getProtoFileList.map(n => n.getName -> n).toMap
    val b = CodeGeneratorResponse.newBuilder
    val filesByName: Map[String, FileDescriptor] =
      request.getProtoFileList.foldLeft[Map[String, FileDescriptor]](Map.empty) {
        case (acc, fp) =>
          val deps = fp.getDependencyList.map(acc)
          acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
      }
    request.getFileToGenerateList.foreach {
      name =>
        val file = filesByName(name)
        val responseFile = generateFile(file)
        b.addFile(responseFile)
    }
    b.build
  }

  def javaPackage(file: FileDescriptor): String = {
    if (file.getOptions.hasJavaPackage)
      file.getOptions.getJavaPackage
    else file.getPackage
  }

  def scalaFullOuterObjectName(file: FileDescriptor) = {
    val pkg = javaPackage(file)
    if (pkg.isEmpty) scalaOuterObjectName(file)
    else pkg + "." + scalaOuterObjectName(file)
  }

  def javaFullOuterClassName(file: FileDescriptor) = {
    val pkg = javaPackage(file)
    if (pkg.isEmpty) javaOuterClassName(file)
    else pkg + "." + javaOuterClassName(file)
  }

  def fullNameFromBase(base: String, fullName: String, file: FileDescriptor) = {
    val nameWithoutPackage = if (file.getPackage.isEmpty) fullName
    else fullName.substring(file.getPackage.size + 1)
    if (base.isEmpty) nameWithoutPackage else (base + "." + nameWithoutPackage)
  }

  def fullJavaName(fullName: String, file: FileDescriptor) =
    fullNameFromBase(javaFullOuterClassName(file), fullName, file)

  def fullScalaName(fullName: String, file: FileDescriptor) = {
    val s = fullNameFromBase(scalaFullOuterObjectName(file), fullName, file)
    val (prefix, last) = s.splitAt(s.lastIndexOf('.') + 1)
    prefix + "`" + last + "`"
  }

  def fullScalaName(message: Descriptor): String =
    fullScalaName(message.getFullName, message.getFile)

  def fullScalaName(enum: EnumDescriptor): String =
    fullScalaName(enum.getFullName, enum.getFile)

  def fullJavaName(message: Descriptor): String =
    fullJavaName(message.getFullName, message.getFile)

  def fullJavaName(enum: EnumDescriptor): String =
    fullJavaName(enum.getFullName, enum.getFile)

  private def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b.append)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString
  }

  def baseName(fileName: String) =
    fileName.split("/").last.replaceAll(raw"[.]proto$$|[.]protodevel", "")

  def javaOuterClassName(file: FileDescriptor) =
    if (file.getOptions.hasJavaOuterClassname)
      file.getOptions.getJavaOuterClassname
    else {
      snakeCaseToCamelCase(baseName(file.getName), true)
    }

  def scalaOuterObjectName(file: FileDescriptor): String =
    javaOuterClassName(file) + "Scala"

  def printEnum(e: EnumDescriptor, printer: FunctionalPrinter): FunctionalPrinter = {
    val javaName = fullJavaName(e)
    val name = e.getName
    printer
      .add(s"object `$name` extends Enumeration {")
      .indent
      .print(e.getValues) {
        case (v, p) => p.add(s"""val `${v.getName}` = Value(${v.getNumber}, "${v.getName}")""")
    }
      .add(s"def fromJavaValue(pbJavaSource: $javaName): Value = apply(pbJavaSource.getNumber)")
      .add(s"def toJavaValue(pbScalaSource: Value): $javaName = $javaName.valueOf(pbScalaSource.id)")
      .outdent
      .add("}")
      .add(s"type `$name` = `$name`.Value")
  }

  def getScalaTypeName(descriptor: FieldDescriptor): String = {
    val base = descriptor.getJavaType match {
      case FieldDescriptor.JavaType.INT => "Int"
      case FieldDescriptor.JavaType.LONG => "Long"
      case FieldDescriptor.JavaType.FLOAT => "Float"
      case FieldDescriptor.JavaType.DOUBLE => "Double"
      case FieldDescriptor.JavaType.BOOLEAN => "Boolean"
      case FieldDescriptor.JavaType.BYTE_STRING => "Array[Byte]"
      case FieldDescriptor.JavaType.STRING => "String"
      case FieldDescriptor.JavaType.MESSAGE => fullScalaName(descriptor.getMessageType)
      case FieldDescriptor.JavaType.ENUM => fullScalaName(descriptor.getEnumType)
    }
    if (descriptor.isOptional) s"Option[$base]"
    else if (descriptor.isRepeated) s"Seq[$base]"
    else base
  }

  sealed trait ValueConversion
  case class ConversionMethod(name: String) extends ValueConversion
  case class ConversionFunction(name: String) extends ValueConversion
  case class BoxFunction(name: String) extends ValueConversion
  case object NoOp extends ValueConversion

  def javaFieldToScala(container: String, field: FieldDescriptor) = {
    val javaGetter = container + ".get" + snakeCaseToCamelCase(field.getName, true)
    val javaHas = container + ".has" + snakeCaseToCamelCase(field.getName, true)

    val valueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => ConversionMethod("intValue")
      case FieldDescriptor.JavaType.LONG => ConversionMethod("longValue")
      case FieldDescriptor.JavaType.FLOAT => ConversionMethod("floatValue")
      case FieldDescriptor.JavaType.DOUBLE => ConversionMethod("doubleValue")
      case FieldDescriptor.JavaType.BOOLEAN => ConversionMethod("booleanValue")
      case FieldDescriptor.JavaType.BYTE_STRING => ConversionMethod("toByteArray")
      case FieldDescriptor.JavaType.STRING => NoOp
      case FieldDescriptor.JavaType.MESSAGE => ConversionFunction(
        fullScalaName(field.getMessageType) + ".fromJavaProto")
      case FieldDescriptor.JavaType.ENUM => ConversionFunction(
        fullScalaName(field.getEnumType) + ".fromJavaValue")
    }

    valueConversion match {
      case ConversionMethod(method) =>
        if (field.isRepeated) s"${javaGetter}List.map(_.$method)"
        else if (field.isOptional) s"if ($javaHas) Some($javaGetter.$method) else None"
        else s"$javaGetter.$method"
      case ConversionFunction(func) =>
        if (field.isRepeated) s"${javaGetter}List.map($func)"
        else if (field.isOptional) s"if ($javaHas) Some($func($javaGetter)) else None"
        else s"$func($javaGetter)"
      case BoxFunction(func) =>
        throw new RuntimeException("Unexpected method type")
      case NoOp =>
        if (field.isRepeated) s"${javaGetter}List.toSeq"
        else if (field.isOptional) s"if ($javaHas) Some($javaGetter) else None"
        else javaGetter
    }
  }

  def assignScalaFieldToJava(scalaObject: String,
                             javaObject: String, field: FieldDescriptor): String = {
    val javaSetter = javaObject +
      (if (field.isRepeated) ".addAll" else ".set") +
      snakeCaseToCamelCase(field.getName, true)
    val scalaGetter = scalaObject + ".`" + snakeCaseToCamelCase(field.getName) + "`"

    val valueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => BoxFunction("Int.box")
      case FieldDescriptor.JavaType.LONG => BoxFunction("Long.box")
      case FieldDescriptor.JavaType.FLOAT => BoxFunction("Float.box")
      case FieldDescriptor.JavaType.DOUBLE => BoxFunction("Double.box")
      case FieldDescriptor.JavaType.BOOLEAN => BoxFunction("Boolean.box")
      case FieldDescriptor.JavaType.BYTE_STRING => ConversionFunction("com.google.protobuf.ByteString.copyFrom")
      case FieldDescriptor.JavaType.STRING => NoOp
      case FieldDescriptor.JavaType.MESSAGE => ConversionFunction(
        fullScalaName(field.getMessageType) + ".toJavaProto")
      case FieldDescriptor.JavaType.ENUM => ConversionFunction(
        fullScalaName(field.getEnumType) + ".toJavaValue")
    }
    valueConversion match {
      case ConversionMethod(method) =>
        throw new RuntimeException("Unexpected method type")
      case ConversionFunction(func) =>
        if (field.isRepeated)
          s"$javaSetter($scalaGetter.map($func))"
        else if (field.isOptional)
          s"$scalaGetter.map($func).foreach($javaSetter)"
        else
          s"$javaSetter($func($scalaGetter))"
      case BoxFunction(func) if field.isRepeated =>
        s"$javaSetter($scalaGetter.map($func))"
      case NoOp | BoxFunction(_) =>
        if (field.isRepeated)
          s"$javaSetter($scalaGetter)"
        else if (field.isOptional)
          s"$scalaGetter.foreach($javaSetter)"
        else
          s"$javaSetter($scalaGetter)"
    }
  }

  def printMessage(message: Descriptor, printer: FunctionalPrinter): FunctionalPrinter = {
    val className = message.getName
    val myFullScalaName = fullScalaName(message)
    val myFullJavaName = fullJavaName(message)
    printer
      .add(s"case class `$className`(")
      .indent
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val typeName = getScalaTypeName(field)
        val defaultValue = if (field.isOptional) " = None" else ""
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"`$fieldName`: $typeName$defaultValue$lineEnd")
    }
    .add(") {")
    .outdent
    .outdent
    .add("}")
    .add("")
    .add(s"object `$className` extends com.trueaccord.scalapb.MessageCompanion[`$className`] {")
    .indent
    .add(s"def fromJavaProto(javaPbSource: $myFullJavaName): $myFullScalaName = $myFullScalaName(")
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val conversion = javaFieldToScala("javaPbSource", field)
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"`$fieldName` = $conversion$lineEnd")
    }
      .add(")")
      .outdent
      .add(s"def toJavaProto(scalaPbSource: $myFullScalaName): $myFullJavaName = {")
      .indent
      .add(s"val javaPbOut = $myFullJavaName.newBuilder")
      .print(message.getFields) {
      case (field, printer) =>
        printer.add(assignScalaFieldToJava("scalaPbSource", "javaPbOut", field))
    }
      .add("javaPbOut.build")
      .outdent
      .add("}")
      .add(s"def parseFrom(pbByteArraySource: Array[Byte]): $myFullScalaName =")
      .add(s"  fromJavaProto($myFullJavaName.parseFrom(pbByteArraySource))")
      .add(s"def serialize(javaPbSource: `$className`): Array[Byte] =")
      .add(s"  toJavaProto(javaPbSource).toByteArray")
      .print(message.getEnumTypes)(printEnum)
      .print(message.getNestedTypes)(printMessage)
    .outdent
    .add("}")
    .add("")
  }


  def generateFile(file: FileDescriptor): CodeGeneratorResponse.File = {
    val b = CodeGeneratorResponse.File.newBuilder()

    b.setName(javaPackage(file).replace('.', '/') + "/" + scalaOuterObjectName(file) + ".scala")
    val p0 = new FunctionalPrinter()
    val p1 = p0.add(
      "// Generated by the Scala Plugin for the Protocol Buffer Compiler.",
      "// Do not edit!",
      "",
      s"package ${javaPackage(file)}",
      "import scala.collection.JavaConversions._",
      "",
      s"object ${scalaOuterObjectName(file)} {")
      .indent
      .print(file.getEnumTypes)(printEnum)
      .print(file.getMessageTypes)(printMessage)
      .outdent
      .add("}")
    b.setContent(p1.toString)
    b.build
  }
}
