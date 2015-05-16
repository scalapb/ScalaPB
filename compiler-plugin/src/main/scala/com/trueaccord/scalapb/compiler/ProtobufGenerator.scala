package com.trueaccord.scalapb.compiler

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors._
import com.google.protobuf.{CodedOutputStream, ByteString}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scala.collection.JavaConversions._

case class GeneratorParams(javaConversions: Boolean = false, flatPackage: Boolean = false)

class ProtobufGenerator(val params: GeneratorParams) extends DescriptorPimps {
  def printEnum(e: EnumDescriptor, printer: FunctionalPrinter): FunctionalPrinter = {
    val name = e.getName.asSymbol
    printer
      .add(s"sealed trait $name extends com.trueaccord.scalapb.GeneratedEnum {")
      .indent
      .print(e.getValues) {
      case (v, p) => p.add(
        s"def is${v.objectName}: Boolean = false")
    }
      .outdent
      .add("}")
      .add("")
      .add(s"object $name extends com.trueaccord.scalapb.GeneratedEnumCompanion[$name] {")
      .indent
      .print(e.getValues) {
      case (v, p) => p.addM(
        s"""case object ${v.getName.asSymbol} extends $name {
           |  val id = ${v.getNumber}
           |  val name = "${v.getName}"
           |  override def is${v.objectName}: Boolean = true
           |}
           |""")
    }
      .add(s"lazy val values = Seq(${e.getValues.map(_.getName.asSymbol).mkString(", ")})")
      .add(s"def fromValue(value: Int): $name = value match {")
      .print(e.getValues) {
      case (v, p) => p.add(s"  case ${v.getNumber} => ${v.getName.asSymbol}")
    }
      .addM(
        s"""}
           |lazy val descriptor = new Descriptors.EnumDescriptor(${e.getIndex}, "${e.getName}", this)""")
      .when(params.javaConversions) {
      _.addM(
        s"""|def fromJavaValue(pbJavaSource: ${e.javaTypeName}): $name = fromValue(pbJavaSource.getNumber)
            |def toJavaValue(pbScalaSource: $name): ${e.javaTypeName} = ${e.javaTypeName}.valueOf(pbScalaSource.id)""")
    }
      .outdent
      .add("}")
  }

  def printOneof(e: OneofDescriptor, printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(s"sealed trait ${e.upperScalaName} {")
      .indent
      .add(s"def isEmpty: Boolean = false")
      .add(s"def isDefined: Boolean = true")
      .add(s"def number: Int")
      .print(e.fields) {
      case (v, p) => p
        .add(s"def is${v.upperScalaName}: Boolean = false")
    }
      .print(e.fields) {
      case (v, p) => p
        .add(s"def ${v.scalaName.asSymbol}: Option[${v.scalaTypeName}] = None")
    }
      .outdent
      .addM(
        s"""}
           |object ${e.upperScalaName} extends {
           |  case object Empty extends ${e.upperScalaName} {
           |    override def isEmpty: Boolean = true
           |    override def isDefined: Boolean = false
           |    override def number: Int = 0
           |  }
           |""")
      .indent
      .print(e.fields) {
      case (v, p) =>
        p.addM(
          s"""case class ${v.upperScalaName}(value: ${v.scalaTypeName}) extends ${e.upperScalaName} {
             |  override def is${v.upperScalaName}: Boolean = true
             |  override def ${v.scalaName.asSymbol}: Option[${v.scalaTypeName}] = Some(value)
             |  override def number: Int = ${v.getNumber}
             |}""")
    }
    .outdent
    .add("}")
  }

  def escapeString(raw: String): String = {
    import scala.reflect.runtime.universe._
    Literal(Constant(raw)).toString
  }

  def byteArrayAsBase64Literal(buffer: Array[Byte]): String = {
    "\"\"\"" + new sun.misc.BASE64Encoder().encode(buffer) + "\"\"\""
  }

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
      case FieldDescriptor.JavaType.BYTE_STRING => defaultValue.asInstanceOf[ByteString]
        .map(_.toString).mkString("com.google.protobuf.ByteString.copyFrom(Array[Byte](", ", ", "))")
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
    else if (field.isMap) "Map.empty"
    else if (field.isRepeated) "Nil"
    else defaultValueForGet(field)

  sealed trait ValueConversion
  case class ConversionMethod(name: String) extends ValueConversion
  case class ConversionFunction(name: String) extends ValueConversion
  case object NoOp extends ValueConversion

  def javaToScalaConversion(field: FieldDescriptor) = {
    val baseValueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => ConversionMethod("intValue")
      case FieldDescriptor.JavaType.LONG => ConversionMethod("longValue")
      case FieldDescriptor.JavaType.FLOAT => ConversionMethod("floatValue")
      case FieldDescriptor.JavaType.DOUBLE => ConversionMethod("doubleValue")
      case FieldDescriptor.JavaType.BOOLEAN => ConversionMethod("booleanValue")
      case FieldDescriptor.JavaType.BYTE_STRING => NoOp
      case FieldDescriptor.JavaType.STRING => NoOp
      case FieldDescriptor.JavaType.MESSAGE => ConversionFunction(
        field.getMessageType.scalaTypeName + ".fromJavaProto")
      case FieldDescriptor.JavaType.ENUM => ConversionFunction(
        field.getEnumType.scalaTypeName + ".fromJavaValue")
    }
    field.customSingleScalaTypeName match {
      case None => baseValueConversion
      case Some(customType) => baseValueConversion match {
        case t: ConversionMethod => t
        case NoOp => ConversionFunction(field.typeMapper + ".toCustom")
        case ConversionFunction(f) => ConversionFunction(s"($f _).andThen(${field.typeMapper}.toCustom)")
      }
    }
  }


  def javaFieldToScala(container: String, field: FieldDescriptor) = {
    val javaGetter = container + ".get" + field.upperScalaName
    val javaHas = container + ".has" + field.upperScalaName

    val valueConversion = javaToScalaConversion(field)

    valueConversion match {
      case ConversionMethod(method) if field.customSingleScalaTypeName.isEmpty =>
        if (field.isRepeated) s"${javaGetter}List.map(_.$method)"
        else if (field.supportsPresence) s"if ($javaHas) Some($javaGetter.$method) else None"
        else s"$javaGetter.$method"
      case ConversionMethod(method) if field.customSingleScalaTypeName.isDefined =>
        val typeMapper = field.typeMapper + ".toCustom"
        if (field.isRepeated) s"${javaGetter}List.map(__x => $typeMapper(__x.$method))"
        else if (field.supportsPresence) s"if ($javaHas) Some($typeMapper($javaGetter.$method)) else None"
        else s"$typeMapper($javaGetter.$method)"
      case ConversionFunction(func) =>
        if (field.isRepeated) s"${javaGetter}List.map($func)"
        else if (field.supportsPresence) s"if ($javaHas) Some($func($javaGetter)) else None"
        else s"$func($javaGetter)"
      case NoOp =>
        if (field.isRepeated) s"${javaGetter}List.toSeq"
        else if (field.supportsPresence) s"if ($javaHas) Some($javaGetter) else None"
        else javaGetter
    }
  }

  def javaMapFieldToScala(container: String, field: FieldDescriptor) = {
    // TODO(thesamet): if both unit conversions are NoOp, we can omit the map call.
    def unitConversion(n: String, field: FieldDescriptor) = javaToScalaConversion(field) match {
      case ConversionMethod(f) => s"$n.$f"
      case ConversionFunction(f) => s"$f($n)"
      case NoOp => s"$n"
    }
    s"${container}.get${field.upperScalaName}.map(pv => (${unitConversion("pv._1", field.mapType.keyField)}, ${unitConversion("pv._2", field.mapType.valueField)})).toMap"
  }

  def assignScalaMapToJava(scalaObject: String, javaObject: String, field: FieldDescriptor): String = {
    def valueConvert(v: String, field: FieldDescriptor) = {
      val c = field.getJavaType match {
        case FieldDescriptor.JavaType.INT => ConversionFunction("Int.box")
        case FieldDescriptor.JavaType.LONG => ConversionFunction("Long.box")
        case FieldDescriptor.JavaType.FLOAT => ConversionFunction("Float.box")
        case FieldDescriptor.JavaType.DOUBLE => ConversionFunction("Double.box")
        case FieldDescriptor.JavaType.BOOLEAN => ConversionFunction("Boolean.box")
        case FieldDescriptor.JavaType.BYTE_STRING => NoOp
        case FieldDescriptor.JavaType.STRING => NoOp
        case FieldDescriptor.JavaType.MESSAGE => ConversionFunction(
          field.getMessageType.scalaTypeName + ".toJavaProto")
        case FieldDescriptor.JavaType.ENUM => ConversionFunction(
          field.getEnumType.scalaTypeName + ".toJavaValue")
      }
      c match {
        case ConversionFunction(f) => s"$f($v)"
        case ConversionMethod(method) =>
          throw new RuntimeException("Unexpected method type")
        case NoOp => v
      }
    }

    s"""$javaObject
       |  .getMutable${field.upperScalaName}
       |  .putAll(
       |    $scalaObject.${fieldAccessorSymbol(field)}.map {
       |      __kv => (${valueConvert("__kv._1", field.mapType.keyField)}, ${valueConvert("__kv._2", field.mapType.valueField)})
       |  })
       |""".stripMargin
  }

  def assignScalaFieldToJava(scalaObject: String,
                             javaObject: String, field: FieldDescriptor): String = if (field.isMap)
    assignScalaMapToJava(scalaObject, javaObject, field) else {
    val javaSetter = javaObject +
      (if (field.isRepeated) ".addAll" else
        ".set") + field.upperScalaName
    val scalaGetter = scalaObject + "." + fieldAccessorSymbol(field)

    def convertIfRepeated(s: String) = if (field.isRepeated) ConversionFunction(s) else NoOp

    val baseValueConversion = field.getJavaType match {
      case FieldDescriptor.JavaType.INT => convertIfRepeated("Int.box")
      case FieldDescriptor.JavaType.LONG => convertIfRepeated("Long.box")
      case FieldDescriptor.JavaType.FLOAT => convertIfRepeated("Float.box")
      case FieldDescriptor.JavaType.DOUBLE => convertIfRepeated("Double.box")
      case FieldDescriptor.JavaType.BOOLEAN => convertIfRepeated("Boolean.box")
      case FieldDescriptor.JavaType.BYTE_STRING => NoOp
      case FieldDescriptor.JavaType.STRING => NoOp
      case FieldDescriptor.JavaType.MESSAGE => ConversionFunction(
        field.getMessageType.scalaTypeName + ".toJavaProto")
      case FieldDescriptor.JavaType.ENUM => ConversionFunction(
        field.getEnumType.scalaTypeName + ".toJavaValue")
    }
    val valueConversion = field.customSingleScalaTypeName match {
      case None => baseValueConversion
      case Some(customType) => baseValueConversion match {
        case NoOp => ConversionFunction(field.typeMapper + ".toBase")
        case ConversionFunction(f) => ConversionFunction(s"($f _).compose(${field.typeMapper}.toBase)")
        case _ => throw new RuntimeException("Unsupported yet")
      }
    }
    valueConversion match {
      case ConversionMethod(method) =>
        throw new RuntimeException("Unexpected method type")
      case ConversionFunction(func) =>
        if (field.isRepeated)
          s"$javaSetter($scalaGetter.map($func))"
        else if (field.isSingular)
          s"$javaSetter($func($scalaGetter))"
        else
          s"$scalaGetter.map($func).foreach($javaSetter)"
      case NoOp =>
        if (field.isRepeated)
          s"$javaSetter($scalaGetter)"
        else if (field.isSingular)
          s"$javaSetter($scalaGetter)"
        else
          s"$scalaGetter.foreach($javaSetter)"
    }
  }

    def generateGetField(message: Descriptor)(fp: FunctionalPrinter) = {
      val signature = "def getField(__field: Descriptors.FieldDescriptor): Any = "
      if (message.getFields.nonEmpty)
        fp.add(signature + "{")
          .indent
          .add("__field.number match {")
          .indent
          .print(message.getFields) {
          case (f, fp) if f.customSingleScalaTypeName.isEmpty => fp.add(s"case ${f.getNumber} => ${fieldAccessorSymbol(f)}")
          case (f, fp) if f.isRequired => fp.add(s"case ${f.getNumber} => ${toBaseType(f)(fieldAccessorSymbol(f))}")
          case (f, fp) => fp.add(s"case ${f.getNumber} => ${mapToBaseType(f)(fieldAccessorSymbol(f))}")
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
        fp.addM(
          s"""output.writeTag(${field.getNumber}, 2)
             |output.writeRawVarint32($valueExpr.serializedSize)
             |$valueExpr.writeTo(output)""")
    } else if (field.isEnum)
        fp.add(s"output.writeEnum(${field.getNumber}, $valueExpr.id)")
    else {
      val capTypeName = Types.capitalizedType(field.getType)
      fp.add(s"output.write$capTypeName(${field.getNumber}, $valueExpr)")
    }
  }

  def sizeExpressionForSingleField(field: FieldDescriptor, expr: String): String =
    if (field.isMessage) {
      val size = s"$expr.serializedSize"
      CodedOutputStream.computeTagSize(field.getNumber) + s" + com.google.protobuf.CodedOutputStream.computeRawVarint32Size($size) + $size"
    } else if(field.isEnum)
      s"com.google.protobuf.CodedOutputStream.computeEnumSize(${field.getNumber}, ${expr}.id)"
    else {
      val capTypeName = Types.capitalizedType(field.getType)
      s"com.google.protobuf.CodedOutputStream.compute${capTypeName}Size(${field.getNumber}, ${expr})"
    }

  def fieldAccessorSymbol(field: FieldDescriptor) =
    if (field.isInOneof)
      (field.getContainingOneof.scalaName.asSymbol + "." +
        field.scalaName.asSymbol)
    else
      field.scalaName.asSymbol

  def mapToBaseType(field: FieldDescriptor)(expr: String) =
    field.customSingleScalaTypeName.fold(expr)(customType =>
      s"""$expr.map(${field.typeMapper}.toBase)""")

  def toBaseType(field: FieldDescriptor)(expr: String) =
    field.customSingleScalaTypeName.fold(expr)(customType =>
      s"""${field.typeMapper}.toBase($expr)""")

  def toCustomType(field: FieldDescriptor)(expr: String) =
    field.customSingleScalaTypeName.fold(expr)(customType =>
      s"""${field.typeMapper}.toCustom($expr)""")

  def mapToCustomType(field: FieldDescriptor)(expr: String) =
    field.customSingleScalaTypeName.fold(expr)(customType =>
      s"""$expr.map(${field.typeMapper}.toCustom)""")

  def generateSerializedSizeForField(field: FieldDescriptor, fp: FunctionalPrinter): FunctionalPrinter = {
    val fieldNameSymbol = fieldAccessorSymbol(field)

    if (field.isRequired) {
      fp.add("__size += " + sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol)))
    } else if (field.isSingular) {
      fp.add(s"if (${toBaseType(field)(fieldNameSymbol)} != ${defaultValueForGet(field, true)}) { __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol))} }")
    } else if (field.isOptional) {
      fp.add(s"if ($fieldNameSymbol.isDefined) { __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol + ".get"))} }")
    } else if (field.isRepeated) {
      val tagSize = CodedOutputStream.computeTagSize(field.getNumber)
      if (!field.isPacked)
        Types.fixedSize(field.getType) match {
          case Some(size) => fp.add(s"__size += ${size + tagSize} * $fieldNameSymbol.size")
          case None => fp.add(
            s"$fieldNameSymbol.foreach($fieldNameSymbol => __size += ${sizeExpressionForSingleField(field, toBaseType(field)(fieldNameSymbol))})")
        }
      else {
        val fieldName = field.scalaName
        fp
          .addM(
            s"""if($fieldNameSymbol.nonEmpty) {
               |  __size += $tagSize + com.google.protobuf.CodedOutputStream.computeRawVarint32Size(${fieldName}SerializedSize) + ${fieldName}SerializedSize
               |}""")
      }
    } else throw new RuntimeException("Should not reach here.")
  }

  def generateSerializedSize(message: Descriptor)(fp: FunctionalPrinter) = {
    fp
      .add("lazy val serializedSize: Int = {")
      .indent
      .add("var __size = 0")
      .print(message.getFields)(generateSerializedSizeForField)
      .add("__size")
      .outdent
      .add("}")
  }

  def generateSerializedSizeForPackedFields(message: Descriptor)(fp: FunctionalPrinter) =
    fp
      .print(message.getFields.filter(_.isPacked).zipWithIndex) {
      case ((field, index), printer) =>
        printer
          .add(s"lazy val ${field.scalaName}SerializedSize =")
          .call({ fp =>
          Types.fixedSize(field.getType) match {
            case Some(size) =>
              fp.add(s"  $size * ${field.scalaName.asSymbol}.size")
            case None =>
              val capTypeName = Types.capitalizedType(field.getType)
              val sizeFunc = Seq(s"com.google.protobuf.CodedOutputStream.compute${capTypeName}SizeNoTag")
              val fromEnum = if (field.isEnum) Seq(s"(_: ${field.baseSingleScalaTypeName}).id") else Nil
              val fromCustom = if (field.customSingleScalaTypeName.isDefined)
                Seq(s"${field.typeMapper}.toBase")
              else Nil
              val funcs = sizeFunc ++ fromEnum ++ fromCustom
              fp.add(s"  ${field.scalaName.asSymbol}.map(${composeGen(funcs)}).sum")
          }
        })
    }

  private def composeGen(funcs: Seq[String]) =
    if (funcs.length == 1) funcs(0)
    else s"(${funcs(0)} _)" + funcs.tail.map(func => s".compose($func)").mkString

  def generateWriteTo(message: Descriptor)(fp: FunctionalPrinter) =
    fp.add(s"def writeTo(output: com.google.protobuf.CodedOutputStream): Unit = {")
      .indent
      .print(message.getFields.sortBy(_.getNumber).zipWithIndex) {
      case ((field, index), printer) =>
        val fieldNameSymbol = fieldAccessorSymbol(field)
        val capTypeName = Types.capitalizedType(field.getType)
        if (field.isPacked) {
          val writeFunc = composeGen(Seq(
            s"output.write${capTypeName}NoTag") ++ (
            if (field.isEnum) Seq(s"(_: ${field.baseSingleScalaTypeName}).id") else Nil
            ) ++ (
            if (field.customSingleScalaTypeName.isDefined)
              Seq(s"${field.typeMapper}.toBase")
            else Nil
            ))

            printer.addM(
              s"""if (${fieldNameSymbol}.nonEmpty) {
                 |  output.writeTag(${field.getNumber}, 2)
                 |  output.writeRawVarint32(${fieldNameSymbol}SerializedSize)
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
            .add(s"${fieldNameSymbol}.foreach { __v => ")
            .indent
            .call(generateWriteSingleValue(field, toBaseType(field)("__v")))
            .outdent
            .add("};")
        }
    }
      .outdent
      .add("}")

  def printConstructorFieldList(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val regularFields = message.getFields.collect {
      case field if !field.isInOneof =>
      val typeName = field.scalaTypeName
      val ctorDefaultValue =
        if (field.isOptional && field.supportsPresence) " = None"
        else if (field.isSingular) " = " + defaultValueForGet(field)
        else if (field.isMap) " = Map.empty"
        else if (field.isRepeated) " = Nil"
        else ""
        s"${field.scalaName.asSymbol}: $typeName$ctorDefaultValue"
    }
    val oneOfFields = message.getOneofs.map {
      oneOf =>
        s"${oneOf.scalaName.asSymbol}: ${oneOf.scalaTypeName} = ${oneOf.empty}"
    }
    printer.addWithDelimiter(",")(regularFields ++ oneOfFields)
  }

  def generateMergeFrom(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer.add(
      s"def mergeFrom(__input: com.google.protobuf.CodedInputStream): $myFullScalaName = {")
      .indent
      .print(message.fieldsWithoutOneofs)((field, printer) =>
        if (!field.isRepeated)
          printer.add(s"var __${field.scalaName} = this.${field.scalaName.asSymbol}")
        else if (field.isMap)
          printer.add(s"val __${field.scalaName} = (scala.collection.immutable.Map.newBuilder[${field.mapType.keyType}, ${field.mapType.valueType}] ++= this.${field.scalaName.asSymbol})")
        else
          printer.add(s"val __${field.scalaName} = (scala.collection.immutable.Vector.newBuilder[${field.singleScalaTypeName}] ++= this.${field.scalaName.asSymbol})")
      )
      .print(message.getOneofs)((oneof, printer) =>
      printer.add(s"var __${oneof.scalaName} = this.${oneof.scalaName.asSymbol}")
      )
      .addM(
        s"""var _done__ = false
           |while (!_done__) {
           |  val _tag__ = __input.readTag()
           |  _tag__ match {
           |    case 0 => _done__ = true""")
      .print(message.getFields) {
      (field, printer) =>
        if (!field.isPacked) {
          val newValBase = if (field.getJavaType == JavaType.MESSAGE) {
            val defInstance = s"${field.getMessageType.scalaTypeName}.defaultInstance"
            val baseInstance = if (field.supportsPresence) {
              val expr = s"__${field.scalaName}"
              s"${mapToBaseType(field)(expr)}.getOrElse($defInstance)"
            } else if (field.isInOneof) {
              s"${mapToBaseType(field)(fieldAccessorSymbol(field))}.getOrElse($defInstance)"
            } else if (field.isRepeated) {
              defInstance
            } else {
              toBaseType(field)(s"__${field.scalaName}")
            }
            s"com.trueaccord.scalapb.LiteParser.readMessage(__input, $baseInstance)"
          } else if (field.isEnum)
            s"${field.getEnumType.scalaTypeName}.fromValue(__input.readEnum())"
          else s"__input.read${Types.capitalizedType(field.getType)}()"

          val newVal = toCustomType(field)(newValBase)

          val updateOp =
            if (field.supportsPresence) s"__${field.scalaName} = Some($newVal)"
            else if (field.isInOneof) {
              s"__${field.getContainingOneof.scalaName} = ${field.oneOfTypeName}($newVal)"
            }
            else if (field.isRepeated) s"__${field.scalaName} += $newVal"
            else s"__${field.scalaName} = $newVal"
          printer.addM(
            s"""    case ${(field.getNumber << 3) + Types.wireType(field.getType)} =>
               |      $updateOp""")
        } else {
          val read = {
            val tmp = s"""__input.read${Types.capitalizedType(field.getType)}"""
            if (field.isEnum)
              s"${field.getEnumType.scalaTypeName}.fromValue($tmp)"
            else tmp
          }
          val readExpr = toCustomType(field)(read)
          printer.addM(
            s"""    case ${(field.getNumber << 3) + Types.WIRETYPE_LENGTH_DELIMITED} => {
               |      val length = __input.readRawVarint32()
               |      val oldLimit = __input.pushLimit(length)
               |      while (__input.getBytesUntilLimit > 0) {
               |        __${field.scalaName} += $readExpr
               |      }
               |      __input.popLimit(oldLimit)
               |    }""")
          }
    }
      .addM(
       s"""|    case tag => __input.skipField(tag)
           |  }
           |}""")
      .add(s"$myFullScalaName(")
      .indent.addWithDelimiter(",")(
        (message.fieldsWithoutOneofs ++ message.getOneofs).map {
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
      .print(message.getFields) {
      case (field, printer) =>
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
        val normal = message.getFields.collect {
          case field if !field.isInOneof =>
            val conversion = if (field.isMap) javaMapFieldToScala("javaPbSource", field)
            else javaFieldToScala("javaPbSource", field)
            Seq(s"${field.scalaName.asSymbol} = $conversion")
        }
        val oneOfs = message.getOneofs.map {
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
    val myFullScalaName = message.scalaTypeName
    printer.add(s"def fromFieldsMap(fieldsMap: Map[Int, Any]): $myFullScalaName = $myFullScalaName(")
      .indent
      .call {
      printer =>
        val fields = message.getFields.collect {
          case field if !field.isInOneof =>
            val baseTypeName = field.baseScalaTypeName
            val baseMapGetter = if (field.isOptional)
              s"fieldsMap.getOrElse(${field.getNumber}, None).asInstanceOf[$baseTypeName]"
            else if (field.isRepeated && field.isMap)
              s"fieldsMap.getOrElse(${field.getNumber}, Map.empty).asInstanceOf[${field.mapType.scalaTypeName}]"
            else if (field.isRepeated)
              s"fieldsMap.getOrElse(${field.getNumber}, Nil).asInstanceOf[$baseTypeName]"
            else
              s"fieldsMap(${field.getNumber}).asInstanceOf[$baseTypeName]"
            if (field.customSingleScalaTypeName.isEmpty || field.isMap)
              s"${field.scalaName.asSymbol} = $baseMapGetter"
            else if (field.isRequired)
              s"${field.scalaName.asSymbol} = ${toCustomType(field)(baseMapGetter)}"
            else
              s"${field.scalaName.asSymbol} = ${mapToCustomType(field)(baseMapGetter)}"
        }
        val oneOfs = message.getOneofs.map {
          oneOf =>
            val elems = oneOf.fields.map {
              field =>
                val typeName = field.scalaTypeName
                val t = field.oneOfTypeName
                s"fieldsMap.getOrElse(${field.getNumber}, None).asInstanceOf[Option[$typeName]].map(value => $t(value))"
            } mkString (" orElse\n")
            s"${oneOf.scalaName.asSymbol} = $elems getOrElse ${oneOf.empty}"
        }
        printer.addWithDelimiter(",")(fields ++ oneOfs)
    }
      .outdent
      .add(")")
  }

  def generateFromAscii(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    printer.addM(
      s"""override def fromAscii(ascii: String): ${message.scalaTypeName} = {
         |  val javaProtoBuilder = ${message.javaTypeName}.newBuilder
         |  com.google.protobuf.TextFormat.merge(ascii, javaProtoBuilder)
         |  fromJavaProto(javaProtoBuilder.build)
         |}""")
  }

  def generateDescriptor(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer.addM(
      s"""lazy val descriptor = new Descriptors.MessageDescriptor("${message.getName}", this,
         |  None, m = Seq(${message.nestedTypes.map(m => m.scalaTypeName + ".descriptor").mkString(", ")}),
         |  e = Seq(${message.getEnumTypes.map(m => m.scalaTypeName + ".descriptor").mkString(", ")}),
         |  f = ${message.getFile.scalaPackageName}.${message.getFile.internalFieldsObjectName}.internalFieldsFor("${myFullScalaName}"))""")
  }

  def generateDefaultInstance(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    printer
      .add(s"lazy val defaultInstance = $myFullScalaName(")
      .indent
      .addWithDelimiter(",")(message.getFields.collect {
      case field if field.isRequired =>
        val default = defaultValueForDefaultInstance(field)
        s"${field.scalaName.asSymbol} = $default"
    })
      .outdent
      .add(")")
  }

  def generateMessageLens(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    val className = message.getName
    val classNameSymbol = className.asSymbol
    def lensType(s: String) = s"com.trueaccord.lenses.Lens[UpperPB, $s]"

    printer.add(
      s"implicit class ${className}Lens[UpperPB](_l: com.trueaccord.lenses.Lens[UpperPB, $classNameSymbol]) extends com.trueaccord.lenses.ObjectLens[UpperPB, $classNameSymbol](_l) {")
      .indent
      .print(message.getFields) {
      case (field, printer) =>
        val fieldName = field.scalaName.asSymbol
        if (!field.isInOneof) {
          if (field.supportsPresence) {
            val optionLensName = "optional" + field.upperScalaName
            printer
              .addM(
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
      .print(message.getOneofs) {
      case (oneof, printer) =>
        val oneofName = oneof.scalaName.asSymbol
        printer
          .add(s"def $oneofName: ${lensType(oneof.scalaTypeName)} = field(_.$oneofName)((c_, f_) => c_.copy($oneofName = f_))")
    }
      .outdent
      .add("}")
  }

  def generateFieldNumbers(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .print(message.getFields) {
      case (field, printer) =>
        val fieldName = field.scalaName.asSymbol
        printer.add(s"final val ${field.fieldNumberConstantName} = ${field.getNumber}")
    }
  }

  def generateTypeMappers(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val customizedFields: Seq[(FieldDescriptor, String)] = for {
      field <- message.getFields
      custom <- field.customSingleScalaTypeName
    } yield (field, custom)

    printer
      .print(customizedFields) {
      case ((field, customType), printer) =>
        val fieldName = field.scalaName.asSymbol
        printer.add(s"private val ${field.typeMapperValName}: com.trueaccord.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}] = implicitly[com.trueaccord.scalapb.TypeMapper[${field.baseSingleScalaTypeName}, ${customType}]]")
    }
  }

  def generateTypeMappersForMapEntry(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val pairToMessage = if (message.mapType.valueField.supportsPresence)
      s"__p => ${message.scalaTypeName}(__p._1, Some(__p._2))"
    else
      s"__p => ${message.scalaTypeName}(__p._1, __p._2)"

    val messageToPair = if (message.mapType.valueField.supportsPresence)
      s"__m => (__m.key, __m.getValue)"
    else
      s"__m => (__m.key, __m.value)"

    printer
      .addM(
        s"""implicit val keyValueMapper: com.trueaccord.scalapb.TypeMapper[${message.scalaTypeName}, ${message.mapType.pairType}] =
           |  com.trueaccord.scalapb.TypeMapper[${message.scalaTypeName}, ${message.mapType.pairType}]($messageToPair)($pairToMessage)"""
      )
  }

  def generateMessageCompanion(message: Descriptor)(printer: FunctionalPrinter): FunctionalPrinter = {
    val myFullScalaName = message.scalaTypeName
    val className = message.getName.asSymbol
    val mixins = if (message.javaConversions)
      s"with com.trueaccord.scalapb.JavaProtoSupport[$className, ${message.javaTypeName}] " else ""
    val companionType = s"com.trueaccord.scalapb.GeneratedMessageCompanion[$className] $mixins"
    printer.addM(
      s"""object $className extends $companionType {
         |  implicit def messageCompanion: $companionType = this""")
      .indent
      .when(message.javaConversions)(generateToJavaProto(message))
      .when(message.javaConversions)(generateFromJavaProto(message))
      .when(message.javaConversions)(generateFromAscii(message))
      .call(generateFromFieldsMap(message))
      .call(generateDescriptor(message))
      .call(generateDefaultInstance(message))
      .print(message.getEnumTypes)(printEnum)
      .print(message.getOneofs)(printOneof)
      .print(message.nestedTypes)(printMessage)
      .call(generateMessageLens(message))
      .call(generateFieldNumbers(message))
      .when(!message.isMapEntry)(generateTypeMappers(message))
      .when(message.isMapEntry)(generateTypeMappersForMapEntry(message))
      .outdent
      .add("}")
      .add("")
  }

  def printMessage(message: Descriptor,
                   printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(s"final case class ${message.nameSymbol}(")
      .indent
      .indent
      .call(printConstructorFieldList(message))
      .add(s") extends ${message.baseClasses.mkString(" with ")} {")
      .call(generateSerializedSizeForPackedFields(message))
      .call(generateSerializedSize(message))
      .call(generateWriteTo(message))
      .call(generateMergeFrom(message))
      .print(message.getFields) {
      case (field, printer) =>
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
            p.addM(
              s"""def $clearMethod: ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = None)
                 |def $withMethod(__v: ${singleType}): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = Some(__v))""")
        }.when(field.isInOneof) {
          p =>
            val default = defaultValueForGet(field)
            p.add(
              s"""def $withMethod(__v: ${singleType}): ${message.nameSymbol} = copy(${field.getContainingOneof.scalaName.asSymbol} = ${field.oneOfTypeName}(__v))""")
        }.when(field.isRepeated) { p =>
          val emptyValue = if (field.isMap) "Map.empty" else "Seq.empty"
          p.addM(
            s"""def $clearMethod = copy(${field.scalaName.asSymbol} = $emptyValue)
               |def add${field.upperScalaName}(__vs: $singleType*): ${message.nameSymbol} = addAll${field.upperScalaName}(__vs)
               |def addAll${field.upperScalaName}(__vs: TraversableOnce[$singleType]): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = ${field.scalaName.asSymbol} ++ __vs)""")
        }.when(field.isRepeated || field.isSingular) {
          _
            .add(s"def $withMethod(__v: ${field.scalaTypeName}): ${message.nameSymbol} = copy(${field.scalaName.asSymbol} = __v)")
        }
    }.print(message.getOneofs) {
      case (oneof, printer) =>
        printer.addM(
          s"""def clear${oneof.upperScalaName}: ${message.nameSymbol} = copy(${oneof.scalaName.asSymbol} = ${oneof.empty})
             |def with${oneof.upperScalaName}(__v: ${oneof.scalaTypeName}): ${message.nameSymbol} = copy(${oneof.scalaName.asSymbol} = __v)""")
    }
      .call(generateGetField(message))
      .when(message.javaConversions)(
        _.add(s"override def toString: String = com.google.protobuf.TextFormat.printToString(${message.scalaTypeName}.toJavaProto(this))"))
      .add(s"def companion = ${message.scalaTypeName}")
      .outdent
      .outdent
      .addM(s"""}
            |""")
      .call(generateMessageCompanion(message))
  }

  def generateInternalFields(message: Descriptor, fp: FunctionalPrinter): FunctionalPrinter = {
    def makeDescriptor(field: FieldDescriptor): String = {
      val index = field.getIndex
      val label = if (field.isOptional) "Descriptors.Optional"
      else if (field.isRepeated) "Descriptors.Repeated"
      else if (field.isRequired) "Descriptors.Required"
      else throw new IllegalArgumentException()

      val t = field.getJavaType match {
        case t if !field.isMessage && !field.isEnum =>
          s"Descriptors.PrimitiveType(com.google.protobuf.Descriptors.FieldDescriptor.JavaType.$t, " +
          s"com.google.protobuf.Descriptors.FieldDescriptor.Type.${field.getType})"
        case _ if field.isMessage => "Descriptors.MessageType(" + field.getMessageType.scalaTypeName + ".descriptor)"
        case _ if field.isEnum => "Descriptors.EnumType(" + field.getEnumType.scalaTypeName + ".descriptor)"
      }
      val oneof = field.containingOneOf.map(s => s"""Some("${s.getName()}")""").getOrElse("None")
      s"""Descriptors.FieldDescriptor($index, ${field.getNumber}, "${field.getName}", $label, $t, isPacked = ${field.isPacked}, containingOneofName = $oneof)"""
    }

    fp.add(s"""case "${message.scalaTypeName}" => Seq(${message.getFields.filterNot(_.isMap).map(makeDescriptor).mkString(", ")})""")
      .print(message.nestedTypes)(generateInternalFields)
  }

  def generateInternalFieldsFor(file: FileDescriptor)(fp: FunctionalPrinter): FunctionalPrinter =
    if (file.getMessageTypes.nonEmpty) {
      fp.add("def internalFieldsFor(scalaName: String): Seq[Descriptors.FieldDescriptor] = scalaName match {")
        .indent
        .print(file.getMessageTypes)(generateInternalFields)
        .outdent
        .add("}")
    } else fp

  def scalaFileHeader(file: FileDescriptor): FunctionalPrinter = {
    new FunctionalPrinter().addM(
      s"""// Generated by the Scala Plugin for the Protocol Buffer Compiler.
         |// Do not edit!
         |//
         |// Protofile syntax: ${file.getSyntax.toString}
         |
         |${if (file.scalaPackageName.nonEmpty) ("package " + file.scalaPackageName) else ""}
         |
         |${if (params.javaConversions) "import scala.collection.JavaConversions._" else ""}
         |import com.trueaccord.scalapb.Descriptors
         |""")
    .print(file.scalaOptions.getImportList) {
      case (i, printer) => printer.add(s"import $i")
    }
  }

  def generateScalaFilesForFileDescriptor(file: FileDescriptor): Seq[CodeGeneratorResponse.File] = {
    val enumFiles = for {
      enum <- file.getEnumTypes
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaPackageName.replace('.', '/') + "/" + enum.getName + ".scala")
      b.setContent(
        scalaFileHeader(file)
          .call(printEnum(enum, _)).result())
      b.build
    }

    val messageFiles = for {
      message <- file.getMessageTypes
    } yield {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaPackageName.replace('.', '/') + "/" + message.getName + ".scala")
      b.setContent(
        scalaFileHeader(file)
          .call(printMessage(message, _)).result())
      b.build
    }

    val internalFieldsFile = {
      val b = CodeGeneratorResponse.File.newBuilder()
      b.setName(file.scalaPackageName.replace('.', '/') + s"/${file.internalFieldsObjectName}.scala")
      b.setContent(
        scalaFileHeader(file)
          .add(s"object ${file.internalFieldsObjectName} {")
          .indent
          .call(generateInternalFieldsFor(file))
          .outdent
          .add("}").result())
      b.build
    }

    enumFiles ++ messageFiles :+ internalFieldsFile
  }
}

object ProtobufGenerator {
  private def parseParameters(params: String): Either[String, GeneratorParams] = {
    params.split(",").map(_.trim).filter(_.nonEmpty).foldLeft[Either[String, GeneratorParams]](Right(GeneratorParams())) {
      case (Right(params), "java_conversions") => Right(params.copy(javaConversions = true))
      case (Right(params), "flat_package") => Right(params.copy(flatPackage = true))
      case (Right(params), p) => Left(s"Unrecognized parameter: '$p'")
      case (x, _) => x
    }
  }

  def handleCodeGeneratorRequest(request: CodeGeneratorRequest): CodeGeneratorResponse = {
    val b = CodeGeneratorResponse.newBuilder
    parseParameters(request.getParameter) match {
      case Right(params) =>
        val generator = new ProtobufGenerator(params)
        val fileProtosByName = request.getProtoFileList.map(n => n.getName -> n).toMap
        val filesByName: Map[String, FileDescriptor] =
          request.getProtoFileList.foldLeft[Map[String, FileDescriptor]](Map.empty) {
            case (acc, fp) =>
              val deps = fp.getDependencyList.map(acc)
              acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
          }
        request.getFileToGenerateList.foreach {
          name =>
            val file = filesByName(name)
            val responseFiles = generator.generateScalaFilesForFileDescriptor(file)
            b.addAllFile(responseFiles)
        }
      case Left(error) =>
        b.setError(error)
    }
    b.build
  }
}

