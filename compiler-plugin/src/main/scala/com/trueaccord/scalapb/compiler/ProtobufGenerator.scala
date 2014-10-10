package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors._
import com.google.protobuf.{Descriptors, ByteString}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scala.collection.JavaConversions._

object ProtobufGenerator {

  val SCALA_RESERVED_WORDS = Set(
    "abstract", "case", "catch", "class", "def",
    "do", "else", "extends", "false", "final",
    "finally", "for", "forSome", "if", "implicit",
    "import", "lazy", "match", "new", "null",
    "object", "override", "package", "private", "protected",
    "return", "sealed", "super", "this", "throw",
    "trait", "try", "true", "type", "val",
    "var", "while", "with", "yield",
    "val", "var", "def", "if", "ne", "case")

  implicit class AsSymbolPimp(val s: String) extends AnyVal {
    def asSymbol: String = if (SCALA_RESERVED_WORDS.contains(s)) s"`$s`" else s
  }

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

  def javaPackageAsSymbol(file: FileDescriptor): String =
    javaPackage(file).split('.').map(_.asSymbol).mkString(".")

  def scalaFullOuterObjectName(file: FileDescriptor) = {
    val pkg = javaPackageAsSymbol(file)
    if (pkg.isEmpty) scalaOuterObjectName(file)
    else pkg + "." + scalaOuterObjectName(file)
  }

  def javaFullOuterClassName(file: FileDescriptor) = {
    val pkg = javaPackageAsSymbol(file)
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
    prefix + last.asSymbol
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
    javaOuterClassName(file) + "PB"

  def printEnum(e: EnumDescriptor, printer: FunctionalPrinter): FunctionalPrinter = {
    val javaName = fullJavaName(e)
    val name = e.getName.asSymbol
    printer
      .add(s"object $name extends Enumeration {")
      .indent
      .print(e.getValues) {
        case (v, p) => p.add(s"""val ${v.getName.asSymbol} = Value(${v.getNumber}, "${v.getName}")""")
    }
      .add(s"def fromJavaValue(pbJavaSource: $javaName): Value = apply(pbJavaSource.getNumber)")
      .add(s"def toJavaValue(pbScalaSource: Value): $javaName = $javaName.valueOf(pbScalaSource.id)")
      .add(s"""lazy val descriptor = Descriptors.EnumDescriptor(${e.getIndex}, "${e.getName}", this)""")
      .outdent
      .add("}")
      .add(s"type $name = $name.Value")
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

  def escapeString(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
  }

  def byteArrayAsBase64Literal(buffer: Array[Byte]): String = {
    "\"\"\"" + new sun.misc.BASE64Encoder().encode(buffer) + "\"\"\""
  }

  def defaultValueForGet(field: FieldDescriptor) = {
    // Needs to be 'def' and not val since for some of the cases it's invalid to call it.
    def defaultValue = field.getDefaultValue
    field.getJavaType match {
      case FieldDescriptor.JavaType.INT => defaultValue.toString
      case FieldDescriptor.JavaType.LONG => defaultValue.toString + "L"
      case FieldDescriptor.JavaType.FLOAT =>
        val f = defaultValue.asInstanceOf[Float]
        if (f.isPosInfinity) "Float.PositiveInfinity"
        else if (f.isNegInfinity) "Float.NegativeInfinity"
        else f.toString + "f"
      case FieldDescriptor.JavaType.DOUBLE =>
        val d = defaultValue.asInstanceOf[Double]
        if (d.isPosInfinity) "Double.PositiveInfinity"
        else if (d.isNegInfinity) "Double.NegativeInfinity"
        else d.toString
      case FieldDescriptor.JavaType.BOOLEAN => Boolean.unbox(defaultValue.asInstanceOf[java.lang.Boolean])
      case FieldDescriptor.JavaType.BYTE_STRING => defaultValue.asInstanceOf[ByteString]
        .map(_.toString).mkString("Array[Byte](", ", ", ")")
      case FieldDescriptor.JavaType.STRING => escapeString(defaultValue.asInstanceOf[String])
      case FieldDescriptor.JavaType.MESSAGE =>
        fullScalaName(field.getMessageType) + ".defaultInstance"
      case FieldDescriptor.JavaType.ENUM =>
        fullScalaName(field.getEnumType) + "." + defaultValue.asInstanceOf[EnumValueDescriptor].getName.asSymbol
    }
  }

  def defaultValueForDefaultInstance(field: FieldDescriptor) =
    if (field.isOptional) "None"
    else if (field.isRepeated) "Nil"
    else defaultValueForGet(field)

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
    val scalaGetter = scalaObject + "." + snakeCaseToCamelCase(field.getName).asSymbol

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

    def generateGetField(message: Descriptor)(fp: FunctionalPrinter) = {
      val signature = "def getField(field: Descriptors.FieldDescriptor): Any = "
      if (message.getFields.nonEmpty)
        fp.add(signature + "{")
          .indent
          .add("field.number match {")
          .indent
          .print(message.getFields) {
          case (f, fp) => fp.add(s"case ${f.getNumber} => ${snakeCaseToCamelCase(f.getName).asSymbol}")
        }
          .outdent
          .add("}")
          .outdent
          .add("}")
      else fp.add(signature + "throw new MatchError(field)")
    }

  def printMessage(message: Descriptor,
                   printer: FunctionalPrinter): FunctionalPrinter = {
    val className = message.getName.asSymbol

    val myFullScalaName = fullScalaName(message).asSymbol
    val myFullJavaName = fullJavaName(message)
    printer
      .add(s"case class $className(")
      .indent
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val typeName = getScalaTypeName(field)
        val ctorDefaultValue = if (field.isOptional) " = None"
          else if (field.isRepeated) " = Nil"
          else ""
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"${fieldName.asSymbol}: $typeName$ctorDefaultValue$lineEnd")
    }
    .add(") extends com.trueaccord.scalapb.GeneratedMessage {")
    .outdent
      .add(s"def toByteArray: Array[Byte] =")
      .add(s"  $myFullScalaName.toJavaProto(this).toByteArray")
      .print(message.getFields) {
      case (field, printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName).asSymbol
        val withMethod = "with" + snakeCaseToCamelCase(field.getName, true)
        val clearMethod = "clear" + snakeCaseToCamelCase(field.getName, true)
        val p0 = if (field.isOptional) {
          val getter = "get" + snakeCaseToCamelCase(field.getName, true)
          val default = defaultValueForGet(field)
          printer.add(s"def $getter = ${fieldName}.getOrElse($default)")
        } else printer
        val p1 = p0.add(s"def $withMethod(${fieldName}: ${getScalaTypeName(field)}) = copy(${fieldName} = ${fieldName})")
        if (field.isOptional || field.isRepeated)  {
          p1.add(s"def $clearMethod = copy(${fieldName} = ${if (field.isOptional) "None" else "Nil"})")
        } else p1
    }
      .call(generateGetField(message))
      .add(s"override def toString: String = com.google.protobuf.TextFormat.printToString($myFullScalaName.toJavaProto(this))")
      .add(s"def companion = $myFullScalaName")
    .outdent
    .add("}")
    .add("")
    .add(s"object $className extends com.trueaccord.scalapb.GeneratedMessageCompanion[$className] {")
    .indent

      // toJavaProto
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

      // fromJavaProto
    .add(s"def fromJavaProto(javaPbSource: $myFullJavaName): $myFullScalaName = $myFullScalaName(")
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val conversion = javaFieldToScala("javaPbSource", field)
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"${fieldName.asSymbol} = $conversion$lineEnd")
    }
      .outdent
      .add(")")

      // fromFieldsMap
      .add(s"def fromFieldsMap(fieldsMap: Map[Int, Any]): $myFullScalaName = $myFullScalaName(")
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val typeName = getScalaTypeName(field)
        val mapGetter = if (field.isOptional)
            s"fieldsMap.getOrElse(${field.getNumber}, None).asInstanceOf[$typeName]"
        else if (field.isRepeated)
            s"fieldsMap.getOrElse(${field.getNumber}, Nil).asInstanceOf[$typeName]"
        else
            s"fieldsMap(${field.getNumber}).asInstanceOf[$typeName]"
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"${fieldName.asSymbol} = $mapGetter$lineEnd")
    }
      .outdent
      .add(")")

      // fromAscii
      .add(s"override def fromAscii(ascii: String): $myFullScalaName = {")
      .add(s"  val javaProtoBuilder = $myFullJavaName.newBuilder")
      .add(s"  com.google.protobuf.TextFormat.merge(ascii, javaProtoBuilder)")
      .add(s"  fromJavaProto(javaProtoBuilder.build)")
      .add(s"}")
      .add(s"""lazy val descriptor = new Descriptors.MessageDescriptor("${message.getName}", this,""")
      .add(s"""  None, m = Seq(${message.getNestedTypes.map(m => fullScalaName(m) + ".descriptor").mkString(", ")}),""")
      .add(s"""  e = Seq(${message.getEnumTypes.map(m => fullScalaName(m) + ".descriptor").mkString(", ")}), """)
      .add(s"""  f = ${scalaFullOuterObjectName(message.getFile)}.internalFieldsFor("${myFullScalaName}"))""")
      .add(s"lazy val defaultInstance = $myFullScalaName(")
      .indent
      .print(message.getFields.zipWithIndex) {
      case ((field, index), printer) =>
        val fieldName = snakeCaseToCamelCase(field.getName)
        val default = defaultValueForDefaultInstance(field)
        val lineEnd = if (index < message.getFields.size() - 1) "," else ""
        printer.add(s"${fieldName.asSymbol} = $default$lineEnd")
    }
      .outdent
      .add(")")
      .add(s"def parseFrom(pbByteArraySource: Array[Byte]): $myFullScalaName =")
      .add(s"  fromJavaProto($myFullJavaName.parseFrom(pbByteArraySource))")
      .print(message.getEnumTypes)(printEnum)
      .print(message.getNestedTypes)(printMessage)
      .add(s"implicit def messageCompanion: com.trueaccord.scalapb.GeneratedMessageCompanion[$className] = this")
    .outdent
    .add("}")
    .add("")
  }

  def generateInternalFields(message: Descriptor, fp: FunctionalPrinter): FunctionalPrinter = {
    def makeDescriptor(field: FieldDescriptor): String = {
      val index = field.getIndex
      val label = if (field.isOptional) "Descriptors.Optional"
      else if (field.isRepeated) "Descriptors.Repeated"
      else if (field.isRequired) "Descriptors.Required"
      else throw new IllegalArgumentException()

      val t = field.getJavaType match {
        case t if field.getJavaType != JavaType.MESSAGE && field.getJavaType != JavaType.ENUM =>
          s"Descriptors.PrimitiveType(com.google.protobuf.Descriptors.FieldDescriptor.JavaType.$t)"
        case JavaType.MESSAGE => "Descriptors.MessageType(" + fullScalaName(field.getMessageType) + ".descriptor)"
        case JavaType.ENUM => "Descriptors.EnumType(" + fullScalaName(field.getEnumType) + ".descriptor)"
      }
      s"""Descriptors.FieldDescriptor($index, ${field.getNumber}, "${field.getName}", $label, $t)"""
    }

    fp.add(s"""case "${fullScalaName(message)}" => Seq(${message.getFields.map(makeDescriptor).mkString(", ")})""")
      .print(message.getNestedTypes)(generateInternalFields)
  }

  def generateInternalFieldsFor(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter =
    if (file.getMessageTypes.nonEmpty) {
      fp.add("def internalFieldsFor(scalaName: String): Seq[Descriptors.FieldDescriptor] = scalaName match {")
        .indent
        .print(file.getMessageTypes)(generateInternalFields)
        .outdent
        .add("}")
    } else fp

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
      "import com.trueaccord.scalapb.Descriptors",
      "",
      s"object ${scalaOuterObjectName(file)} {")
      .indent
      .print(file.getEnumTypes)(printEnum)
      .print(file.getMessageTypes)(printMessage)
      .call(generateInternalFieldsFor(file))
      .outdent
      .add("}")
    b.setContent(p1.toString)
    b.build
  }
}
