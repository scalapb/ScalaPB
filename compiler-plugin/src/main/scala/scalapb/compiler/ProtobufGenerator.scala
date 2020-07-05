package scalapb.compiler

import com.google.protobuf.Descriptors._
import com.google.protobuf.{CodedOutputStream, DescriptorProtos, ByteString => GoogleByteString}
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import scalapb.compiler.FunctionalPrinter.PrinterEndo
import scalapb.options.compiler.Scalapb
import scalapb.options.compiler.Scalapb.FieldOptions
import scala.jdk.CollectionConverters._
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocgen.CodeGenRequest
import protocgen.CodeGenResponse

// Exceptions that are caught and passed upstreams as errors.
case class GeneratorException(message: String) extends Exception(message)

class ProtobufGenerator(
    params: GeneratorParams,
    implicits: DescriptorImplicits
) {
  import implicits._
  import DescriptorImplicits.AsSymbolPimp
  import ProtobufGenerator._

  def printEnum(printer: FunctionalPrinter, e: EnumDescriptor): FunctionalPrinter = {
    val name = e.scalaType.nameSymbol
    printer
      .when(e.getOptions.getDeprecated) {
        _.add(ProtobufGenerator.deprecatedAnnotation)
      }
      .call(generateScalaDoc(e))
      .add(
        s"sealed abstract class $name(val value: _root_.scala.Int) extends ${e.baseTraitExtends.mkString(" with ")} {"
      )
      .indent
      .add(s"type EnumType = $name")
      .print(e.getValues.asScala) {
        case (p, v) => p.add(s"def ${v.isName}: _root_.scala.Boolean = false")
      }
      .add(s"def companion: _root_.scalapb.GeneratedEnumCompanion[$name] = ${e.scalaType.fullName}")
      .add(
        s"final def asRecognized: _root_.scala.Option[${e.recognizedEnum.fullName}] = if (isUnrecognized) _root_.scala.None else _root_.scala.Some(this.asInstanceOf[${e.recognizedEnum.fullName}])"
      )
      .outdent
      .add("}")
      .add("")
      .when(e.getOptions.getDeprecated) {
        _.add(ProtobufGenerator.deprecatedAnnotation)
      }
      .add(s"object $name extends ${e.companionExtends.mkString(" with ")} {")
      .indent
      .add(s"sealed trait ${e.recognizedEnum.nameSymbol} extends $name")
      .add(s"implicit def enumCompanion: _root_.scalapb.GeneratedEnumCompanion[$name] = this")
      .print(e.getValues.asScala) {
        case (p, v) =>
          p.call(generateScalaDoc(v))
            .add(s"""@SerialVersionUID(0L)${if (v.getOptions.getDeprecated) {
                      " " + ProtobufGenerator.deprecatedAnnotation
                    } else ""}
                    |case object ${v.scalaName.asSymbol} extends ${v.valueExtends
                      .mkString(" with ")} {
                    |  val index = ${v.getIndex}
                    |  val name = "${v.getName}"
                    |  override def ${v.isName}: _root_.scala.Boolean = true
                    |}
                    |""".stripMargin)
      }
      .add(s"""@SerialVersionUID(0L)
              |final case class Unrecognized(unrecognizedValue: _root_.scala.Int) extends $name(unrecognizedValue) with _root_.scalapb.UnrecognizedEnum
              |
              |lazy val values = scala.collection.immutable.Seq(${e.getValues.asScala
                .map(_.scalaName.asSymbol)
                .mkString(", ")})
              |def fromValue(__value: _root_.scala.Int): $name = __value match {""".stripMargin)
      .print(e.valuesWithNoDuplicates) {
        case (p, v) => p.add(s"  case ${v.getNumber} => ${v.scalaName.asSymbol}")
      }
      .add(
        s"""  case __other => Unrecognized(__other)
           |}
           |def javaDescriptor: _root_.com.google.protobuf.Descriptors.EnumDescriptor = ${e.javaDescriptorSource}
           |def scalaDescriptor: _root_.scalapb.descriptors.EnumDescriptor = ${e.scalaDescriptorSource}""".stripMargin
      )
      .when(e.javaConversions) {
        _.add(
          s"""|def fromJavaValue(pbJavaSource: ${e.javaTypeName}): $name = fromValue(pbJavaSource.getNumber)
              |def toJavaValue(pbScalaSource: $name): ${e.javaTypeName} = {
              |  _root_.scala.Predef.require(!pbScalaSource.isUnrecognized, "Unrecognized enum values can not be converted to Java")
              |  ${e.javaTypeName}.forNumber(pbScalaSource.value)
              |}""".stripMargin
        )
      }
      .outdent
      .add("}")
  }

  def printOneof(printer: FunctionalPrinter, e: OneofDescriptor): FunctionalPrinter = {
    printer
      .add(s"sealed trait ${e.scalaType.nameSymbol} extends ${e.baseClasses.mkString(" with ")} {")
      .indent
      .add(s"def isEmpty: _root_.scala.Boolean = false")
      .add(s"def isDefined: _root_.scala.Boolean = true")
      .print(e.fields) {
        case (p, v) =>
          p.add(s"def is${v.upperScalaName}: _root_.scala.Boolean = false")
      }
      .print(e.fields) {
        case (p, v) =>
          p.add(s"def ${v.scalaName.asSymbol}: _root_.scala.Option[${v.scalaTypeName}] = ${C.None}")
      }
      .outdent
      .add(s"""}
              |object ${e.scalaType.nameSymbol} {
              |  @SerialVersionUID(0L)
              |  case object Empty extends ${e.scalaType.fullName} {
              |    type ValueType = _root_.scala.Nothing
              |    override def isEmpty: _root_.scala.Boolean = true
              |    override def isDefined: _root_.scala.Boolean = false
              |    override def number: _root_.scala.Int = 0
              |    override def value: _root_.scala.Nothing = throw new java.util.NoSuchElementException("Empty.value")
              |  }
              |""".stripMargin)
      .indent
      .print(e.fields) {
        case (p, v) =>
          p.add(s"""@SerialVersionUID(0L)${if (v.getOptions.getDeprecated) {
                     " " + ProtobufGenerator.deprecatedAnnotation
                   } else ""}
                   |final case class ${v.upperScalaName}(value: ${v.scalaTypeName}) extends ${e.scalaType.fullName} {
                   |  type ValueType = ${v.scalaTypeName}
                   |  override def is${v.upperScalaName}: _root_.scala.Boolean = true
                   |  override def ${v.scalaName.asSymbol}: _root_.scala.Option[${v.scalaTypeName}] = Some(value)
                   |  override def number: _root_.scala.Int = ${v.getNumber}
                   |}""".stripMargin)
      }
      .outdent
      .add("}")
  }

  def defaultValueForGet(field: FieldDescriptor, uncustomized: Boolean = false) = {
    // Needs to be 'def' and not val since for some of the cases it's invalid to call it.
    def defaultValue = field.getDefaultValue
    val baseDefaultValue: String = field.getJavaType match {
      case FieldDescriptor.JavaType.INT  => defaultValue.toString
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
      case FieldDescriptor.JavaType.BOOLEAN =>
        Boolean.unbox(defaultValue.asInstanceOf[java.lang.Boolean]).toString
      case FieldDescriptor.JavaType.BYTE_STRING =>
        val d = defaultValue.asInstanceOf[GoogleByteString]
        if (d.isEmpty)
          "_root_.com.google.protobuf.ByteString.EMPTY"
        else
          d.asScala
            .map(_.toString)
            .mkString("_root_.com.google.protobuf.ByteString.copyFrom(Array[Byte](", ", ", "))")
      case FieldDescriptor.JavaType.STRING => escapeScalaString(defaultValue.asInstanceOf[String])
      case FieldDescriptor.JavaType.MESSAGE =>
        field.getMessageType.scalaType
          .fullNameWithMaybeRoot(field.getContainingType) + ".defaultInstance"
      case FieldDescriptor.JavaType.ENUM =>
        field.getEnumType.scalaType
          .fullNameWithMaybeRoot(field.getContainingType) + "." + defaultValue
          .asInstanceOf[EnumValueDescriptor]
          .scalaName
          .asSymbol
    }
    if (!uncustomized && field.customSingleScalaTypeName.isDefined)
      s"${field.typeMapper.fullName}.toCustom($baseDefaultValue)"
    else baseDefaultValue
  }

  def defaultValueForDefaultInstance(field: FieldDescriptor) =
    if (field.supportsPresence) C.None
    else if (field.isRepeated) field.emptyCollection
    else defaultValueForGet(field)

  def javaToScalaConversion(field: FieldDescriptor) = {
    val baseValueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT         => MethodApplication("intValue")
      case FieldDescriptor.JavaType.LONG        => MethodApplication("longValue")
      case FieldDescriptor.JavaType.FLOAT       => MethodApplication("floatValue")
      case FieldDescriptor.JavaType.DOUBLE      => MethodApplication("doubleValue")
      case FieldDescriptor.JavaType.BOOLEAN     => MethodApplication("booleanValue")
      case FieldDescriptor.JavaType.BYTE_STRING => Identity
      case FieldDescriptor.JavaType.STRING      => Identity
      case FieldDescriptor.JavaType.MESSAGE =>
        FunctionApplication(field.getMessageType.scalaType.fullName + ".fromJavaProto")
      case FieldDescriptor.JavaType.ENUM =>
        if (field.getFile.isProto3)
          MethodApplication("intValue") andThen FunctionApplication(
            field.getEnumType.scalaType.fullName + ".fromValue"
          )
        else FunctionApplication(field.getEnumType.scalaType.fullName + ".fromJavaValue")
    }
    baseValueConversion andThen toCustomTypeExpr(field)
  }

  def javaFieldToScala(javaHazzer: String, javaGetter: String, field: FieldDescriptor): String = {
    val valueConversion: Expression = javaToScalaConversion(field)

    if (field.supportsPresence)
      s"if ($javaHazzer) Some(${valueConversion.apply(javaGetter, enclosingType = EnclosingType.None)}) else ${C.None}"
    else if (field.isRepeated)
      valueConversion(
        javaGetter + ".asScala",
        EnclosingType.Collection(field.collectionType),
        mustCopy = true
      )
    else valueConversion(javaGetter, EnclosingType.None)
  }

  def javaFieldToScala(container: String, field: FieldDescriptor): String = {
    val javaHazzer = container + ".has" + field.upperJavaName
    val upperJavaName =
      if (field.isEnum && field.getFile.isProto3) (field.upperJavaName + "Value")
      else field.upperJavaName
    val javaGetter =
      if (field.isRepeated)
        container + ".get" + upperJavaName + "List"
      else
        container + ".get" + upperJavaName

    javaFieldToScala(javaHazzer, javaGetter, field)
  }

  def javaMapFieldToScala(container: String, field: FieldDescriptor): String = {
    // TODO(thesamet): if both unit conversions are NoOp, we can omit the map call.
    def unitConversion(n: String, field: FieldDescriptor) =
      javaToScalaConversion(field).apply(n, EnclosingType.None)
    val upperJavaName =
      if (field.mapType.valueField.isEnum && field.getFile.isProto3)
        (field.upperJavaName + "Value")
      else field.upperJavaName
    ExpressionBuilder.convertCollection(
      s"${container}.get${upperJavaName}Map.asScala.iterator.map(__pv => (${unitConversion(
        "__pv._1",
        field.mapType.keyField
      )}, ${unitConversion("__pv._2", field.mapType.valueField)}))",
      field.enclosingType
    )
  }

  def scalaToJava(field: FieldDescriptor, boxPrimitives: Boolean): Expression = {
    def maybeBox(name: String) = if (boxPrimitives) FunctionApplication(name) else Identity

    field.getJavaType match {
      case FieldDescriptor.JavaType.INT         => maybeBox("_root_.scala.Int.box")
      case FieldDescriptor.JavaType.LONG        => maybeBox("_root_.scala.Long.box")
      case FieldDescriptor.JavaType.FLOAT       => maybeBox("_root_.scala.Float.box")
      case FieldDescriptor.JavaType.DOUBLE      => maybeBox("_root_.scala.Double.box")
      case FieldDescriptor.JavaType.BOOLEAN     => maybeBox("_root_.scala.Boolean.box")
      case FieldDescriptor.JavaType.BYTE_STRING => Identity
      case FieldDescriptor.JavaType.STRING      => Identity
      case FieldDescriptor.JavaType.MESSAGE =>
        FunctionApplication(field.getMessageType.scalaType.fullName + ".toJavaProto")
      case FieldDescriptor.JavaType.ENUM =>
        if (field.getFile.isProto3)
          (MethodApplication("value") andThen maybeBox("_root_.scala.Int.box"))
        else
          FunctionApplication(field.getEnumType.scalaType.fullName + ".toJavaValue")
    }
  }

  def assignScalaMapToJava(
      scalaObject: String,
      javaObject: String,
      field: FieldDescriptor
  ): String = {
    def valueConvert(v: String, field: FieldDescriptor) =
      (toBaseTypeExpr(field) andThen scalaToJava(field, boxPrimitives = true))
        .apply(v, EnclosingType.None)

    val putAll =
      s"putAll${field.upperScalaName}" + (if (field.mapType.valueField.isEnum && field.getFile.isProto3)
                                            "Value"
                                          else "")

    s"""$javaObject
       |  .$putAll($scalaObject.${fieldAccessorSymbol(field)}.iterator.map {
       |    __kv => (${valueConvert("__kv._1", field.mapType.keyField)}, ${valueConvert(
         "__kv._2",
         field.mapType.valueField
       )})
       |  }.toMap.asJava)""".stripMargin
  }

  def assignScalaFieldToJava(
      scalaObject: String,
      javaObject: String,
      field: FieldDescriptor
  ): String =
    if (field.isMapField) assignScalaMapToJava(scalaObject, javaObject, field)
    else {
      val javaSetter = javaObject +
        (if (field.isRepeated) ".addAll"
         else
           ".set") + field.upperJavaName + (if (field.isEnum && field.getFile.isProto3) "Value"
                                            else "")
      val scalaGetter = scalaObject + "." + fieldAccessorSymbol(field)

      val scalaExpr =
        (toBaseTypeExpr(field) andThen scalaToJava(field, boxPrimitives = field.isRepeated))
          .apply(
            scalaGetter,
            enclosingType = field.enclosingType match {
              case EnclosingType.Collection(_) =>
                EnclosingType.Collection(DescriptorImplicits.ScalaIterable)
              case o => o
            }
          )
      if (field.supportsPresence || field.isInOneof)
        s"$scalaExpr.foreach($javaSetter)"
      else if (field.isRepeated)
        s"$javaSetter($scalaExpr.asJava)"
      else
        s"$javaSetter($scalaExpr)"
    }

  def generateGetField(message: Descriptor)(fp: FunctionalPrinter) = {
    val signature = "def getFieldByNumber(__fieldNumber: _root_.scala.Int): _root_.scala.Any = "
    if (message.fields.nonEmpty)
      fp.add(signature + "{")
        .indent
        .add("(__fieldNumber: @_root_.scala.unchecked) match {")
        .indent
        .print(message.fields) {
          case (fp, f) =>
            val e = toBaseFieldType(f)
              .apply(fieldAccessorSymbol(f), enclosingType = f.fieldMapEnclosingType)
            if (f.supportsPresence || f.isInOneof)
              fp.add(s"case ${f.getNumber} => $e.orNull")
            else if (f.isOptional) {
              // In proto3, drop default value
              fp.add(s"case ${f.getNumber} => {")
                .indent
                .add(s"val __t = $e")
                .add({
                  val cond =
                    if (!f.isEnum)
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
    else fp.add(signature + "throw new MatchError(__fieldNumber)")
  }

  def singleFieldAsPvalue(fd: FieldDescriptor): LiteralExpression = {
    val d = "_root_.scalapb.descriptors"
    fd.getJavaType match {
      case FieldDescriptor.JavaType.INT         => FunctionApplication(s"$d.PInt")
      case FieldDescriptor.JavaType.LONG        => FunctionApplication(s"$d.PLong")
      case FieldDescriptor.JavaType.FLOAT       => FunctionApplication(s"$d.PFloat")
      case FieldDescriptor.JavaType.DOUBLE      => FunctionApplication(s"$d.PDouble")
      case FieldDescriptor.JavaType.BOOLEAN     => FunctionApplication(s"$d.PBoolean")
      case FieldDescriptor.JavaType.BYTE_STRING => FunctionApplication(s"$d.PByteString")
      case FieldDescriptor.JavaType.STRING      => FunctionApplication(s"$d.PString")
      case FieldDescriptor.JavaType.ENUM        => FunctionApplication(s"$d.PEnum")
      case FieldDescriptor.JavaType.MESSAGE     => MethodApplication("toPMessage")
    }
  }

  def generateGetFieldPValue(message: Descriptor)(fp: FunctionalPrinter) = {
    val signature =
      "def getField(__field: _root_.scalapb.descriptors.FieldDescriptor): _root_.scalapb.descriptors.PValue = "
    if (message.fields.nonEmpty)
      fp.add(signature + "{")
        .indent
        .add("_root_.scala.Predef.require(__field.containingMessage eq companion.scalaDescriptor)")
        .add("(__field.number: @_root_.scala.unchecked) match {")
        .indent
        .print(message.fields) {
          case (fp, f) =>
            val e = toBaseFieldTypeWithScalaDescriptors(f)
              .andThen(singleFieldAsPvalue(f))
              .apply(
                fieldAccessorSymbol(f),
                enclosingType = f.enclosingType match {
                  case EnclosingType.Collection(_) =>
                    EnclosingType.Collection(DescriptorImplicits.ScalaVector)
                  case other => other
                }
              )
            if (f.supportsPresence || f.isInOneof) {
              fp.add(s"case ${f.getNumber} => $e.getOrElse(_root_.scalapb.descriptors.PEmpty)")
            } else if (f.isRepeated) {
              fp.add(s"case ${f.getNumber} => _root_.scalapb.descriptors.PRepeated($e)")
            } else {
              fp.add(s"case ${f.getNumber} => $e")
            }
        }
        .outdent
        .add("}")
        .outdent
        .add("}")
    else fp.add(signature + "throw new MatchError(__field)")
  }

  def generateWriteSingleValue(field: FieldDescriptor, valueExpr: String)(
      fp: FunctionalPrinter
  ): FunctionalPrinter = {
    if (field.isMessage) {
      fp.add(s"""_output__.writeTag(${field.getNumber}, 2)
                |_output__.writeUInt32NoTag($valueExpr.serializedSize)
                |$valueExpr.writeTo(_output__)""".stripMargin)
    } else {
      val capTypeName = Types.capitalizedType(field.getType)
      fp.add(s"_output__.write$capTypeName(${field.getNumber}, $valueExpr)")
    }
  }

  def sizeExpressionForSingleField(field: FieldDescriptor, expr: String): String =
    if (field.isMessage) {
      val size = s"$expr.serializedSize"
      CodedOutputStream
        .computeTagSize(field.getNumber)
        .toString + s" + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag($size) + $size"
    } else {
      val capTypeName = Types.capitalizedType(field.getType)
      s"_root_.com.google.protobuf.CodedOutputStream.compute${capTypeName}Size(${field.getNumber}, ${expr})"
    }

  def fieldAccessorSymbol(field: FieldDescriptor) =
    if (field.isInOneof)
      (field.getContainingOneof.scalaName.nameSymbol + "." + field.scalaName.asSymbol)
    else
      field.scalaName.asSymbol

  def toBaseTypeExpr(field: FieldDescriptor) =
    if (field.customSingleScalaTypeName.isDefined)
      FunctionApplication(field.typeMapper.fullName + ".toBase")
    else Identity

  def toBaseFieldType(field: FieldDescriptor) =
    if (field.isEnum)
      (toBaseTypeExpr(field) andThen MethodApplication("javaValueDescriptor"))
    else toBaseTypeExpr(field)

  def toBaseFieldTypeWithScalaDescriptors(field: FieldDescriptor) =
    if (field.isEnum)
      (toBaseTypeExpr(field) andThen MethodApplication("scalaValueDescriptor"))
    else toBaseTypeExpr(field)

  def toBaseType(field: FieldDescriptor)(expr: String) = {
    val suffix = if (field.isEnum) ".value" else ""
    toBaseTypeExpr(field).apply(expr, EnclosingType.None) + suffix
  }

  def toCustomTypeExpr(field: FieldDescriptor) =
    if (field.customSingleScalaTypeName.isEmpty) Identity
    else FunctionApplication(s"${field.typeMapper.fullName}.toCustom")

  def toCustomType(field: FieldDescriptor)(expr: String) =
    toCustomTypeExpr(field).apply(expr, EnclosingType.None)

  def generateSerializedSizeForField(
      fp: FunctionalPrinter,
      field: FieldDescriptor
  ): FunctionalPrinter = {
    val fieldNameSymbol = fieldAccessorSymbol(field)

    if (field.isRequired) {
      fp.add(s"""
                |{
                |  val __value = ${toBaseType(field)(fieldNameSymbol)}
                |  __size += ${sizeExpressionForSingleField(field, "__value")}
                |};""".stripMargin)
    } else if (field.isSingular) {
      fp.add(
        s"""
           |{
           |  val __value = ${toBaseType(field)(fieldNameSymbol)}
           |  if (${isNonEmpty("__value", field)}) {
           |    __size += ${sizeExpressionForSingleField(field, "__value")}
           |  }
           |};""".stripMargin
      )
    } else if (field.isOptional) {
      fp.add(
        s"""if ($fieldNameSymbol.isDefined) {
           |  val __value = ${toBaseType(field)(fieldNameSymbol + ".get")}
           |  __size += ${sizeExpressionForSingleField(field, "__value")}
           |};""".stripMargin
      )
    } else if (field.isRepeated) {
      val tagSize = CodedOutputStream.computeTagSize(field.getNumber)
      if (!field.isPacked) {
        Types.fixedSize(field.getType) match {
          case Some(size) => fp.add(s"__size += ${size + tagSize} * $fieldNameSymbol.size")
          case None =>
            fp.add(s"""$fieldNameSymbol.foreach { __item =>
                      |  val __value = ${toBaseType(field)("__item")}
                      |  __size += ${sizeExpressionForSingleField(field, "__value")}
                      |}""".stripMargin)
        }
      } else {
        val fieldName = field.scalaName
        fp.add(s"""if($fieldNameSymbol.nonEmpty) {
                  |  val __localsize = ${fieldName}SerializedSize
                  |  __size += $tagSize + _root_.com.google.protobuf.CodedOutputStream.computeUInt32SizeNoTag(__localsize) + __localsize
                  |}""".stripMargin)
      }
    } else throw new RuntimeException("Should not reach here.")
  }

  def generateSerializedSize(message: Descriptor)(fp: FunctionalPrinter) = {
    if (message.fields.nonEmpty || message.preservesUnknownFields) {
      fp.when(!message.isValueClass) {
          _.add(
            """@transient
              |private[this] var __serializedSizeCachedValue: _root_.scala.Int = 0""".stripMargin
          )
        }
        .add("private[this] def __computeSerializedValue(): _root_.scala.Int = {")
        .indent
        .add("var __size = 0")
        .print(message.fields)(generateSerializedSizeForField)
        .when(message.preservesUnknownFields)(_.add("__size += unknownFields.serializedSize"))
        .add("__size")
        .outdent
        .add("}")
        .add("override def serializedSize: _root_.scala.Int = {")
        .indent
        .when(message.isValueClass) {
          _.add("__computeSerializedValue()")
        }
        .when(!message.isValueClass) {
          _.add("""var read = __serializedSizeCachedValue
                  |if (read == 0) {
                  |  read = __computeSerializedValue()
                  |  __serializedSizeCachedValue = read
                  |}
                  |read""".stripMargin)
        }
        .outdent
        .add("}")
    } else {
      fp.add("final override def serializedSize: _root_.scala.Int = 0")
    }
  }

  def generateSerializedSizeForPackedFields(message: Descriptor)(fp: FunctionalPrinter) =
    fp.print(message.fields.filter(_.isPacked)) {
      case (printer, field) =>
        val methodName = s"${field.scalaName}SerializedSize"
        printer
          .add(s"private[this] def $methodName = {") //closing brace is in each case
          .call({ fp =>
            Types.fixedSize(field.getType) match {
              case Some(size) =>
                fp.add(s"  $size * ${field.scalaName.asSymbol}.size").add("}")
              case None =>
                val capTypeName = Types.capitalizedType(field.getType)
                val sizeFunc = FunctionApplication(
                  s"_root_.com.google.protobuf.CodedOutputStream.compute${capTypeName}SizeNoTag"
                )
                val fromEnum = if (field.isEnum) MethodApplication("value") else Identity
                val fromCustom =
                  if (field.customSingleScalaTypeName.isDefined)
                    FunctionApplication(s"${field.typeMapper.fullName}.toBase")
                  else Identity
                val funcs    = List(sizeFunc, fromEnum, fromCustom)
                val sizeExpr = ExpressionBuilder.runSingleton(funcs)("__i")
                fp.indent
                  .add(s"if (__${methodName}Field == 0) __${methodName}Field = {")
                  .add(s"  var __s: _root_.scala.Int = 0")
                  .add(s"  ${field.scalaName.asSymbol}.foreach(__i => __s += $sizeExpr)")
                  .add(s"  __s")
                  .add(s"}")
                  .add(s"__${methodName}Field")
                  .outdent
                  .add("}") // closing brace for the method
                  .add(s"@transient private[this] var __${methodName}Field: _root_.scala.Int = 0")
            }
          })
    }

  private def composeGen(funcs: Seq[String]) =
    if (funcs.length == 1) funcs(0)
    else s"(${funcs(0)} _)" + funcs.tail.map(func => s".compose($func)").mkString

  private def isNonEmpty(expr: String, field: FieldDescriptor): String = {
    if (field.getType == Type.BYTES | field.getType == Type.STRING) s"!${expr}.isEmpty"
    else if (field.getType == Type.ENUM) s"${expr} != 0"
    else s"${expr} != ${defaultValueForGet(field, uncustomized = true)}"
  }

  def generateWriteTo(message: Descriptor)(fp: FunctionalPrinter) =
    fp.add(
        s"def writeTo(`_output__`: _root_.com.google.protobuf.CodedOutputStream): _root_.scala.Unit = {"
      )
      .indent
      .print(message.fields.sortBy(_.getNumber)) {
        case (printer, field) =>
          val fieldNameSymbol = fieldAccessorSymbol(field)
          val capTypeName     = Types.capitalizedType(field.getType)
          if (field.isPacked) {
            val writeFunc = composeGen(
              Seq(s"_output__.write${capTypeName}NoTag") ++ (
                if (field.isEnum) Seq(s"(_: ${field.baseSingleScalaTypeName}).value") else Nil
              ) ++ (
                if (field.customSingleScalaTypeName.isDefined)
                  Seq(s"${field.typeMapper.fullName}.toBase")
                else Nil
              )
            )

            printer.add(s"""if (${fieldNameSymbol}.nonEmpty) {
                           |  _output__.writeTag(${field.getNumber}, 2)
                           |  _output__.writeUInt32NoTag(${field.scalaName}SerializedSize)
                           |  ${fieldNameSymbol}.foreach($writeFunc)
                           |};""".stripMargin)
          } else if (field.isRequired) {
            printer
              .add("")
              .add("{")
              .indent
              .add(s"val __v = ${toBaseType(field)(fieldNameSymbol)}")
              .call(generateWriteSingleValue(field, "__v"))
              .outdent
              .add("};")
          } else if (field.isSingular) {
            // Singular that are not required are written only if they don't equal their default
            // value.
            printer
              .add(s"{")
              .indent
              .add(s"val __v = ${toBaseType(field)(fieldNameSymbol)}")
              .add(s"if (${isNonEmpty("__v", field)}) {")
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
              .add(s"val __m = ${toBaseType(field)("__v")}")
              .call(generateWriteSingleValue(field, "__m"))
              .outdent
              .add("};")
          }
      }
      .when(message.preservesUnknownFields)(_.add("unknownFields.writeTo(_output__)"))
      .outdent
      .add("}")

  def constructorFields(message: Descriptor): Seq[ConstructorField] = {
    def annotations(field: FieldDescriptor) =
      if (field.annotationList.nonEmpty) field.annotationList else Nil

    val regularFields = message.fields.collect {
      case field if !field.isInOneof =>
        val typeName = field.scalaTypeName
        val ctorDefaultValue: Option[String] =
          if (message.getFile.noDefaultValuesInConstructor) None
          else if (field.isOptional && field.supportsPresence) Some(C.None)
          else if (field.isSingular && !field.isRequired) Some(defaultValueForGet(field).toString)
          else if (field.isRepeated) Some(s"${field.emptyCollection}")
          else None

        ConstructorField(
          name = field.scalaName.asSymbol,
          typeName = typeName,
          default = ctorDefaultValue,
          index = field.getIndex,
          annotations = annotations(field)
        )
    }
    val oneOfFields = message.getRealOneofs.asScala.map { oneOf =>
      val ctorDefaultValue: Option[String] =
        if (message.getFile.noDefaultValuesInConstructor) None
        else Some(oneOf.empty.fullNameWithMaybeRoot(message))

      ConstructorField(
        name = oneOf.scalaName.nameSymbol,
        typeName = oneOf.scalaType.fullNameWithMaybeRoot(message),
        default = ctorDefaultValue,
        index = oneOf.getField(0).getIndex
      )
    }
    val maybeUnknownFields =
      if (message.preservesUnknownFields)
        Seq(ConstructorField.UnknownFields)
      else Seq()

    (regularFields ++ oneOfFields ++ maybeUnknownFields).sortBy(_.index)
  }

  def printConstructorFieldList(
      message: Descriptor
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.addWithDelimiter(",")(constructorFields(message).map(_.fullString))
  }

  def generateMerge(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaType.fullNameWithMaybeRoot(message)
    val requiredFieldMap: Map[FieldDescriptor, Int] =
      message.fields.filter(_.isRequired).zipWithIndex.toMap
    printer
      .add(
        s"def merge(`_message__`: $myFullScalaName, `_input__`: _root_.com.google.protobuf.CodedInputStream): $myFullScalaName = {"
      )
      .indent
      .print(message.fieldsWithoutOneofs)((printer, field) =>
        if (!field.isRepeated)
          printer.add(s"var __${field.scalaName} = `_message__`.${field.scalaName.asSymbol}")
        else
          printer.add(
            s"val __${field.scalaName} = (${field.collectionBuilder} ++= " +
              s"`_message__`.${field.scalaName.asSymbol})"
          )
      )
      .when(message.preservesUnknownFields)(
        _.add(
          "var `_unknownFields__`: _root_.scalapb.UnknownFieldSet.Builder = null"
        )
      )
      .when(requiredFieldMap.nonEmpty) { fp =>
        // Sets the bit 0...(n-1) inclusive to 1.
        def hexBits(n: Int): String = "0x%xL".format((0 to (n - 1)).map(i => (1L << i)).sum)
        val requiredFieldCount      = requiredFieldMap.size
        val fullWords               = (requiredFieldCount - 1) / 64
        val bits: Seq[String] = (1 to fullWords).map(_ => hexBits(64)) :+ hexBits(
          requiredFieldCount - 64 * fullWords
        )
        fp.print(bits.zipWithIndex) {
          case (fp, (bn, index)) =>
            fp.add(s"var __requiredFields$index: _root_.scala.Long = $bn")
        }
      }
      .print(message.getRealOneofs.asScala)((printer, oneof) =>
        printer.add(s"var __${oneof.scalaName.name} = `_message__`.${oneof.scalaName.nameSymbol}")
      )
      .add(s"""var _done__ = false
              |while (!_done__) {
              |  val _tag__ = _input__.readTag()
              |  _tag__ match {
              |    case 0 => _done__ = true""".stripMargin)
      .print(message.fields) { (printer, field) =>
        val p = {
          val newValBase = if (field.isMessage) {
            val defInstance =
              s"${field.getMessageType.scalaType.fullNameWithMaybeRoot(message)}.defaultInstance"
            val baseInstance =
              if (field.isRepeated) defInstance
              else {
                val expr =
                  if (field.isInOneof)
                    s"_message__.${fieldAccessorSymbol(field)}"
                  else s"__${field.scalaName}"
                val mappedType =
                  toBaseFieldType(field).apply(expr, field.enclosingType)
                if (field.isInOneof || field.supportsPresence)
                  (mappedType + s".getOrElse($defInstance)")
                else mappedType
              }
            s"_root_.scalapb.LiteParser.readMessage(_input__, $baseInstance)"
          } else if (field.isEnum)
            s"${field.getEnumType.scalaType.fullNameWithMaybeRoot(message)}.fromValue(_input__.readEnum())"
          else if (field.getType == Type.STRING) s"_input__.readStringRequireUtf8()"
          else s"_input__.read${Types.capitalizedType(field.getType)}()"

          val newVal = toCustomType(field)(newValBase)

          val updateOp =
            if (field.supportsPresence) s"__${field.scalaName} = Option($newVal)"
            else if (field.isInOneof) {
              s"__${field.getContainingOneof.scalaName.name} = ${field.oneOfTypeName.fullName}($newVal)"
            } else if (field.isRepeated) s"__${field.scalaName} += $newVal"
            else s"__${field.scalaName} = $newVal"

          printer
            .add(
              s"""    case ${(field.getNumber << 3) + Types.wireType(field.getType)} =>
                 |      $updateOp""".stripMargin
            )
            .when(field.isRequired) { p =>
              val fieldNumber = requiredFieldMap(field)
              p.add(
                s"      __requiredFields${fieldNumber / 64} &= 0x${"%x".format(~(1L << fieldNumber))}L"
              )
            }
        }

        if (field.isPackable) {
          val read = {
            val tmp = s"""_input__.read${Types.capitalizedType(field.getType)}()"""
            if (field.isEnum)
              s"${field.getEnumType.scalaType.fullName}.fromValue($tmp)"
            else tmp
          }
          val readExpr = toCustomType(field)(read)
          p.add(s"""    case ${(field.getNumber << 3) + Types.WIRETYPE_LENGTH_DELIMITED} => {
                   |      val length = _input__.readRawVarint32()
                   |      val oldLimit = _input__.pushLimit(length)
                   |      while (_input__.getBytesUntilLimit > 0) {
                   |        __${field.scalaName} += $readExpr
                   |      }
                   |      _input__.popLimit(oldLimit)
                   |    }""".stripMargin)
        } else p
      }
      .when(!message.preservesUnknownFields)(_.add("    case tag => _input__.skipField(tag)"))
      .when(message.preservesUnknownFields)(
        _.add(
          """    case tag =>
            |      if (_unknownFields__ == null) {
            |        _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder(_message__.unknownFields)
            |      }
            |      _unknownFields__.parseField(tag, _input__)""".stripMargin
        )
      )
      .add("  }")
      .add("}")
      .when(requiredFieldMap.nonEmpty) { p =>
        val r = (0 until (requiredFieldMap.size + 63) / 64)
          .map(i => s"__requiredFields$i != 0L")
          .mkString(" || ")
        p.add(
          s"""if (${r}) { throw new _root_.com.google.protobuf.InvalidProtocolBufferException("Message missing required fields.") } """
        )
      }
      .add(s"$myFullScalaName(")
      .indent
      .addWithDelimiter(",")(
        (message.fieldsWithoutOneofs ++ message.getRealOneofs.asScala).map {
          case e: FieldDescriptor if e.isRepeated =>
            s"  ${e.scalaName.asSymbol} = __${e.scalaName}.result()"
          case e: FieldDescriptor =>
            s"  ${e.scalaName.asSymbol} = __${e.scalaName}"
          case e: OneofDescriptor =>
            s"  ${e.scalaName.nameSymbol} = __${e.scalaName.name}"
        } ++ (if (message.preservesUnknownFields)
                Seq(
                  "  unknownFields = if (_unknownFields__ == null) _message__.unknownFields else _unknownFields__.result()"
                )
              else Seq())
      )
      .outdent
      .add(")")
      .outdent
      .add("}")
  }

  def generateToJavaProto(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaType.fullName
    printer
      .add(s"def toJavaProto(scalaPbSource: $myFullScalaName): ${message.javaTypeName} = {")
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
    val myFullScalaName = message.scalaType.fullName
    printer
      .add(
        s"def fromJavaProto(javaPbSource: ${message.javaTypeName}): $myFullScalaName = $myFullScalaName("
      )
      .indent
      .call { printer =>
        val normal = message.fields.collect {
          case field if !field.isInOneof =>
            val conversion =
              if (field.isMapField) javaMapFieldToScala("javaPbSource", field)
              else javaFieldToScala("javaPbSource", field)
            Seq(s"${field.scalaName.asSymbol} = $conversion")
        }
        val oneOfs = message.getRealOneofs.asScala.map {
          case oneOf =>
            val head =
              s"${oneOf.scalaName.nameSymbol} = javaPbSource.${oneOf.javaEnumName}.getNumber match {"
            val body = oneOf.fields.map { field =>
              s"  case ${field.getNumber} => ${field.oneOfTypeName.fullName}(${javaFieldToScala("javaPbSource", field)})"
            }
            val tail = Seq(s"  case _ => ${oneOf.empty.fullName}", "}")
            Seq(head) ++ body ++ tail
        }
        printer.addGroupsWithDelimiter(",")(normal ++ oneOfs)
      }
      .outdent
      .add(")")
  }

  def generateNoDefaultArgsFactory(
      message: Descriptor
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    val fields = constructorFields(message).filterNot(_ == ConstructorField.UnknownFields)

    printer
      .add(
        s"def of("
      )
      .indented(
        _.addWithDelimiter(",")(fields.map(_.nameAndType))
      )
      .add(
        s"): ${message.scalaType.fullNameWithMaybeRoot} = ${message.scalaType.fullNameWithMaybeRoot}("
      )
      .indented(
        _.addWithDelimiter(",")(fields.map(_.name))
      )
      .add(")")
  }

  def generateMessageReads(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    def transform(field: FieldDescriptor) =
      (if (!field.isEnum) Identity
       else
         (MethodApplication("number") andThen
           FunctionApplication(field.getEnumType.scalaType.fullName + ".fromValue"))) andThen
        toCustomTypeExpr(field)

    val myFullScalaName = message.scalaType.fullName
    printer
      .add(s"""implicit def messageReads: _root_.scalapb.descriptors.Reads[${myFullScalaName}] = _root_.scalapb.descriptors.Reads{
              |  case _root_.scalapb.descriptors.PMessage(__fieldsMap) =>
              |    _root_.scala.Predef.require(__fieldsMap.keys.forall(_.containingMessage == scalaDescriptor), \"FieldDescriptor does not match message type.\")
              |    ${myFullScalaName}(""".stripMargin)
      .indent(3)
      .call { printer =>
        val fields = message.fields.collect {
          case field if !field.isInOneof =>
            val baseTypeName = field.fieldMapCollection(
              if (field.isEnum) "_root_.scalapb.descriptors.EnumValueDescriptor"
              else field.baseSingleScalaTypeName
            )
            val value =
              s"__fieldsMap.get(scalaDescriptor.findFieldByNumber(${field.getNumber}).get)"
            val e =
              if (field.supportsPresence)
                s"$value.flatMap(_.as[$baseTypeName])"
              else if (field.isRepeated)
                s"$value.map(_.as[${baseTypeName}]).getOrElse(${field.fieldsMapEmptyCollection})"
              else if (field.isRequired)
                s"$value.get.as[$baseTypeName]"
              else {
                // This is for proto3, no default value.
                val t = defaultValueForGet(field, uncustomized = true) + (if (field.isEnum)
                                                                            ".scalaValueDescriptor"
                                                                          else "")
                s"$value.map(_.as[$baseTypeName]).getOrElse($t)"
              }

            s"${field.scalaName.asSymbol} = " + transform(field).apply(
              e,
              enclosingType = field.enclosingType
            )
        }
        val oneOfs = message.getRealOneofs.asScala.map { oneOf =>
          val elems = oneOf.fields.map { field =>
            val value =
              s"__fieldsMap.get(scalaDescriptor.findFieldByNumber(${field.getNumber}).get)"
            val typeName =
              if (field.isEnum) "_root_.scalapb.descriptors.EnumValueDescriptor"
              else field.baseSingleScalaTypeName
            val e = s"$value.flatMap(_.as[_root_.scala.Option[$typeName]])"
            (transform(field) andThen FunctionApplication(field.oneOfTypeName.fullName)).apply(
              e,
              EnclosingType.ScalaOption
            )
          }
          val expr =
            elems.reduceLeft((acc, field) =>
              s"$acc\n    .orElse[${oneOf.scalaType.fullName}]($field)"
            )
          s"${oneOf.scalaName.nameSymbol} = $expr\n    .getOrElse(${oneOf.empty.fullName})"
        }
        printer.addWithDelimiter(",")(fields ++ oneOfs)
      }
      .outdent(3)
      .add(s"""    )
              |  case _ => throw new RuntimeException(\"Expected PMessage\")
              |}""".stripMargin)
  }

  def generateDescriptors(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(
        s"""def javaDescriptor: _root_.com.google.protobuf.Descriptors.Descriptor = ${message.javaDescriptorSource}
           |def scalaDescriptor: _root_.scalapb.descriptors.Descriptor = ${message.scalaDescriptorSource}""".stripMargin
      )
  }

  def generateDefaultInstance(
      message: Descriptor
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaType.fullName
    printer
      .add(s"lazy val defaultInstance = $myFullScalaName(")
      .indent
      .addWithDelimiter(",")(message.fields.collect {
        case field if !field.isInOneof =>
          val default = defaultValueForDefaultInstance(field)
          s"${field.scalaName.asSymbol} = $default"
      } ++ message.getRealOneofs.asScala.map { oneof =>
        s"${oneof.scalaName.nameSymbol} = ${oneof.empty.fullName}"
      })
      .outdent
      .add(")")
  }

  def generateMessageLens(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val className           = message.scalaType.name
    def lensType(s: String) = s"_root_.scalapb.lenses.Lens[UpperPB, $s]"

    printer
      .add(
        s"implicit class ${className}Lens[UpperPB](_l: _root_.scalapb.lenses.Lens[UpperPB, ${message.scalaType.fullName}]) extends _root_.scalapb.lenses.ObjectLens[UpperPB, ${message.scalaType.fullName}](_l) {"
      )
      .indent
      .print(message.fields) {
        case (printer, field) =>
          val fieldName = field.scalaName.asSymbol
          if (!field.isInOneof) {
            if (field.supportsPresence) {
              val optionLensName = "optional" + field.upperScalaName
              printer
                .add(
                  s"""def $fieldName: ${lensType(field.singleScalaTypeName)} = field(_.${field.getMethod})((c_, f_) => c_.copy($fieldName = Option(f_)))
                     |def ${optionLensName}: ${lensType(field.scalaTypeName)} = field(_.$fieldName)((c_, f_) => c_.copy($fieldName = f_))""".stripMargin
                )
            } else
              printer.add(
                s"def $fieldName: ${lensType(field.scalaTypeName)} = field(_.$fieldName)((c_, f_) => c_.copy($fieldName = f_))"
              )
          } else {
            val oneofName = field.getContainingOneof.scalaName.nameSymbol
            printer
              .add(
                s"def $fieldName: ${lensType(field.scalaTypeName)} = field(_.${field.getMethod})((c_, f_) => c_.copy($oneofName = ${field.oneOfTypeName
                  .fullNameWithMaybeRoot(message)}(f_)))"
              )
          }
      }
      .print(message.getRealOneofs.asScala) {
        case (printer, oneof) =>
          val oneofName = oneof.scalaName.nameSymbol
          printer
            .add(
              s"def $oneofName: ${lensType(oneof.scalaType.fullNameWithMaybeRoot(message))} = field(_.$oneofName)((c_, f_) => c_.copy($oneofName = f_))"
            )
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

  def generateTypeMappers(
      fields: Seq[FieldDescriptor]
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    val customizedFields: Seq[(FieldDescriptor, String)] = for {
      field  <- fields
      custom <- field.customSingleScalaTypeName
    } yield (field, custom)

    printer
      .print(customizedFields) {
        case (printer, (field, customType)) =>
          val modifier =
            if (field.getContainingType.isMapEntry)
              s"private[${field.getContainingType.getContainingType.scalaType.nameSymbol}]"
            else s"private"
          printer
            .add("@transient")
            .add(
              s"$modifier val ${field.typeMapperValName}: _root_.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}] = implicitly[_root_.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}]]"
            )
      }
  }

  def generateTypeMappersForMapEntry(
      message: Descriptor
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    val pairToMessage = {
      val k = if (message.mapType.keyField.supportsPresence) "Some(__p._1)" else "__p._1"
      val v = if (message.mapType.valueField.supportsPresence) "Some(__p._2)" else "__p._2"
      s"__p => ${message.scalaType.fullName}($k, $v)"
    }

    val messageToPair = {
      val k = if (message.mapType.keyField.supportsPresence) "__m.getKey" else "__m.key"
      val v = if (message.mapType.valueField.supportsPresence) "__m.getValue" else "__m.value"
      s"__m => ($k, $v)"
    }

    printer
      .add(
        s"""@transient
           |implicit val keyValueMapper: _root_.scalapb.TypeMapper[${message.scalaType.fullName}, ${message.mapType.pairType}] =
           |  _root_.scalapb.TypeMapper[${message.scalaType.fullName}, ${message.mapType.pairType}]($messageToPair)($pairToMessage)""".stripMargin
      )
  }

  def generateMessageCompanionMatcher(methodName: String, messageNumbers: Seq[(Descriptor, Int)])(
      fp: FunctionalPrinter
  ): FunctionalPrinter = {
    val signature =
      s"def $methodName(__number: _root_.scala.Int): _root_.scalapb.GeneratedMessageCompanion[_] = "
    // Due to https://issues.scala-lang.org/browse/SI-9111 we can't directly return the companion
    // object.
    if (messageNumbers.nonEmpty)
      fp.add(signature + "{")
        .indent
        .add("var __out: _root_.scalapb.GeneratedMessageCompanion[_] = null")
        .add("(__number: @_root_.scala.unchecked) match {")
        .indent
        .print(messageNumbers) {
          case (fp, (f, number)) =>
            fp.add(s"case $number => __out = ${f.scalaType.fullName}")
        }
        .outdent
        .add("}")
        .add("__out")
        .outdent
        .add("}")
    else fp.add(signature + "throw new MatchError(__number)")
  }

  // Finding companion objects by field number
  def generateMessageCompanionForField(
      message: Descriptor
  )(fp: FunctionalPrinter): FunctionalPrinter =
    generateMessageCompanionMatcher(
      "messageCompanionForFieldNumber",
      message.fields.filter(_.isMessage).map(f => (f.getMessageType, f.getNumber))
    )(fp)

  // Finding companion objects for nested types.
  def generateNestedMessagesCompanions(
      message: Descriptor
  )(fp: FunctionalPrinter): FunctionalPrinter = {
    val signature =
      s"lazy val nestedMessagesCompanions: ${ProtobufGenerator.CompSeqType} ="
    if (message.nestedTypes.isEmpty)
      fp.add(signature + " Seq.empty")
    else
      fp.add(signature)
        .indent
        .add(ProtobufGenerator.CompSeqType + "(")
        .indent
        .addWithDelimiter(",")(message.nestedTypes.map(m => m.scalaType.fullNameWithMaybeRoot))
        .outdent
        .add(")")
        .outdent
  }

  // Finding companion objects for top-level types.
  def generateMessagesCompanions(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    val signature = s"lazy val messagesCompanions: ${ProtobufGenerator.CompSeqType} ="
    if (file.getMessageTypes.isEmpty)
      fp.add(signature + " Seq.empty")
    else
      fp.add(signature)
        .indent
        .add(ProtobufGenerator.CompSeqType + "(")
        .indent
        .addWithDelimiter(",")(file.getMessageTypes.asScala.map(_.scalaType.fullName).toSeq)
        .outdent
        .add(")")
        .outdent
  }

  def generateEnumCompanionForField(
      message: Descriptor
  )(fp: FunctionalPrinter): FunctionalPrinter = {
    val signature =
      "def enumCompanionForFieldNumber(__fieldNumber: _root_.scala.Int): _root_.scalapb.GeneratedEnumCompanion[_] = "
    if (message.fields.exists(_.isEnum))
      fp.add(signature + "{")
        .indent
        .add("(__fieldNumber: @_root_.scala.unchecked) match {")
        .indent
        .print(message.fields.filter(_.isEnum)) {
          case (fp, f) =>
            fp.add(s"case ${f.getNumber} => ${f.getEnumType.scalaType.fullName}")
        }
        .outdent
        .add("}")
        .outdent
        .add("}")
    else fp.add(signature + "throw new MatchError(__fieldNumber)")
  }

  def printExtension(fp: FunctionalPrinter, fd: FieldDescriptor) = {
    fp.add(
        s"val ${fd.scalaName.asSymbol}: _root_.scalapb.GeneratedExtension[${fd.getContainingType.scalaType.fullName}, ${fd.scalaTypeName}] ="
      )
      .call { fp =>
        val (listLens, fromFieldToBase, fromBaseToField) = fd.getType match {
          case Type.DOUBLE =>
            (
              "fixed64Lens",
              FunctionApplication("java.lang.Double.longBitsToDouble"),
              FunctionApplication("java.lang.Double.doubleToLongBits")
            )
          case Type.FLOAT =>
            (
              "fixed32Lens",
              FunctionApplication("java.lang.Float.intBitsToFloat"),
              FunctionApplication("java.lang.Float.floatToIntBits")
            )
          case Type.INT64   => ("varintLens", Identity, Identity)
          case Type.UINT64  => ("varintLens", Identity, Identity)
          case Type.INT32   => ("varintLens", MethodApplication("toInt"), MethodApplication("toLong"))
          case Type.FIXED64 => ("fixed64Lens", Identity, Identity)
          case Type.FIXED32 => ("fixed32Lens", Identity, Identity)
          case Type.BOOL =>
            (
              "varintLens",
              OperatorApplication("!= 0"),
              FunctionApplication("_root_.scalapb.GeneratedExtension.Internal.bool2Long")
            )
          case Type.STRING =>
            (
              "lengthDelimitedLens",
              MethodApplication("toStringUtf8()"),
              FunctionApplication("_root_.com.google.protobuf.ByteString.copyFromUtf8")
            )
          case Type.GROUP => throw new RuntimeException("Not supported")
          case Type.MESSAGE =>
            (
              "lengthDelimitedLens",
              FunctionApplication(
                s"_root_.scalapb.GeneratedExtension.readMessageFromByteString(${fd.baseSingleScalaTypeName})"
              ),
              MethodApplication(s"toByteString")
            )
          case Type.BYTES => ("lengthDelimitedLens", Identity, Identity)
          case Type.UINT32 =>
            ("varintLens", MethodApplication("toInt"), MethodApplication("toLong"))
          case Type.ENUM =>
            (
              "varintLens",
              MethodApplication("toInt") andThen FunctionApplication(
                fd.baseSingleScalaTypeName + ".fromValue"
              ),
              MethodApplication("value") andThen MethodApplication("toLong")
            )
          case Type.SFIXED32 => ("fixed32Lens", Identity, Identity)
          case Type.SFIXED64 => ("fixed64Lens", Identity, Identity)
          case Type.SINT32 =>
            (
              "varintLens",
              MethodApplication("toInt") andThen FunctionApplication(
                "_root_.com.google.protobuf.CodedInputStream.decodeZigZag32"
              ),
              FunctionApplication("_root_.com.google.protobuf.CodedOutputStream.encodeZigZag32") andThen (
                MethodApplication("toLong")
              )
            )
          case Type.SINT64 =>
            (
              "varintLens",
              FunctionApplication("_root_.com.google.protobuf.CodedInputStream.decodeZigZag64"),
              FunctionApplication("_root_.com.google.protobuf.CodedOutputStream.encodeZigZag64")
            )
        }
        val fromFieldToCustom = fromFieldToBase andThen toCustomTypeExpr(fd)
        val fromCustomToField = toBaseTypeExpr(fd) andThen fromBaseToField

        val (factoryMethod, args) = {
          if (fd.supportsPresence && !fd.isMessage)
            ("_root_.scalapb.GeneratedExtension.forOptionalUnknownField", Seq.empty)
          else if (fd.supportsPresence && fd.isMessage)
            ("_root_.scalapb.GeneratedExtension.forOptionalUnknownMessageField", Seq.empty)
          else if (fd.isRepeated && fd.isPackable)
            (
              "_root_.scalapb.GeneratedExtension.forRepeatedUnknownFieldPackable",
              Seq(fd.getType match {
                case Type.DOUBLE | Type.FIXED64 | Type.SFIXED64 => "_.readFixed64"
                case Type.FLOAT | Type.FIXED32 | Type.SFIXED32  => "_.readFixed32"
                case Type.UINT32 | Type.UINT64 | Type.INT32 | Type.INT64 | Type.ENUM | Type.BOOL |
                    Type.SINT32 | Type.SINT64 =>
                  "_.readInt64"
                case _ =>
                  throw new GeneratorException(s"Unexpected packable type: ${fd.getType.name()}")
              })
            )
          else if (fd.isRepeated && !fd.isPackable) {
            ("_root_.scalapb.GeneratedExtension.forRepeatedUnknownFieldUnpackable", Seq())
          } else
            (
              if (!fd.isMessage) "_root_.scalapb.GeneratedExtension.forSingularUnknownField"
              else "_root_.scalapb.GeneratedExtension.forSingularUnknownMessageField",
              Seq(defaultValueForGet(fd))
            )
        }
        val argList = Seq(
          s"{__valueIn => ${fromFieldToCustom("__valueIn", EnclosingType.None)}}",
          s"{__valueIn => ${fromCustomToField("__valueIn", EnclosingType.None)}}"
        ) ++ args
        fp.add(
          s"  $factoryMethod(${fd.getNumber}, _root_.scalapb.UnknownFieldSet.Field.$listLens)(${argList
            .mkString(", ")})"
        )
      }
  }

  def generateMessageCompanion(
      message: Descriptor
  )(printer: FunctionalPrinter): FunctionalPrinter = {
    val className     = message.scalaType.nameSymbol
    val companionType = message.companionBaseClasses.mkString(" with ")
    printer
      .seq(message.companionAnnotationList)
      .add(s"""object $className extends $companionType {
              |  implicit def messageCompanion: $companionType = this""".stripMargin)
      .indent
      .when(message.javaConversions)(generateToJavaProto(message))
      .when(message.javaConversions)(generateFromJavaProto(message))
      .call(generateMerge(message))
      .call(generateMessageReads(message))
      .call(generateDescriptors(message))
      .call(generateMessageCompanionForField(message))
      .call(generateNestedMessagesCompanions(message))
      .call(generateEnumCompanionForField(message))
      .call(generateDefaultInstance(message))
      .print(message.getEnumTypes.asScala)(printEnum)
      .print(message.getRealOneofs.asScala)(printOneof)
      .print(message.nestedTypes)(printMessage)
      .print(message.getExtensions.asScala)(printExtension)
      .when(message.generateLenses)(generateMessageLens(message))
      .call(generateFieldNumbers(message))
      .call(generateTypeMappers(message.fields ++ message.getExtensions.asScala))
      .when(message.isMapEntry)(generateTypeMappersForMapEntry(message))
      .call(generateNoDefaultArgsFactory(message))
      .add(s"// @@protoc_insertion_point(${message.messageCompanionInsertionPoint.insertionPoint})")
      .outdent
      .add("}")
      .add("")
  }

  def generateScalaDoc(enum: EnumDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(enum.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty))
    fp.add(lines: _*)
  }

  def generateScalaDoc(enumValue: EnumValueDescriptor): PrinterEndo = { fp =>
    val lines = asScalaDocBlock(enumValue.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty))
    fp.add(lines: _*)
  }

  def generateScalaDoc(message: Descriptor): PrinterEndo = { fp =>
    val mainDoc: Seq[String] = {
      val base = message.comment.map(_.split('\n').toSeq).getOrElse(Seq.empty)
      // Hack: there's an heading in `any.proto` that causes a Scaladoc
      // warning.
      if (message.getFullName() != "google.protobuf.Any")
        base
      else
        base.collect {
          case l if l.trim == "====" => ""
          case l                     => l
        }
    }

    val fieldsDoc: Seq[String] = message.fields
      .filterNot(_.isInOneof)
      .map { fd => (fd, fd.comment.map(_.split("\n").toSeq).getOrElse(Seq.empty)) }
      .filter(_._2.nonEmpty)
      .flatMap {
        case (fd, lines) =>
          Seq(s"@param ${fd.scalaName}") ++ lines.map("  " + _)
      }

    val sep = if (mainDoc.nonEmpty && fieldsDoc.nonEmpty) Seq("") else Seq.empty

    fp.add(asScalaDocBlock(mainDoc ++ sep ++ fieldsDoc): _*)
  }

  def printMessage(printer: FunctionalPrinter, message: Descriptor): FunctionalPrinter = {
    val fullName = message.scalaType.fullNameWithMaybeRoot(message)
    printer
      .call(new SealedOneofsGenerator(message, implicits).generateSealedOneofTrait)
      .call(generateScalaDoc(message))
      .add(s"@SerialVersionUID(0L)")
      .seq(message.annotationList)
      .add(s"final case class ${message.scalaType.nameSymbol}(")
      .indent
      .indent
      .call(printConstructorFieldList(message))
      .add(s") extends ${message.baseClasses.mkString(" with ")} {")
      .call(generateSerializedSizeForPackedFields(message))
      .call(generateSerializedSize(message))
      .call(generateWriteTo(message))
      .print(message.fields) {
        case (printer, field) =>
          val withMethod  = "with" + field.upperScalaName
          val clearMethod = "clear" + field.upperScalaName
          val singleType  = field.singleScalaTypeName
          printer
            .when(field.supportsPresence || field.isInOneof) { p =>
              val default = defaultValueForGet(field)
              p.add(
                s"def ${field.getMethod}: ${field.singleScalaTypeName} = ${fieldAccessorSymbol(field)}.getOrElse($default)"
              )
            }
            .when(field.supportsPresence) { p =>
              p.add(
                s"""def $clearMethod: ${message.scalaType.nameSymbol} = copy(${field.scalaName.asSymbol} = ${C.None})
                   |def $withMethod(__v: ${singleType}): ${message.scalaType.nameSymbol} = copy(${field.scalaName.asSymbol} = Option(__v))""".stripMargin
              )
            }
            .when(field.isInOneof) { p =>
              p.add(
                s"""def $withMethod(__v: ${singleType}): ${message.scalaType.nameSymbol} = copy(${field.getContainingOneof.scalaName.nameSymbol} = ${field.oneOfTypeName
                  .fullNameWithMaybeRoot(message)}(__v))"""
              )
            }
            .when(field.isRepeated) { p =>
              val emptyValue = field.emptyCollection
              p.add(
                s"""def $clearMethod = copy(${field.scalaName.asSymbol} = $emptyValue)
                   |def add${field.upperScalaName}(__vs: $singleType*): ${message.scalaType.nameSymbol} = addAll${field.upperScalaName}(__vs)
                   |def addAll${field.upperScalaName}(__vs: Iterable[$singleType]): ${message.scalaType.nameSymbol} = copy(${field.scalaName.asSymbol} = ${field.scalaName.asSymbol} ++ __vs)""".stripMargin
              )
            }
            .when(field.isRepeated || field.isSingular) {
              _.add(
                s"def $withMethod(__v: ${field.scalaTypeName}): ${message.scalaType.nameSymbol} = copy(${field.scalaName.asSymbol} = __v)"
              )
            }
      }
      .print(message.getRealOneofs.asScala) {
        case (printer, oneof) =>
          printer.add(
            s"""def clear${oneof.scalaType.name}: ${message.scalaType.nameSymbol} = copy(${oneof.scalaName.nameSymbol} = ${oneof.empty
                 .fullNameWithMaybeRoot(message)})
               |def with${oneof.scalaType.name}(__v: ${oneof.scalaType.fullNameWithMaybeRoot(
                 message
               )}): ${message.scalaType.nameSymbol} = copy(${oneof.scalaName.nameSymbol} = __v)""".stripMargin
          )
      }
      .when(message.preservesUnknownFields)(
        _.add(
          s"""def withUnknownFields(__v: ${C.UnknownFieldSet}) = copy(unknownFields = __v)
             |def discardUnknownFields = copy(unknownFields = ${C.UnknownFieldSetEmpty})""".stripMargin
        )
      )
      .call(generateGetField(message))
      .call(generateGetFieldPValue(message))
      .when(!params.singleLineToProtoString)(
        _.add(
          s"def toProtoString: _root_.scala.Predef.String = " +
            "_root_.scalapb.TextFormat.printToUnicodeString(this)"
        )
      )
      .when(params.singleLineToProtoString)(
        _.add(
          s"def toProtoString: _root_.scala.Predef.String = " +
            "_root_.scalapb.TextFormat.printToSingleLineUnicodeString(this)"
        )
      )
      .when(params.asciiFormatToString)(
        _.add("override def toString: _root_.scala.Predef.String = toProtoString")
      )
      .add(s"def companion = ${fullName}")
      .when(message.isSealedOneofType) { fp =>
        val scalaType = message.sealedOneofScalaType
        val name      = message.sealedOneofTraitScalaType.name
        fp.add(
          s"def to$name: $scalaType = ${message.sealedOneofTypeMapper.fullName}.toCustom(this)"
        )
      }
      .outdent
      .outdent
      .add(s"""}
              |""".stripMargin)
      .call(generateMessageCompanion(message))
  }

  def scalaFileHeader(file: FileDescriptor, javaConverterImport: Boolean): FunctionalPrinter = {
    if (file.scalaOptions.getPreambleList.asScala.nonEmpty && !file.scalaOptions.getSingleFile) {
      throw new GeneratorException(
        s"${file.getName}: single_file must be true when a preamble is provided."
      )
    }
    new FunctionalPrinter()
      .add(s"""// Generated by the Scala Plugin for the Protocol Buffer Compiler.
              |// Do not edit!
              |//
              |// Protofile syntax: ${file.getSyntax.toString}
              |""".stripMargin)
      .when(file.scalaPackage.fullName.nonEmpty)(
        _.add("package " + file.scalaPackage.fullName).add()
      )
      .when(javaConverterImport)(
        _.add("import _root_.scalapb.internal.compat.JavaConverters._").add()
      )
      .print(file.scalaOptions.getImportList.asScala) {
        case (printer, i) => printer.add(s"import $i")
      }
      .add("")
      .seq(file.scalaOptions.getPreambleList.asScala.toSeq)
      .when(file.scalaOptions.getPreambleList.asScala.nonEmpty)(_.add(""))
  }

  def updateDescriptor(tmp: FileDescriptor): DescriptorProtos.FileDescriptorProto = {
    def updateField(field: FieldDescriptor): DescriptorProtos.FieldDescriptorProto = {
      val fb         = field.toProto.toBuilder
      val extBuilder = fb.getOptions.getExtension[FieldOptions](Scalapb.field).toBuilder
      assert(!extBuilder.hasScalaName || extBuilder.getScalaName == field.scalaName)
      extBuilder.setScalaName(field.scalaName)
      fb.getOptionsBuilder.setExtension(Scalapb.field, extBuilder.build())
      fb.build()
    }

    def updateMessageType(msg: Descriptor): DescriptorProtos.DescriptorProto = {
      msg.toProto.toBuilder
        .clearField()
        .addAllField(msg.getFields.asScala.map(updateField(_)).asJava)
        .clearNestedType()
        .addAllNestedType(msg.getNestedTypes.asScala.map(updateMessageType(_)).asJava)
        .build()
    }

    val fileProto = tmp.toProto
    fileProto.toBuilder
      .clearMessageType()
      .addAllMessageType(tmp.getMessageTypes.asScala.map(updateMessageType).asJava)
      .build
  }

  def generateFileDescriptor(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    val descriptor = {
      val withScalaName = updateDescriptor(file)

      if (file.retainSourceCodeInfo) withScalaName
      else withScalaName.toBuilder.clearSourceCodeInfo.build
    }

    // Encoding the file descriptor proto in base64. JVM has a limit on string literal to be up
    // to 64k, so we chunk it into a sequence and combining in run time.  The chunks are less
    // than 64k to account for indentation and new lines.
    val base64: Seq[Seq[String]] = scalapb.internal.Encoding
      .toBase64(descriptor.toByteArray)
      .grouped(55000)
      .map { group =>
        val lines = ("\"\"\"" + group).grouped(100).toSeq
        lines.dropRight(1) :+ (lines.last + "\"\"\"")
      }
      .toSeq
    fp.add("private lazy val ProtoBytes: Array[Byte] =")
      .add("    scalapb.Encoding.fromBase64(scala.collection.immutable.Seq(")
      .addGroupsWithDelimiter(",")(base64)
      .add("    ).mkString)")
      .add("lazy val scalaDescriptor: _root_.scalapb.descriptors.FileDescriptor = {")
      .add(
        "  val scalaProto = com.google.protobuf.descriptor.FileDescriptorProto.parseFrom(ProtoBytes)"
      )
      .add(
        "  _root_.scalapb.descriptors.FileDescriptor.buildFrom(scalaProto, dependencies.map(_.scalaDescriptor))"
      )
      .add("}")
      .when(file.javaConversions) {
        _.add("lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor =")
          .add(s"  ${file.javaFullOuterClassName}.getDescriptor()")
      }
      .when(!file.javaConversions) {
        _.add("lazy val javaDescriptor: com.google.protobuf.Descriptors.FileDescriptor = {")
          .add(
            "  val javaProto = com.google.protobuf.DescriptorProtos.FileDescriptorProto.parseFrom(ProtoBytes)"
          )
          .add("  com.google.protobuf.Descriptors.FileDescriptor.buildFrom(javaProto, Array(")
          .addWithDelimiter(",")(file.getDependencies.asScala.map { d =>
            s"    ${d.fileDescriptorObject.fullName}.javaDescriptor"
          }.toSeq)
          .add("  ))")
          .add("}")
      }
      .add(
        """@deprecated("Use javaDescriptor instead. In a future version this will refer to scalaDescriptor.", "ScalaPB 0.5.47")"""
      )
      .add("def descriptor: com.google.protobuf.Descriptors.FileDescriptor = javaDescriptor")
  }

  def generateServiceFiles(file: FileDescriptor): Seq[CodeGeneratorResponse.File] = {
    if (params.grpc) {
      file.getServices.asScala.map { service =>
        val p    = new GrpcServicePrinter(service, implicits)
        val code = p.printService(FunctionalPrinter()).result()
        val b    = CodeGeneratorResponse.File.newBuilder()
        b.setName(file.scalaDirectory + "/" + service.companionObject.name + ".scala")
        b.setContent(code)
        b.build
      }
    }.toSeq
    else Nil
  }

  def generateFileObject(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter = {
    fp.add(
        s"object ${file.fileDescriptorObject.nameSymbol} extends _root_.scalapb.GeneratedFileObject {"
      )
      .indent
      .when(file.getDependencies.isEmpty) {
        _.add("lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq.empty")
      }
      .when(!file.getDependencies.isEmpty) {
        _.add("lazy val dependencies: Seq[_root_.scalapb.GeneratedFileObject] = Seq(").indent
          .addWithDelimiter(",")(
            file.getDependencies.asScala.map(_.fileDescriptorObject.fullName).toSeq
          )
          .outdent
          .add(")")
      }
      .call(generateMessagesCompanions(file)(_))
      .call(generateFileDescriptor(file)(_))
      .print(file.getExtensions.asScala)(printExtension(_, _))
      .call(generateTypeMappers(file.getExtensions.asScala.toSeq))
      .outdent
      .add("}")
  }

  private def messageContainsRepeatedFields(message: Descriptor): Boolean = {
    message.fields.exists(_.isRepeated) || message.nestedTypes.exists(messageContainsRepeatedFields)
  }

  def generateSingleScalaFileForFileDescriptor(
      file: FileDescriptor
  ): Seq[CodeGeneratorResponse.File] = {
    val code =
      scalaFileHeader(
        file,
        file.javaConversions && file.getMessageTypes.asScala.exists(messageContainsRepeatedFields)
      ).print(file.getEnumTypes.asScala)(printEnum)
        .print(file.getMessageTypes.asScala)(printMessage)
        .call(generateFileObject(file))
        .result()
    val b = CodeGeneratorResponse.File.newBuilder()
    b.setName(file.scalaFileName)
    b.setContent(code)
    generateServiceFiles(file) :+ b.build
  }

  def generateMultipleScalaFilesForFileDescriptor(
      file: FileDescriptor
  ): Seq[CodeGeneratorResponse.File] = {
    val serviceFiles = generateServiceFiles(file)

    val enumFiles = for {
      enum <- file.getEnumTypes.asScala
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaDirectory + "/" + enum.getName + ".scala")
      b.setContent(
        scalaFileHeader(file, false)
          .call(printEnum(_, enum))
          .result()
      )
      b.build
    }

    val messageFiles = for {
      message <- file.getMessageTypes.asScala if !message.isSealedOneofCase
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(message.scalaFileName)
      b.setContent(
        scalaFileHeader(
          file,
          javaConverterImport = file.javaConversions && messageContainsRepeatedFields(message)
        ).call(printMessage(_, message))
          .print(message.sealedOneofCases.getOrElse(Seq.empty))(printMessage)
          .result()
      )
      b.build
    }

    val fileDescriptorObjectFile = {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaFileName)
      b.setContent(
        scalaFileHeader(file, false)
          .call(generateFileObject(file))
          .result()
      )
      b.build
    }

    serviceFiles ++ enumFiles ++ messageFiles :+ fileDescriptorObjectFile
  }
}

private[this] object C {
  val None                 = "_root_.scala.None"
  val UnknownFieldSet      = "_root_.scalapb.UnknownFieldSet"
  val UnknownFieldSetEmpty = "_root_.scalapb.UnknownFieldSet.empty"
}

object ProtobufGenerator {
  def parseParameters(params: String): Either[String, GeneratorParams] =
    GeneratorParams.fromString(params)

  def handleCodeGeneratorRequest(request: CodeGenRequest): CodeGenResponse = {
    parseParameters(request.parameter) match {
      case Right(params) =>
        val implicits = new DescriptorImplicits(params, request.allProtos)
        val generator = new ProtobufGenerator(params, implicits)
        val validator = new ProtoValidation(implicits)
        validator.validateFiles(request.allProtos)
        import implicits.FileDescriptorPimp
        val files = request.filesToGenerate.flatMap { file =>
          if (file.scalaOptions.getSingleFile)
            generator.generateSingleScalaFileForFileDescriptor(file)
          else generator.generateMultipleScalaFilesForFileDescriptor(file)
        }
        CodeGenResponse.succeed(files, Set(CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL))
      case Left(error) =>
        CodeGenResponse.fail(error)
    }
  }

  def asScalaDocBlock(contentLines: Seq[String]): Seq[String] = {
    if (contentLines.nonEmpty) {
      contentLines.zipWithIndex.map {
        case (line, index) =>
          val prefix = if (index == 0) "/**" else "  *"
          if (line.startsWith(" ") || line.isEmpty) (prefix + line) else (prefix + " " + line)
      } :+ "  */"
    } else contentLines
  }

  val deprecatedAnnotation: String =
    """@scala.deprecated(message="Marked as deprecated in proto file", "")"""

  private val CompSeqType =
    "Seq[_root_.scalapb.GeneratedMessageCompanion[_ <: _root_.scalapb.GeneratedMessage]]"

  private[scalapb] def escapeScalaString(raw: String): String =
    raw
      .map {
        case '\b'                      => "\\b"
        case '\f'                      => "\\f"
        case '\n'                      => "\\n"
        case '\r'                      => "\\r"
        case '\t'                      => "\\t"
        case '\\'                      => "\\\\"
        case '\"'                      => "\\\""
        case '\''                      => "\\\'"
        case u if u >= ' ' && u <= '~' => u.toString
        case c: Char                   => "\\u%4s".format(c.toInt.toHexString).replace(' ', '0')
      }
      .mkString("\"", "", "\"")
}
