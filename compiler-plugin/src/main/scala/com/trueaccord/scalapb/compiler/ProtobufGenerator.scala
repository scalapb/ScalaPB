package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors._
import com.google.protobuf.CodedOutputStream
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.{ByteString => GoogleByteString}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo

import scala.collection.JavaConverters._

case class GeneratorParams(
  javaConversions: Boolean = false, flatPackage: Boolean = false,
  grpc: Boolean = false, singleLineToString: Boolean = false)

// Exceptions that are caught and passed upstreams as errors.
case class GeneratorException(message: String) extends Exception(message)

class ProtobufGenerator(val params: GeneratorParams) extends DescriptorPimps {
  def printEnum(printer: FunctionalPrinter, e: EnumDescriptor): FunctionalPrinter = {
    val name = e.nameSymbol
    printer
      .add(s"sealed trait $name extends _root_.com.trueaccord.scalapb.GeneratedEnum {")
      .indent
      .add(s"type EnumType = $name")
      .print(e.getValues.asScala) {
        case (p, v) => p.add(
          s"def ${v.isName}: Boolean = false")
      }
      .add(s"def isUnrecognized: Boolean = false")
      .add(s"def companion: _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[$name] = $name")
      .outdent
      .add("}")
      .add("")
      .add(s"object $name extends _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[$name] {")
      .indent
      .add(s"implicit def enumCompanion: _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[$name] = this")
      .print(e.getValues.asScala) {
        case (p, v) => p.addStringMargin(
          s"""@SerialVersionUID(0L)
             |case object ${v.getName.asSymbol} extends $name {
             |  val value = ${v.getNumber}
             |  val index = ${v.getIndex}
             |  val name = "${v.getName}"
             |  override def ${v.isName}: Boolean = true
             |}
             |""")
      }.addStringMargin(
        s"""@SerialVersionUID(0L)
           |case class Unrecognized(value: Int) extends $name {
           |  val name = "UNRECOGNIZED"
           |  val index = -1
           |  override def isUnrecognized: Boolean = true
           |}
           |""")
      .add(s"lazy val values = scala.collection.Seq(${e.getValues.asScala.map(_.getName.asSymbol).mkString(", ")})")
      .add(s"def fromValue(value: Int): $name = value match {")
      .print(e.valuesWithNoDuplicates) {
        case (p, v) => p.add(s"  case ${v.getNumber} => ${v.getName.asSymbol}")
      }
      .add(s"  case __other => Unrecognized(__other)")
      .add("}")
      .add(s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = ${e.javaDescriptorSource}")
      .when(params.javaConversions) {
      _.addStringMargin(
        s"""|def fromJavaValue(pbJavaSource: ${e.javaTypeName}): $name = fromValue(pbJavaSource.getNumber)
            |def toJavaValue(pbScalaSource: $name): ${e.javaTypeName} = ${e.javaTypeName}.forNumber(pbScalaSource.value)""")
    }
      .outdent
      .add("}")
  }

  def printOneof(printer: FunctionalPrinter, e: OneofDescriptor): FunctionalPrinter = {
    val possiblyConflictingName =
      e.getContainingType.getEnumTypes.asScala.exists(_.name == e.upperScalaName) ||
      e.getContainingType.nestedTypes.exists(_.scalaName == e.upperScalaName)

    if (possiblyConflictingName) {
      throw new GeneratorException(
        s"${e.getFile.getName}: The sealed trait generated for the oneof '${e.getName}' conflicts with " +
        s"another message name '${e.upperScalaName}'.")
    }
    printer
      .add(s"sealed trait ${e.upperScalaName} extends _root_.com.trueaccord.scalapb.GeneratedOneof {")
      .indent
      .add(s"def isEmpty: Boolean = false")
      .add(s"def isDefined: Boolean = true")
      .add(s"def number: Int")
      .print(e.fields) {
      case (p, v) => p
        .add(s"def is${v.upperScalaName}: Boolean = false")
      }
      .print(e.fields) {
        case (p, v) => p
          .add(s"def ${v.scalaName.asSymbol}: scala.Option[${v.scalaTypeName}] = None")
      }
      .outdent
      .addStringMargin(
        s"""}
           |object ${e.upperScalaName} extends {
           |  @SerialVersionUID(0L)
           |  case object Empty extends ${e.scalaTypeName} {
           |    override def isEmpty: Boolean = true
           |    override def isDefined: Boolean = false
           |    override def number: Int = 0
           |  }
           |""")
      .indent
      .print(e.fields) {
        case (p, v) =>
          p.addStringMargin(
            s"""@SerialVersionUID(0L)
               |case class ${v.upperScalaName}(value: ${v.scalaTypeName}) extends ${e.scalaTypeName} {
               |  override def is${v.upperScalaName}: Boolean = true
               |  override def ${v.scalaName.asSymbol}: scala.Option[${v.scalaTypeName}] = Some(value)
               |  override def number: Int = ${v.getNumber}
               |}""")
    }
    .outdent
    .add("}")
  }

  def escapeString(raw: String): String = raw.map {
    case u if u >= ' ' && u <= '~' => u.toString
    case '\b' => "\\b"
    case '\f' => "\\f"
    case '\n' => "\\n"
    case '\r' => "\\r"
    case '\t' => "\\t"
    case '\\' => "\\\\"
    case '\"' => "\\\""
    case '\'' => "\\\'"
    case c: Char => "\\u%4s".format(c.toInt.toHexString).replace(' ','0')
  }.mkString("\"", "", "\"")

  def defaultValueForGet(field: FieldDescriptor, uncustomized: Boolean = false) = {
    // Needs to be 'def' and not val since for some of the cases it's invalid to call it.
    def defaultValue = field.getDefaultValue
    val baseDefaultValue = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => defaultValue.toString
      case FieldDescriptor.JavaType.LONG => defaultValue.toString + "L"
      case FieldDescriptor.JavaType.FLOAT =>
        val f = defaultValue.asInstanceOf[Float]
        if (f.isPosInfinity) "Float.PositiveInfinity"
        else if (f.isNegInfinity) "Float.NegativeInfinity"
        else if (f.isNaN) "Float.NaN"
        else f.toString + "f"
      case FieldDescriptor.JavaType.DOUBLE =>
        val d = defaultValue.asInstanceOf[Double]
        if (d.isPosInfinity) "Double.PositiveInfinity"
        else if (d.isNegInfinity) "Double.NegativeInfinity"
        else if (d.isNaN) "Double.NaN"
        else d.toString
      case FieldDescriptor.JavaType.BOOLEAN => Boolean.unbox(defaultValue.asInstanceOf[java.lang.Boolean])
      case FieldDescriptor.JavaType.BYTE_STRING =>
        val d = defaultValue.asInstanceOf[GoogleByteString]
        if (d.isEmpty)
          "_root_.com.google.protobuf.ByteString.EMPTY"
        else
          d.asScala.map(_.toString).mkString("_root_.com.google.protobuf.ByteString.copyFrom(Array[Byte](", ", ", "))")
      case FieldDescriptor.JavaType.STRING => escapeString(defaultValue.asInstanceOf[String])
      case FieldDescriptor.JavaType.MESSAGE =>
        field.getMessageType.scalaTypeName + ".defaultInstance"
      case FieldDescriptor.JavaType.ENUM =>
        field.getEnumType.scalaTypeName + "." + defaultValue.asInstanceOf[EnumValueDescriptor].getName.asSymbol
    }
    if (!uncustomized && field.customSingleScalaTypeName.isDefined)
      s"${field.typeMapper}.toCustom($baseDefaultValue)" else baseDefaultValue
  }

  def defaultValueForDefaultInstance(field: FieldDescriptor) =
    if (field.supportsPresence) "None"
    else if (field.isMap) "scala.collection.immutable.Map.empty"
    else if (field.isRepeated) "Nil"
    else defaultValueForGet(field)

  def javaToScalaConversion(field: FieldDescriptor) = {
    val baseValueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => MethodApplication("intValue")
      case FieldDescriptor.JavaType.LONG => MethodApplication("longValue")
      case FieldDescriptor.JavaType.FLOAT => MethodApplication("floatValue")
      case FieldDescriptor.JavaType.DOUBLE => MethodApplication("doubleValue")
      case FieldDescriptor.JavaType.BOOLEAN => MethodApplication("booleanValue")
      case FieldDescriptor.JavaType.BYTE_STRING => Identity
      case FieldDescriptor.JavaType.STRING => Identity
      case FieldDescriptor.JavaType.MESSAGE => FunctionApplication(
        field.getMessageType.scalaTypeName + ".fromJavaProto")
      case FieldDescriptor.JavaType.ENUM => FunctionApplication(
        field.getEnumType.scalaTypeName + ".fromJavaValue")
    }
    baseValueConversion andThen toCustomTypeExpr(field)
  }


  def javaFieldToScala(javaHazzer: String, javaGetter: String, field: FieldDescriptor): String = {
    val valueConversion: Expression = javaToScalaConversion(field)

    if (field.supportsPresence)
      s"if ($javaHazzer) Some(${valueConversion.apply(javaGetter, isCollection = false)}) else None"
    else if (field.isRepeated)
      valueConversion(javaGetter + ".asScala", isCollection = true)
    else valueConversion(javaGetter, isCollection = false)
  }

  def javaFieldToScala(container: String, field: FieldDescriptor): String = {
    val javaHazzer = container + ".has" + field.upperJavaName
    val javaGetter = if (field.isRepeated)
      container + ".get" + field.upperJavaName + "List"
    else
      container + ".get" + field.upperJavaName

    javaFieldToScala(javaHazzer, javaGetter, field)
  }

  def javaMapFieldToScala(container: String, field: FieldDescriptor) = {
    // TODO(thesamet): if both unit conversions are NoOp, we can omit the map call.
    def unitConversion(n: String, field: FieldDescriptor) = javaToScalaConversion(field).apply(n, isCollection = false)
    s"${container}.get${field.upperScalaName}Map.asScala.map(__pv => (${unitConversion("__pv._1", field.mapType.keyField)}, ${unitConversion("__pv._2", field.mapType.valueField)})).toMap"
  }

  def scalaToJava(field: FieldDescriptor, boxPrimitives: Boolean): Expression = {
    def maybeBox(name: String) = if (boxPrimitives) FunctionApplication(name) else Identity

    field.getJavaType match {
      case FieldDescriptor.JavaType.INT => maybeBox("Int.box")
      case FieldDescriptor.JavaType.LONG => maybeBox("Long.box")
      case FieldDescriptor.JavaType.FLOAT => maybeBox("Float.box")
      case FieldDescriptor.JavaType.DOUBLE => maybeBox("Double.box")
      case FieldDescriptor.JavaType.BOOLEAN => maybeBox("Boolean.box")
      case FieldDescriptor.JavaType.BYTE_STRING => Identity
      case FieldDescriptor.JavaType.STRING => Identity
      case FieldDescriptor.JavaType.MESSAGE => FunctionApplication(
        field.getMessageType.scalaTypeName + ".toJavaProto")
      case FieldDescriptor.JavaType.ENUM => if (field.getFile.isProto3)
        (MethodApplication("value") andThen maybeBox("Int.box"))
      else
        FunctionApplication(field.getEnumType.scalaTypeName + ".toJavaValue")
    }
  }

  def assignScalaMapToJava(scalaObject: String, javaObject: String, field: FieldDescriptor): String = {
    def valueConvert(v: String, field: FieldDescriptor) =
      scalaToJava(field, boxPrimitives = true).apply(v, isCollection = false)

    val getMutableMap = s"getMutable${field.upperScalaName}" + (
      if (field.mapType.valueField.isEnum && field.getFile.isProto3) "Value" else "")

    s"""$javaObject
       |  .$getMutableMap()
       |  .putAll(
       |    $scalaObject.${fieldAccessorSymbol(field)}.map {
       |      __kv => (${valueConvert("__kv._1", field.mapType.keyField)}, ${valueConvert("__kv._2", field.mapType.valueField)})
       |  }.asJava)
       |""".stripMargin
  }

  def assignScalaFieldToJava(scalaObject: String,
                             javaObject: String, field: FieldDescriptor): String =
    if (field.isMap) assignScalaMapToJava(scalaObject, javaObject, field) else {
      val javaSetter = javaObject +
        (if (field.isRepeated) ".addAll" else
          ".set") + field.upperJavaName + (
        if (field.isEnum && field.getFile.isProto3) "Value" else "")
      val scalaGetter = scalaObject + "." + fieldAccessorSymbol(field)

      val scalaExpr = (toBaseTypeExpr(field) andThen scalaToJava(field, boxPrimitives = field.isRepeated)).apply(
        scalaGetter, isCollection = !field.isSingular)
      if (field.supportsPresence || field.isInOneof)
        s"$scalaExpr.foreach($javaSetter)"
      else
      if (field.isRepeated)
        s"$javaSetter($scalaExpr.asJava)"
      else
        s"$javaSetter($scalaExpr)"
    }

  def generateGetField(message: Descriptor)(fp: FunctionalPrinter) = {
    val signature = "def getField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): scala.Any = "
    if (message.fields.nonEmpty)
      fp.add(signature + "{")
        .indent
        .add("__field.getNumber match {")
        .indent
        .print(message.fields) {
          case (fp, f) =>
            val e = toBaseFieldType(f)
              .apply(fieldAccessorSymbol(f), isCollection = !f.isSingular)
            if (f.supportsPresence || f.isInOneof)
              fp.add(s"case ${f.getNumber} => $e.orNull")
            else if (f.isOptional) {
              // In proto3, drop default value
              fp.add(s"case ${f.getNumber} => {")
                .indent
                .add(s"val __t = $e")
                .add({
                  val cond = if (!f.isEnum)
                    s"__t != ${defaultValueForGet(f, uncustomized = true)}"
                  else
                    s"__t.getNumber() != 0"
                  s"if ($cond) __t else null"
                })
                .outdent
                .add("}")
            } else fp.add(s"case ${f.getNumber} => $e")
        }
        .outdent
        .add("}")
        .outdent
        .add("}")
    else fp.add(signature + "throw new MatchError(__field)")
  }

  def generateWriteSingleValue(field: FieldDescriptor, valueExpr: String)(fp: FunctionalPrinter):
  FunctionalPrinter = {
    if (field.isMessage) {
        fp.addStringMargin(
          s"""_output__.writeTag(${field.getNumber}, 2)
             |_output__.writeUInt32NoTag($valueExpr.serializedSize)
             |$valueExpr.writeTo(_output__)""")
    } else if (field.isEnum)
        fp.add(s"_output__.writeEnum(${field.getNumber}, $valueExpr.value)")
    else {
      val capTypeName = Types.capitalizedType(field.getType)
      fp.add(s"_output__.write$capTypeName(${field.getNumber}, $valueExpr)")
    }
  }

  def sizeExpressionForSingleField(field: FieldDescriptor, expr: String): String =
    if (field.isMessage) {
      val size = s"$expr.serializedSize"
      CodedOutputStream.computeTagSize(field.getNumber) + s" + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag($size) + $size"
    } else if(field.isEnum)
      s"_root_.com.google.protobuf.CodedOutputStream.computeEnumSize(${field.getNumber}, ${expr}.value)"
    else {
      val capTypeName = Types.capitalizedType(field.getType)
      s"_root_.com.google.protobuf.CodedOutputStream.compute${capTypeName}Size(${field.getNumber}, ${expr})"
    }

  def fieldAccessorSymbol(field: FieldDescriptor) =
    if (field.isInOneof)
      (field.getContainingOneof.scalaName.asSymbol + "." + field.scalaName.asSymbol)
    else
      field.scalaName.asSymbol

  def toBaseTypeExpr(field: FieldDescriptor) =
    if (field.customSingleScalaTypeName.isDefined) FunctionApplication(field.typeMapper + ".toBase")
    else Identity

  def toBaseFieldType(field: FieldDescriptor) = if (field.isEnum)
    (toBaseTypeExpr(field) andThen MethodApplication("javaValueDescriptor"))
  else toBaseTypeExpr(field)

  def toBaseType(field: FieldDescriptor)(expr: String) =
    toBaseTypeExpr(field).apply(expr, isCollection = false)

  def toCustomTypeExpr(field: FieldDescriptor) =
    if (field.customSingleScalaTypeName.isEmpty) Identity
    else FunctionApplication(s"${field.typeMapper}.toCustom")

  def toCustomType(field: FieldDescriptor)(expr: String) =
    toCustomTypeExpr(field).apply(expr, isCollection = false)

  def generateSerializedSizeForField(fp: FunctionalPrinter, field: FieldDescriptor): FunctionalPrinter = {
    val fieldNameSymbol = fieldAccessorSymbol(field)

    if (field.isRequired) {
      fp.add("__size += " + sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol)))
    } else if (field.isSingular) {
      fp.add(s"if (${toBaseType(field)(fieldNameSymbol)} != ${defaultValueForGet(field, true)}) { __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol))} }")
    } else if (field.isOptional) {
      fp.add(s"if ($fieldNameSymbol.isDefined) { __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol + ".get"))} }")
    } else if (field.isRepeated) {
      val tagSize = CodedOutputStream.computeTagSize(field.getNumber)
      if (!field.isPacked) {
        Types.fixedSize(field.getType) match {
          case Some(size) => fp.add(s"__size += ${size + tagSize} * $fieldNameSymbol.size")
          case None => fp.add(
            s"$fieldNameSymbol.foreach($fieldNameSymbol => __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol))})")
        }
      } else {
        val fieldName = field.scalaName
        fp
          .addStringMargin(
            s"""if($fieldNameSymbol.nonEmpty) {
               |  val __localsize = ${fieldName}SerializedSize
               |  __size += $tagSize + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
               |}""")
      }
    } else throw new RuntimeException("Should not reach here.")
  }

  def generateSerializedSize(message: Descriptor)(fp: FunctionalPrinter) = {
    if (message.fields.nonEmpty) {
      fp
      .add("@transient")
      .add("private[this] var __serializedSizeCachedValue: Int = 0")
      .add("private[this] def __computeSerializedValue(): Int = {")
      .indent
      .add("var __size = 0")
      .print(message.fields)(generateSerializedSizeForField)
      .add("__size")
      .outdent
      .add("}")
      .add("final override def serializedSize: Int = {")
      .indent
      .add("var read = __serializedSizeCachedValue")
      .add("if (read == 0) {")
      .indent
      .add("read = __computeSerializedValue()")
      .add("__serializedSizeCachedValue = read")
      .outdent
      .add("}")
      .add("read")
      .outdent
      .add("}")
    } else {
      fp.add("final override def serializedSize: Int = 0")
    }
  }

  def generateSerializedSizeForPackedFields(message: Descriptor)(fp: FunctionalPrinter) =
    fp
      .print(message.fields.filter(_.isPacked).zipWithIndex) {
      case (printer, (field, index)) =>
        val methodName = s"${field.scalaName}SerializedSize"
        printer
          .add(s"private[this] def $methodName = {") //closing brace is in each case
          .call({ fp =>
          Types.fixedSize(field.getType) match {
            case Some(size) =>
              fp.add(s"  $size * ${field.scalaName.asSymbol}.size").add("}")
            case None =>
              val capTypeName = Types.capitalizedType(field.getType)
              val sizeFunc = Seq(s"_root_.com.google.protobuf.CodedOutputStream.compute${capTypeName}SizeNoTag")
              val fromEnum = if (field.isEnum) Seq(s"(_: ${field.baseSingleScalaTypeName}).value") else Nil
              val fromCustom = if (field.customSingleScalaTypeName.isDefined)
                Seq(s"${field.typeMapper}.toBase")
              else Nil
              val funcs = sizeFunc ++ fromEnum ++ fromCustom
              fp
                .add(s"if (__${methodName}Field == 0) __${methodName}Field = ")
                .add(s"  ${field.scalaName.asSymbol}.map(${composeGen(funcs)}).sum")
                .add(s"__${methodName}Field")
                .add("}") // closing brace for the method
                .add(s"@transient private[this] var __${methodName}Field: Int = 0")
          }
        })
    }

  private def composeGen(funcs: Seq[String]) =
    if (funcs.length == 1) funcs(0)
    else s"(${funcs(0)} _)" + funcs.tail.map(func => s".compose($func)").mkString

  def generateWriteTo(message: Descriptor)(fp: FunctionalPrinter) =
    fp.add(s"def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): Unit = {")
      .indent
      .print(message.fields.sortBy(_.getNumber).zipWithIndex) {
        case (printer, (field, index)) =>
          val fieldNameSymbol = fieldAccessorSymbol(field)
          val capTypeName = Types.capitalizedType(field.getType)
          if (field.isPacked) {
            val writeFunc = composeGen(Seq(
              s"_output__.write${capTypeName}NoTag") ++ (
              if (field.isEnum) Seq(s"(_: ${field.baseSingleScalaTypeName}).value") else Nil
              ) ++ (
              if (field.customSingleScalaTypeName.isDefined)
                Seq(s"${field.typeMapper}.toBase")
              else Nil
              ))

            printer.addStringMargin(
              s"""if (${fieldNameSymbol}.nonEmpty) {
                 |  _output__.writeTag(${field.getNumber}, 2)
                 |  _output__.writeUInt32NoTag(${field.scalaName}SerializedSize)
                 |  ${fieldNameSymbol}.foreach($writeFunc)
                 |};""")
          } else if (field.isRequired) {
          generateWriteSingleValue(field, toBaseType(field)(fieldNameSymbol))(printer)
        } else if (field.isSingular) {
          // Singular that are not required are written only if they don't equal their default
          // value.
          printer
            .add(s"{")
            .indent
            .add(s"val __v = ${toBaseType(field)(fieldNameSymbol)}")
            .add(s"if (__v != ${defaultValueForGet(field, uncustomized = true)}) {")
            .indent
            .call(generateWriteSingleValue(field, "__v"))
            .outdent
            .add("}")
            .outdent
            .add("};")
        } else {
          printer
            .add(s"${fieldNameSymbol}.foreach { __v =>")
            .indent
            .call(generateWriteSingleValue(field, toBaseType(field)("__v")))
            .outdent
            .add("};")
        }
    }
      .outdent
      .add("}")

  def printConstructorFieldList(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val regularFields = message.fields.collect {
      case field if !field.isInOneof =>
      val typeName = field.scalaTypeName
      val ctorDefaultValue =
        if (field.isOptional && field.supportsPresence) " = None"
        else if (field.isSingular && !field.isRequired) " = " + defaultValueForGet(field)
        else if (field.isMap) " = scala.collection.immutable.Map.empty"
        else if (field.isRepeated) " = Nil"
        else ""
        s"${field.scalaName.asSymbol}: $typeName$ctorDefaultValue"
    }
    val oneOfFields = message.getOneofs.asScala.map {
      oneOf =>
        s"${oneOf.scalaName.asSymbol}: ${oneOf.scalaTypeName} = ${oneOf.empty}"
    }
    printer.addWithDelimiter(",")(regularFields ++ oneOfFields)
  }

  def generateMergeFrom(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeNameWithMaybeRoot(message)
    val requiredFieldMap: Map[FieldDescriptor, Int] = message.fields.filter(_.isRequired).zipWithIndex.toMap
    printer
      .add(
      s"def mergeFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): $myFullScalaName = {")
      .indent
      .print(message.fieldsWithoutOneofs)((printer, field) =>
        if (!field.isRepeated)
          printer.add(s"var __${field.scalaName} = this.${field.scalaName.asSymbol}")
        else if (field.isMap)
          printer.add(s"val __${field.scalaName} = (scala.collection.immutable.Map.newBuilder[${field.mapType.keyType}, ${field.mapType.valueType}] ++= this.${field.scalaName.asSymbol})")
        else
          printer.add(s"val __${field.scalaName} = (scala.collection.immutable.Vector.newBuilder[${field.singleScalaTypeName}] ++= this.${field.scalaName.asSymbol})")
      )
      .when(requiredFieldMap.nonEmpty) {
        fp =>
          // Sets the bit 0...(n-1) inclusive to 1.
          def hexBits(n: Int): String = "0x%xL".format((0 to (n-1)).map(i => (1L << i)).sum)
          val requiredFieldCount = requiredFieldMap.size
          val fullWords = (requiredFieldCount - 1) / 64
          val bits: Seq[String] = (1 to fullWords).map(_ => hexBits(64)) :+ hexBits(requiredFieldCount - 64 * fullWords)
          fp.print(bits.zipWithIndex) {
            case (fp, (bn, index)) =>
              fp.add(s"var __requiredFields$index: Long = $bn")
          }
      }
      .print(message.getOneofs.asScala)((printer, oneof) =>
        printer.add(s"var __${oneof.scalaName} = this.${oneof.scalaName.asSymbol}"))
      .addStringMargin(
        s"""var _done__ = false
           |while (!_done__) {
           |  val _tag__ = _input__.readTag()
           |  _tag__ match {
           |    case 0 => _done__ = true""")
      .print(message.fields) {
        (printer, field) =>

        val p = {
          val newValBase = if (field.isMessage) {
            val defInstance = s"${field.getMessageType.scalaTypeName}.defaultInstance"
            val baseInstance = if (field.isRepeated) defInstance else {
              val expr = if (field.isInOneof)
                fieldAccessorSymbol(field) else s"__${field.scalaName}"
              val mappedType =
                toBaseFieldType(field).apply(expr,
                  isCollection = !field.isSingular)
              if (field.isInOneof || field.supportsPresence) (mappedType + s".getOrElse($defInstance)")
              else mappedType
            }
            s"_root_.com.trueaccord.scalapb.LiteParser.readMessage(_input__, $baseInstance)"
          } else if (field.isEnum)
            s"${field.getEnumType.scalaTypeName}.fromValue(_input__.readEnum())"
          else s"_input__.read${Types.capitalizedType(field.getType)}()"

          val newVal = toCustomType(field)(newValBase)

          val updateOp =
            if (field.supportsPresence) s"__${field.scalaName} = Some($newVal)"
            else if (field.isInOneof) {
              s"__${field.getContainingOneof.scalaName} = ${field.oneOfTypeName}($newVal)"
            }
            else if (field.isRepeated) s"__${field.scalaName} += $newVal"
            else s"__${field.scalaName} = $newVal"

          printer.addStringMargin(
            s"""    case ${(field.getNumber << 3) + Types.wireType(field.getType)} =>
               |      $updateOp""")
            .when(field.isRequired) {
              p =>
              val fieldNumber = requiredFieldMap(field)
              p.add(s"      __requiredFields${fieldNumber/64} &= 0x${"%x".format(~(1L << fieldNumber))}L")
            }
        }

        if(field.isPackable) {
          val read = {
            val tmp = s"""_input__.read${Types.capitalizedType(field.getType)}"""
            if (field.isEnum)
              s"${field.getEnumType.scalaTypeName}.fromValue($tmp)"
            else tmp
          }
          val readExpr = toCustomType(field)(read)
          p.addStringMargin(
            s"""    case ${(field.getNumber << 3) + Types.WIRETYPE_LENGTH_DELIMITED} => {
               |      val length = _input__.readRawVarint32()
               |      val oldLimit = _input__.pushLimit(length)
               |      while (_input__.getBytesUntilLimit > 0) {
               |        __${field.scalaName} += $readExpr
               |      }
               |      _input__.popLimit(oldLimit)
               |    }""")
        } else p
    }
      .addStringMargin(
       s"""|    case tag => _input__.skipField(tag)
           |  }
           |}""")
      .when(requiredFieldMap.nonEmpty) {
        p =>
          val r = (0 until (requiredFieldMap.size + 63)/64).map(i => s"__requiredFields$i != 0L").mkString(" || ")
          p.add(s"""if (${r}) { throw new _root_.com.google.protobuf.InvalidProtocolBufferException("Message missing required fields.") } """)
      }
      .add(s"$myFullScalaName(")
      .indent.addWithDelimiter(",")(
        (message.fieldsWithoutOneofs ++ message.getOneofs.asScala).map {
          case e: FieldDescriptor if e.isRepeated =>
            s"  ${e.scalaName.asSymbol} = __${e.scalaName}.result()"
          case e: FieldDescriptor =>
            s"  ${e.scalaName.asSymbol} = __${e.scalaName}"
          case e: OneofDescriptor =>
            s"  ${e.scalaName.asSymbol} = __${e.scalaName}"
        })
      .outdent
      .add(")")
      .outdent
      .add("}")
  }

  def generateToJavaProto(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer.add(s"def toJavaProto(scalaPbSource: $myFullScalaName): ${message.javaTypeName} = {")
      .indent
      .add(s"val javaPbOut = ${message.javaTypeName}.newBuilder")
      .print(message.fields) {
        case (printer, field) =>
          printer.add(assignScalaFieldToJava("scalaPbSource", "javaPbOut", field))
      }
      .add("javaPbOut.build")
      .outdent
      .add("}")
  }

  def generateFromJavaProto(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer.add(s"def fromJavaProto(javaPbSource: ${message.javaTypeName}): $myFullScalaName = $myFullScalaName(")
      .indent
      .call {
      printer =>
        val normal = message.fields.collect {
          case field if !field.isInOneof =>
            val conversion = if (field.isMap) javaMapFieldToScala("javaPbSource", field)
            else javaFieldToScala("javaPbSource", field)
            Seq(s"${field.scalaName.asSymbol} = $conversion")
        }
        val oneOfs = message.getOneofs.asScala.map {
          case oneOf =>
            val javaEnumName = s"get${oneOf.upperScalaName}Case"
            val head = s"${oneOf.scalaName.asSymbol} = javaPbSource.$javaEnumName.getNumber match {"
            val body = oneOf.fields.map {
              field =>
                s"  case ${field.getNumber} => ${field.oneOfTypeName}(${javaFieldToScala("javaPbSource", field)})"
            }
            val tail = Seq(s"  case _ => ${oneOf.empty}", "}")
            Seq(head) ++ body ++ tail
        }
        printer.addGroupsWithDelimiter(",")(normal ++ oneOfs)
    }
      .outdent
      .add(")")
  }

  def generateFromFieldsMap(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    def transform(field: FieldDescriptor) =
      (if (!field.isEnum) Identity else (
        MethodApplication("getNumber") andThen
          FunctionApplication(field.getEnumType.scalaTypeName + ".fromValue"))) andThen
        toCustomTypeExpr(field)

    val myFullScalaName = message.scalaTypeName
    printer.add(s"def fromFieldsMap(__fieldsMap: scala.collection.immutable.Map[_root_.com.google.protobuf.Descriptors.FieldDescriptor, scala.Any]): $myFullScalaName = {")
      .indent
      .add("require(__fieldsMap.keys.forall(_.getContainingType() == javaDescriptor), \"FieldDescriptor does not match message type.\")")
      .add("val __fields = javaDescriptor.getFields")
      .add(myFullScalaName + "(")
      .indent
      .call {
      printer =>
        val fields = message.fields.collect {
          case field if !field.isInOneof =>
            val baseTypeName = field.typeCategory(if (field.isEnum) "_root_.com.google.protobuf.Descriptors.EnumValueDescriptor" else field.baseSingleScalaTypeName)
            val e = if (field.supportsPresence)
              s"__fieldsMap.get(__fields.get(${field.getIndex})).asInstanceOf[$baseTypeName]"
            else if (field.isRepeated)
              s"__fieldsMap.getOrElse(__fields.get(${field.getIndex}), Nil).asInstanceOf[$baseTypeName]"
            else if (field.isRequired)
              s"__fieldsMap(__fields.get(${field.getIndex})).asInstanceOf[$baseTypeName]"
            else {
              // This is for proto3, no default value.
              val t = defaultValueForGet(field, uncustomized = true) + (if (field.isEnum)
                ".javaValueDescriptor" else "")
              s"__fieldsMap.getOrElse(__fields.get(${field.getIndex}), $t).asInstanceOf[$baseTypeName]"
            }

            val s = transform(field).apply(e, isCollection = !field.isSingular)
            if (field.isMap) s + "(scala.collection.breakOut)"
            else s
        }
        val oneOfs = message.getOneofs.asScala.toSeq.map {
          oneOf =>
            val elems = oneOf.fields.map {
              field =>
                val typeName = if (field.isEnum) "_root_.com.google.protobuf.Descriptors.EnumValueDescriptor" else field.baseSingleScalaTypeName
                val e = s"__fieldsMap.get(__fields.get(${field.getIndex})).asInstanceOf[scala.Option[$typeName]]"
                (transform(field) andThen FunctionApplication(field.oneOfTypeName)).apply(e, isCollection = true)
            } mkString (" orElse\n")
            s"${oneOf.scalaName.asSymbol} = $elems getOrElse ${oneOf.empty}"
        }
        printer.addWithDelimiter(",")(fields ++ oneOfs)
    }
      .outdent
      .add(")")
      .outdent
      .add("}")
  }

  def generateDescriptor(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(s"def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ${message.javaDescriptorSource}")
  }

  def generateDefaultInstance(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer
      .add(s"lazy val defaultInstance = $myFullScalaName(")
      .indent
      .addWithDelimiter(",")(message.fields.collect {
      case field if field.isRequired =>
        val default = defaultValueForDefaultInstance(field)
        s"${field.scalaName.asSymbol} = $default"
    })
      .outdent
      .add(")")
  }

  def generateMessageLens(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val className = message.scalaName
    def lensType(s: String) = s"_root_.com.trueaccord.lenses.Lens[UpperPB, $s]"

    printer.add(
      s"implicit class ${className}Lens[UpperPB](_l: _root_.com.trueaccord.lenses.Lens[UpperPB, ${message.scalaTypeName}]) extends _root_.com.trueaccord.lenses.ObjectLens[UpperPB, ${message.scalaTypeName}](_l) {")
      .indent
      .print(message.fields) {
      case (printer, field) =>
        val fieldName = field.scalaName.asSymbol
        if (!field.isInOneof) {
          if (field.supportsPresence) {
            val optionLensName = "optional" + field.upperScalaName
            printer
              .addStringMargin(
                s"""def $fieldName: ${lensType(field.singleScalaTypeName)} = field(_.${field.getMethod})((c_, f_) => c_.copy($fieldName = Some(f_)))
                   |def ${optionLensName}: ${lensType(field.scalaTypeName)} = field(_.$fieldName)((c_, f_) => c_.copy($fieldName = f_))""")
          } else
            printer.add(s"def $fieldName: ${lensType(field.scalaTypeName)} = field(_.$fieldName)((c_, f_) => c_.copy($fieldName = f_))")
        } else {
          val oneofName = field.getContainingOneof.scalaName.asSymbol
          printer
            .add(s"def $fieldName: ${lensType(field.scalaTypeName)} = field(_.${field.getMethod})((c_, f_) => c_.copy($oneofName = ${field.oneOfTypeName}(f_)))")
        }
    }
      .print(message.getOneofs.asScala) {
        case (printer, oneof) =>
          val oneofName = oneof.scalaName.asSymbol
          printer
            .add(s"def $oneofName: ${lensType(oneof.scalaTypeName)} = field(_.$oneofName)((c_, f_) => c_.copy($oneofName = f_))")
      }
      .outdent
      .add("}")
  }

  def generateFieldNumbers(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .print(message.fields) {
        case (printer, field) =>
          printer.add(s"final val ${field.fieldNumberConstantName} = ${field.getNumber}")
      }
  }

  def generateTypeMappers(fields: Seq[FieldDescriptor])(printer: FunctionalPrinter): FunctionalPrinter = {
    val customizedFields: Seq[(FieldDescriptor, String)] = for {
      field <- fields
      custom <- field.customSingleScalaTypeName
    } yield (field, custom)

    printer
      .print(customizedFields) {
      case (printer, (field, customType)) =>
        printer
          .add("@transient")
          .add(s"private val ${field.typeMapperValName}: _root_.com.trueaccord.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}] = implicitly[_root_.com.trueaccord.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}]]")
    }
  }

  def generateTypeMappersForMapEntry(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val pairToMessage = {
      val k = if (message.mapType.keyField.supportsPresence) "Some(__p._1)" else "__p._1"
      val v = if (message.mapType.valueField.supportsPresence) "Some(__p._2)" else "__p._2"
      s"__p => ${message.scalaTypeName}($k, $v)"
    }

    val messageToPair = {
      val k = if (message.mapType.keyField.supportsPresence) "__m.getKey" else "__m.key"
      val v = if (message.mapType.valueField.supportsPresence) "__m.getValue" else "__m.value"
      s"__m => ($k, $v)"
    }

    printer
      .addStringMargin(
        s"""implicit val keyValueMapper: _root_.com.trueaccord.scalapb.TypeMapper[${message.scalaTypeName}, ${message.mapType.pairType}] =
           |  _root_.com.trueaccord.scalapb.TypeMapper[${message.scalaTypeName}, ${message.mapType.pairType}]($messageToPair)($pairToMessage)"""
      )
  }

  def generateMessageCompanionForField(message: Descriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    val signature = "def messageCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = "
    // Due to https://issues.scala-lang.org/browse/SI-9111 we can't directly return the companion
    // object.
    if (message.fields.exists(_.isMessage))
      fp.add(signature + "{")
        .indent
        .add("require(__field.getContainingType() == javaDescriptor, \"FieldDescriptor does not match message type.\")")
        .add("var __out: _root_.com.trueaccord.scalapb.GeneratedMessageCompanion[_] = null")
        .add("__field.getNumber match {")
        .indent
        .print(message.fields.filter(_.isMessage)) {
          case (fp, f) =>
            fp.add(s"case ${f.getNumber} => __out = ${f.getMessageType.scalaTypeName}")
        }
        .outdent
        .add("}")
        .outdent
        .add("__out")
        .add("}")
    else fp.add(signature + "throw new MatchError(__field)")
  }

  def generateEnumCompanionForField(message: Descriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    val signature = "def enumCompanionForField(__field: _root_.com.google.protobuf.Descriptors.FieldDescriptor): _root_.com.trueaccord.scalapb.GeneratedEnumCompanion[_] = "
    if (message.fields.exists(_.isEnum))
      fp.add(signature + "{")
        .indent
        .add("require(__field.getContainingType() == javaDescriptor, \"FieldDescriptor does not match message type.\")")
        .add("__field.getNumber match {")
        .indent
        .print(message.fields.filter(_.isEnum)) {
          case (fp, f) =>
            fp.add(s"case ${f.getNumber} => ${f.getEnumType.scalaTypeName}")
        }
        .outdent
        .add("}")
        .outdent
        .add("}")
    else fp.add(signature + "throw new MatchError(__field)")
  }

  def printExtension(fp: FunctionalPrinter, fd: FieldDescriptor) = {
    val optionType = fd.getContainingType.getFullName match {
      case "google.protobuf.FileOptions" => true
      case "google.protobuf.MessageOptions" => true
      case "google.protobuf.FieldOptions" => true
      case "google.protobuf.EnumOptions" => true
      case "google.protobuf.EnumValueOptions" => true
      case "google.protobuf.ServiceOptions" => true
      case "google.protobuf.MethodOptions" => true
      case "google.protobuf.OneofOptions" => true
      case _ => false
    }
    if (!optionType) fp
    else fp
      .add(s"val ${fd.scalaName}: _root_.com.trueaccord.scalapb.GeneratedExtension[${fd.getContainingType.javaTypeName}, ${fd.scalaTypeName}] =")
      .when(params.javaConversions) {
        fp =>
          val hazzer = s"__valueIn.hasExtension(${fd.javaExtensionFieldFullName})"
          val getter = s"__valueIn.getExtension(${fd.javaExtensionFieldFullName})"
          fp.add(s"  _root_.com.trueaccord.scalapb.GeneratedExtension.forExtension({__valueIn => ${javaFieldToScala(hazzer, getter, fd)}})")
        }
      .when(!params.javaConversions) {
        fp =>
          val (container, baseExpr) = fd.getType match {
            case Type.DOUBLE => ("getFixed64List", FunctionApplication("java.lang.Double.longBitsToDouble"))
            case Type.FLOAT => ("getFixed32List", FunctionApplication("java.lang.Float.intBitsToFloat"))
            case Type.INT64 => ("getVarintList", Identity)
            case Type.UINT64 => ("getVarintList", Identity)
            case Type.INT32 => ("getVarintList", MethodApplication("toInt"))
            case Type.FIXED64 => ("getFixed64List", Identity)
            case Type.FIXED32 => ("getFixed32List", Identity)
            case Type.BOOL => ("getVarintList", OperatorApplication("!= 0"))
            case Type.STRING => ("getLengthDelimitedList", MethodApplication("toStringUtf8"))
            case Type.GROUP => throw new RuntimeException("Not supported")
            case Type.MESSAGE => ("getLengthDelimitedList", FunctionApplication(s"_root_.com.trueaccord.scalapb.GeneratedExtension.readMessageFromByteString(${fd.baseSingleScalaTypeName})"))
            case Type.BYTES => ("getLengthDelimitedList", Identity)
            case Type.UINT32 => ("getVarintList", MethodApplication("toInt"))
            case Type.ENUM => ("getVarintList", MethodApplication("toInt") andThen FunctionApplication(fd.baseSingleScalaTypeName + ".fromValue"))
            case Type.SFIXED32 => ("getFixed32List", Identity)
            case Type.SFIXED64 => ("getFixed64List", Identity)
            case Type.SINT32 => ("getVarintList", MethodApplication("toInt") andThen FunctionApplication("_root_.com.google.protobuf.CodedInputStream.decodeZigZag32"))
            case Type.SINT64 => ("getVarintList", FunctionApplication("_root_.com.google.protobuf.CodedInputStream.decodeZigZag64"))
          }
          val customExpr = baseExpr andThen toCustomTypeExpr(fd)

          val (factoryMethod, defaultNeeded) = {
            if (fd.supportsPresence) ("_root_.com.trueaccord.scalapb.GeneratedExtension.forOptionalUnknownField", false)
            else if (fd.isRepeated) ("_root_.com.trueaccord.scalapb.GeneratedExtension.forRepeatedUnknownField", false)
            else ("_root_.com.trueaccord.scalapb.GeneratedExtension.forSingularUnknownField", true)
          }
          val default = if (defaultNeeded) s", ${defaultValueForGet(fd)}"
          else ""
          fp.add(s"  $factoryMethod(${fd.getNumber}, _.$container)({__valueIn => ${customExpr("__valueIn", false)}}$default)")
      }
  }

  def generateMessageCompanion(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val className = message.nameSymbol
    val companionType = message.companionBaseClasses.mkString(" with ")
    printer.addStringMargin(
      s"""object $className extends $companionType {
         |  implicit def messageCompanion: $companionType = this""")
      .indent
      .when(message.javaConversions)(generateToJavaProto(message))
      .when(message.javaConversions)(generateFromJavaProto(message))
      .call(generateFromFieldsMap(message))
      .call(generateDescriptor(message))
      .call(generateMessageCompanionForField(message))
      .call(generateEnumCompanionForField(message))
      .call(generateDefaultInstance(message))
      .print(message.getEnumTypes.asScala)(printEnum)
      .print(message.getOneofs.asScala)(printOneof)
      .print(message.nestedTypes)(printMessage)
      .print(message.getExtensions.asScala)(printExtension)
      .call(generateMessageLens(message))
      .call(generateFieldNumbers(message))
      .when(!message.isMapEntry)(generateTypeMappers(message.fields ++ message.getExtensions.asScala))
      .when(message.isMapEntry)(generateTypeMappersForMapEntry(message))
      .outdent
      .add("}")
      .add("")
  }

  def generateScalaDoc(message: Descriptor): PrinterEndo = {
    fp =>
      val mainDoc: Seq[String] = message.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty)

      val fieldsDoc: Seq[String] = message.fields.filterNot(_.isInOneof).map {
        fd => (fd, fd.comment.map(_.split("\n").toSeq).getOrElse(Seq.empty))
      }.filter(_._2.nonEmpty).flatMap {
        case (fd, lines) =>
          Seq(s"@param ${fd.scalaName}") ++ lines.map("  " + _)
      }

      val sep = if (mainDoc.nonEmpty && fieldsDoc.nonEmpty) Seq("") else Seq.empty

      if (mainDoc.nonEmpty || fieldsDoc.nonEmpty) {
        val doc = (mainDoc ++ sep ++ fieldsDoc).zipWithIndex.map {
          case (line, index) =>
            val prefix = if (index == 0) "/**" else "  *"
            if (line.startsWith(" ") || line.isEmpty) (prefix + line) else (prefix + " " + line)
        } :+ "  */"
        fp.add(doc: _*)
      } else fp
  }

  def printMessage(printer: FunctionalPrinter, message: Descriptor) = {
    printer
      .call(generateScalaDoc(message))
      .add(s"@SerialVersionUID(0L)")
      .add(s"final case class ${message.nameSymbol}(")
      .indent
      .indent
      .call(printConstructorFieldList(message))
      .add(s") extends ${message.baseClasses.mkString(" with ")} {")
      .call(generateSerializedSizeForPackedFields(message))
      .call(generateSerializedSize(message))
      .call(generateWriteTo(message))
      .call(generateMergeFrom(message))
      .print(message.fields) {
        case (printer, field) =>
          val withMethod = "with" + field.upperScalaName
          val clearMethod = "clear" + field.upperScalaName
          val singleType = field.singleScalaTypeName
          printer
            .when(field.supportsPresence || field.isInOneof) {
              p =>
                val default = defaultValueForGet(field)
                p.add(s"def ${field.getMethod}: ${field.singleScalaTypeName} = ${fieldAccessorSymbol(field)}.getOrElse($default)")
            }
            .when(field.supportsPresence) {
              p =>
                p.addStringMargin(
                  s"""def $clearMethod: ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = None)
                     |def $withMethod(__v: ${singleType}): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = Some(__v))""")
            }.when(field.isInOneof) {
            p =>
              p.add(
                s"""def $withMethod(__v: ${singleType}): ${message.nameSymbol} = copy(${field.getContainingOneof.scalaName.asSymbol} = ${field.oneOfTypeName}(__v))""")
          }.when(field.isRepeated) { p =>
            val emptyValue = if (field.isMap) "scala.collection.immutable.Map.empty" else "scala.collection.Seq.empty"
            p.addStringMargin(
              s"""def $clearMethod = copy(${field.scalaName.asSymbol} = $emptyValue)
                 |def add${field.upperScalaName}(__vs: $singleType*): ${message.nameSymbol} = addAll${field.upperScalaName}(__vs)
                 |def addAll${field.upperScalaName}(__vs: TraversableOnce[$singleType]): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = ${field.scalaName.asSymbol} ++ __vs)""")
          }.when(field.isRepeated || field.isSingular) {
            _
              .add(s"def $withMethod(__v: ${field.scalaTypeName}): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = __v)")
          }
      }.print(message.getOneofs.asScala) {
      case (printer, oneof) =>
        printer.addStringMargin(
          s"""def clear${oneof.upperScalaName}: ${message.nameSymbol} = copy(${oneof.scalaName.asSymbol} = ${oneof.empty})
             |def with${oneof.upperScalaName}(__v: ${oneof.scalaTypeName}): ${message.nameSymbol} = copy(${oneof.scalaName.asSymbol} = __v)""")
    }
      .call(generateGetField(message))
      .when(!params.singleLineToString)(_.add(s"override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToUnicodeString(this)"))
      .when(params.singleLineToString)(_.add(s"override def toString: String = _root_.com.trueaccord.scalapb.TextFormat.printToSingleLineUnicodeString(this)"))
      .add(s"def companion = ${message.scalaTypeNameWithMaybeRoot(message)}")
      .outdent
      .outdent
      .addStringMargin(s"""}
                           |""")
      .call(generateMessageCompanion(message))
  }

  def scalaFileHeader(file: FileDescriptor): FunctionalPrinter = {
    if (file.scalaOptions.getPreambleList.asScala.nonEmpty && !file.scalaOptions.getSingleFile) {
      throw new GeneratorException(s"${file.getName}: single_file must be true when a preamble is provided.")
    }
    new FunctionalPrinter().addStringMargin(
      s"""// Generated by the Scala Plugin for the Protocol Buffer Compiler.
         |// Do not edit!
         |//
         |// Protofile syntax: ${file.getSyntax.toString}
         |
         |${if (file.scalaPackageName.nonEmpty) ("package " + file.scalaPackageName) else ""}
         |
         |${if (params.javaConversions) "import scala.collection.JavaConverters._" else ""}""")
    .print(file.scalaOptions.getImportList.asScala) {
      case (printer, i) => printer.add(s"import $i")
    }
    .add("")
    .seq(file.scalaOptions.getPreambleList.asScala)
    .when(file.scalaOptions.getPreambleList.asScala.nonEmpty)(_.add(""))
  }

  def generateFileDescriptor(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    // Encoding the file descriptor proto in base64. JVM has a limit on string literal to be up
    // to 64k, so we chunk it into a sequence and combining in run time.  The chunks are less
    // than 64k to account for indentation and new lines.
    val clearProto = file.toProto.toBuilder.clearSourceCodeInfo.build
    val base64: Seq[Seq[String]] = com.trueaccord.scalapb.Encoding.toBase64(clearProto.toByteArray)
      .grouped(55000).map {
      group =>
        val lines = ("\"\"\"" + group).grouped(100).toSeq
        lines.dropRight(1) :+ (lines.last + "\"\"\"")
    }.toSeq
    (if (params.javaConversions)
      fp.add("lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor =")
        .add(s"  ${file.javaFullOuterClassName}.getDescriptor()")
      else
      fp.add("lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {")
        .add("  val proto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(")
        .add("    com.trueaccord.scalapb.Encoding.fromBase64(scala.collection.Seq(")
        .addGroupsWithDelimiter(",")(base64)
        .add("    ).mkString))")
        .add("  com.google.protobuf.Descriptors.FileDescriptor.buildFrom(proto, Array(")
        .addWithDelimiter(",")(file.getDependencies.asScala.map {
        d =>
          if (d.getPackage == "scalapb") "com.trueaccord.scalapb.Scalapb.getDescriptor()"
          else if (d.getPackage == "google.protobuf" && d.javaOuterClassName == "DescriptorProtos") "com.google.protobuf.DescriptorProtos.getDescriptor()"
          else d.fileDescriptorObjectFullName + ".javaDescriptor"
      })
        .add("  ))")
        .add("}"))
      .add("""@deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")""")
      .add("def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor")
  }

  private def encodeByteArray(a: GoogleByteString): Seq[String] = {
    val CH_SLASH: java.lang.Byte = '\\'.toByte
    val CH_SQ: java.lang.Byte = '\''.toByte
    val CH_DQ: java.lang.Byte = '\"'.toByte
    for {
      groups <- a.asScala.grouped(60).toSeq
    } yield {
      val sb = scala.collection.mutable.StringBuilder.newBuilder
      sb.append('\"')
      groups.foreach {
        b =>
          b match {
            case CH_SLASH => sb.append("\\\\")
            case CH_SQ => sb.append("\\\'")
            case CH_DQ => sb.append("\\\"")
            case b if b >= 0x20 => sb.append(b)
            case b =>
              sb.append("\\u00")
              sb.append(Integer.toHexString((b >>> 4) & 0xf))
              sb.append(Integer.toHexString(b & 0xf))
          }
      }
      sb.append('\"')
      sb.result()
    }
  }

  def generateServiceFiles(file: FileDescriptor): Seq[CodeGeneratorResponse.File] = {
    if(params.grpc) {
      file.getServices.asScala.map { service =>
        val p = new GrpcServicePrinter(service, params)
        val code = p.printService(FunctionalPrinter()).result()
        val b = CodeGeneratorResponse.File.newBuilder()
        b.setName(file.scalaDirectory + "/" + service.objectName + ".scala")
        b.setContent(code)
        b.build
      }
    } else Nil
  }

  def createFileDescriptorCompanionObject(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    fp.add(s"object ${file.fileDescriptorObjectName} {")
      .indent
      .call(generateFileDescriptor(file))
      .print(file.getExtensions.asScala)(printExtension)
      .call(generateTypeMappers(file.getExtensions.asScala))
      .outdent
      .add("}")
  }

  def generateSingleScalaFileForFileDescriptor(file: FileDescriptor): Seq[CodeGeneratorResponse.File] = {
    val code =
      scalaFileHeader(file)
      .print(file.getEnumTypes.asScala)(printEnum)
      .print(file.getMessageTypes.asScala)(printMessage)
      .call(createFileDescriptorCompanionObject(file)).result()
    val b = CodeGeneratorResponse.File.newBuilder()
    b.setName(file.scalaDirectory + "/" + file.fileDescriptorObjectName + ".scala")
    b.setContent(code)
    generateServiceFiles(file) :+ b.build
  }

  def generateMultipleScalaFilesForFileDescriptor(file: FileDescriptor): Seq[CodeGeneratorResponse.File] = {
    val serviceFiles = generateServiceFiles(file)

    val enumFiles = for {
      enum <- file.getEnumTypes.asScala
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaDirectory + "/" + enum.getName + ".scala")
      b.setContent(
        scalaFileHeader(file)
          .call(printEnum(_, enum)).result())
      b.build
    }

    val messageFiles = for {
      message <- file.getMessageTypes.asScala
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaDirectory + "/" + message.scalaName + ".scala")
      b.setContent(
        scalaFileHeader(file)
          .call(printMessage(_, message)).result())
      b.build
    }

    val fileDescriptorObjectFile = {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaDirectory + s"/${file.fileDescriptorObjectName}.scala")
      b.setContent(
        scalaFileHeader(file)
          .call(createFileDescriptorCompanionObject(file)).result())
      b.build
    }

    serviceFiles ++ enumFiles ++ messageFiles :+ fileDescriptorObjectFile
  }
}

object ProtobufGenerator {
  def parseParameters(params: String): Either[String, GeneratorParams] = {
    params.split(",").map(_.trim).filter(_.nonEmpty).foldLeft[Either[String, GeneratorParams]](Right(GeneratorParams())) {
      case (Right(params), "java_conversions") => Right(params.copy(javaConversions = true))
      case (Right(params), "flat_package") => Right(params.copy(flatPackage = true))
      case (Right(params), "grpc") => Right(params.copy(grpc = true))
      case (Right(params), "single_line_to_string") => Right(params.copy(singleLineToString = true))
      case (Right(params), p) => Left(s"Unrecognized parameter: '$p'")
      case (x, _) => x
    }
  }

  def handleCodeGeneratorRequest(request: CodeGeneratorRequest): CodeGeneratorResponse = {
    val b = CodeGeneratorResponse.newBuilder
    parseParameters(request.getParameter) match {
      case Right(params) =>
        try {
          val generator = new ProtobufGenerator(params)
          import generator.FileDescriptorPimp
          val filesByName: Map[String, FileDescriptor] =
            request.getProtoFileList.asScala.foldLeft[Map[String, FileDescriptor]](Map.empty) {
              case (acc, fp) =>
                val deps = fp.getDependencyList.asScala.map(acc)
                acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
            }
          val validator = new ProtoValidation(params)
          filesByName.values.foreach(validator.validateFile)
          request.getFileToGenerateList.asScala.foreach {
            name =>
              val file = filesByName(name)
              val responseFiles =
                if (file.scalaOptions.getSingleFile)
                  generator.generateSingleScalaFileForFileDescriptor(file)
                else generator.generateMultipleScalaFilesForFileDescriptor(file)
              b.addAllFile(responseFiles.asJava)
          }
        } catch {
          case e: GeneratorException =>
            b.setError(e.message)
        }
      case Left(error) =>
        b.setError(error)
    }
    b.build
  }
}
