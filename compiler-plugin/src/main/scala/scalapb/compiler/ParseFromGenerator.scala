package scalapb.compiler

import com.google.protobuf.Descriptors._
import scala.jdk.CollectionConverters._
import com.google.protobuf.Descriptors.FieldDescriptor.Type

private[compiler] class ParseFromGenerator(
    implicits: DescriptorImplicits,
    generator: ProtobufGenerator,
    message: Descriptor
) {
  import implicits._
  import DescriptorImplicits.AsSymbolExtension
  import generator.{
    toBaseTypeExpr,
    toCustomTypeExpr,
    defaultValueForGet,
    defaultValueForDefaultInstance,
    toBaseFieldType,
    toCustomType
  }
  case class Field(
      name: String,
      targetName: String,
      typeName: String,
      default: String,
      accessor: String,
      builder: String,
      isRepeated: Boolean
  )

  val fields = message.fieldsWithoutOneofs.map { field =>
    if (usesBaseTypeInBuilder(field)) {
      // To handle custom types that have no default values, we wrap required/no-boxed messages in
      // Option during parsing. We also apply the type mapper after parsing is complete.
      if (field.isMessage)
        Field(
          s"__${field.scalaName}",
          field.scalaName.asSymbol,
          s"_root_.scala.Option[${field.baseSingleScalaTypeName}]",
          C.None,
          s"_root_.scala.Some(${toBaseTypeExpr(field)(s"_message__.${field.scalaName.asSymbol}", EnclosingType.None)})",
          toCustomTypeExpr(field)(
            s"__${field.scalaName}.getOrElse(${field.getMessageType.scalaType.fullName}.defaultInstance)",
            EnclosingType.None
          ),
          field.isRepeated
        )
      else
        Field(
          s"__${field.scalaName}",
          field.scalaName.asSymbol,
          field.baseSingleScalaTypeName,
          defaultValueForGet(field, uncustomized = true),
          toBaseTypeExpr(field)(s"_message__.${field.scalaName.asSymbol}", EnclosingType.None),
          toCustomTypeExpr(field)(s"__${field.scalaName}", EnclosingType.None),
          field.isRepeated
        )
    } else if (!field.isRepeated)
      Field(
        s"__${field.scalaName}",
        field.scalaName.asSymbol,
        field.scalaTypeName,
        defaultValueForDefaultInstance(field),
        s"_message__.${field.scalaName.asSymbol}",
        s"__${field.scalaName}",
        field.isRepeated
      )
    else {
      val it =
        if (field.collection.adapter.isDefined)
          field.collection.iterator(s"_message__.${field.scalaName.asSymbol}", EnclosingType.None)
        else s"_message__.${field.scalaName.asSymbol}"
      Field(
        s"__${field.scalaName}",
        field.scalaName.asSymbol,
        field.collection.builderType,
        field.collection.newBuilder,
        s"${field.collection.newBuilder} ++= $it",
        if (field.collection.adapter.isDefined)
          s"__${field.scalaName}.result().fold(throw _, identity(_))"
        else
          s"__${field.scalaName}.result()",
        field.isRepeated
      )
    }
  } ++ message.getRealOneofs.asScala.map { oneof =>
    Field(
      s"__${oneof.scalaName.name}",
      oneof.scalaName.name.asSymbol,
      oneof.scalaType.fullName,
      oneof.empty.fullName,
      s"_message__.${oneof.scalaName.nameSymbol}",
      s"__${oneof.scalaName.name}",
      isRepeated = false
    )
  } ++ (if (message.preservesUnknownFields)
          Seq(
            Field(
              "`_unknownFields__`",
              "unknownFields",
              "_root_.scalapb.UnknownFieldSet.Builder",
              "null",
              "new _root_.scalapb.UnknownFieldSet.Builder(_message__.unknownFields)",
              s"if (_unknownFields__ == null)${message.V.Then} _root_.scalapb.UnknownFieldSet.empty else _unknownFields__.result()",
              false
            )
          )
        else Seq.empty)

  private def usesBaseTypeInBuilder(field: FieldDescriptor) = field.isSingular

  private def isClosedEnum(field: FieldDescriptor): Boolean =
    field.isEnum && field.getEnumType.isClosed

  private def unknownValue(field: FieldDescriptor, method: String, value: String): String =
    if (message.preservesUnknownFields)
      s"""if (_unknownFields__ == null)${message.V.Then} {
         |  _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
         |}
         |_unknownFields__.$method(${field.getNumber}, $value)""".stripMargin
    else "()"

  private def readClosedEnum(field: FieldDescriptor, update: String): String = {
    val fieldNumber = if (message.preservesUnknownFields) s", ${field.getNumber}" else ""
    s"""val _enumValue__ = _readEnum__(${field.getEnumType.scalaType.fullName}$fieldNumber)
       |if (_enumValue__ != null)${message.V.Then} {
       |  ${update.replace("\n", "\n  ")}
       |}""".stripMargin
  }

  val requiredFieldMap: Map[FieldDescriptor, Int] =
    message.fields.filter(fd => fd.isRequired || fd.noBoxRequired).zipWithIndex.toMap

  val myFullScalaName = message.scalaType.fullName

  def generateParseFrom(printer: FunctionalPrinter): FunctionalPrinter = {
    printer
      .add(
        s"def parseFrom(`_input__`: _root_.com.google.protobuf.CodedInputStream): $myFullScalaName = {"
      )
      .indented(
        _.when(requiredFieldMap.nonEmpty) { fp =>
          // Sets the bit 0...(n-1) inclusive to 1.
          def hexBits(n: Int): String = "0x%xL".format((0 to (n - 1)).map(i => (1L << i)).sum)
          val requiredFieldCount      = requiredFieldMap.size
          val fullWords               = (requiredFieldCount - 1) / 64
          val bits: Seq[String]       = (1 to fullWords).map(_ => hexBits(64)) :+ hexBits(
            requiredFieldCount - 64 * fullWords
          )
          fp.print(bits.zipWithIndex) { case (fp, (bn, index)) =>
            fp.add(s"var __requiredFields$index: _root_.scala.Long = $bn")
          }
        }
          .print(fields)((fp, f) =>
            fp.add(
              s"${if (f.isRepeated) "val" else "var"} ${f.name}: ${f.typeName} = ${f.default}"
            )
          )
          .when(message.fields.exists(isClosedEnum)) { fp =>
            val fieldNumber =
              if (message.preservesUnknownFields) ", fieldNumber: _root_.scala.Int" else ""
            fp.add(
              s"def _readEnum__[A <: _root_.scalapb.GeneratedEnum](companion: _root_.scalapb.GeneratedEnumCompanion[A]$fieldNumber): A = {",
              "  val value = companion.fromValue(_input__.readEnum())",
              s"  if (value.isUnrecognized)${message.V.Then} {"
            ).when(message.preservesUnknownFields)(
              _.add(
                s"    if (_unknownFields__ == null)${message.V.Then} {",
                "      _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()",
                "    }",
                "    _unknownFields__.addVarint(fieldNumber, value.value.toLong)"
              )
            ).add(
              // Null is internal to this helper: callers only update fields for recognized values.
              "    null.asInstanceOf[A]",
              "  } else value",
              "}"
            )
          }
          .add(s"""var _done__ = false
                  |while (!_done__)${message.V.Do} {
                  |  val _tag__ = _input__.readTag()
                  |  _tag__ match {
                  |    case 0 => _done__ = true""".stripMargin)
          .print(message.fields) { (printer, field) =>
            val p = {
              val newValBase = if (field.isMessage) {
                // In 0.10.x we can't simply call any of the new methods that relies on Builder,
                // since the references message may have been generated using an older version of
                // ScalaPB.
                val baseName = field.baseSingleScalaTypeName
                val read     =
                  if (field.isRepeated)
                    s"_root_.scalapb.LiteParser.readMessage[$baseName](_input__)"
                  else if (usesBaseTypeInBuilder(field)) {
                    s"_root_.scala.Some(__${field.scalaName}.fold(_root_.scalapb.LiteParser.readMessage[$baseName](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _)))"
                  } else {
                    val expr =
                      if (field.isInOneof)
                        s"__${field.getContainingOneof.scalaName.name}.${field.scalaName.asSymbol}"
                      else s"__${field.scalaName}"
                    val mappedType = toBaseFieldType(field).apply(expr, field.enclosingType)
                    if (field.isInOneof || field.supportsPresence)
                      s"$mappedType.fold(_root_.scalapb.LiteParser.readMessage[$baseName](_input__))(_root_.scalapb.LiteParser.readMessage(_input__, _))"
                    else s"_root_.scalapb.LiteParser.readMessage[$baseName](_input__, $mappedType)"
                  }
                read
              } else if (field.isEnum)
                s"${field.getEnumType.scalaType.fullName}.fromValue(_input__.readEnum())"
              else if (field.getType == Type.STRING) {
                // Use resolved features (including inherited Java overrides and map semantics).
                // Keep the legacy strict policy unless the schema explicitly opts in.
                if (
                  !field.getFile.scalaOptions.getUseSchemaUtf8Validation || field.needsUtf8Check()
                )
                  "_input__.readStringRequireUtf8()"
                else "_input__.readString()"
              } else s"_input__.read${Types.capitalizedType(field.getType)}()"

              val baseValue = if (isClosedEnum(field)) "_enumValue__" else newValBase
              val newVal    =
                if (!usesBaseTypeInBuilder(field)) toCustomType(field)(baseValue) else baseValue

              val updateOp =
                if (field.supportsPresence) s"__${field.scalaName} = _root_.scala.Option($newVal)"
                else if (field.isInOneof) {
                  s"__${field.getContainingOneof.scalaName.name} = ${field.oneOfTypeName.fullName}($newVal)"
                } else if (field.isRepeated) s"__${field.scalaName} += $newVal"
                else s"__${field.scalaName} = $newVal"

              val update = if (field.isRequired || field.noBoxRequired) {
                val fieldNumber = requiredFieldMap(field)
                s"$updateOp\n__requiredFields${fieldNumber / 64} &= 0x${"%x".format(~(1L << fieldNumber))}L"
              } else updateOp
              val body =
                if (isClosedEnum(field)) readClosedEnum(field, update)
                else if (field.isMapField && isClosedEnum(field.mapType.valueField)) {
                  val entryType   = field.getMessageType.scalaType.fullName
                  val enumType    = field.mapType.valueField.getEnumType.scalaType.fullName
                  val mappedEntry =
                    toCustomType(field)(s"$entryType.parseFrom(_entryBytes__.newCodedInput())")
                  val validateKeyUtf8 = field.mapType.keyField.needsUtf8Check()
                  s"""val _entryBytes__ = _input__.readBytes()
                     |if (_root_.scalapb.ClosedEnumMapEntry.isUnknown(_entryBytes__, $enumType, $validateKeyUtf8))${message.V.Then} {
                     |  ${unknownValue(field, "addLengthDelimited", "_entryBytes__")
                      .replace("\n", "\n  ")}
                     |} else {
                     |  __${field.scalaName} += $mappedEntry
                     |}""".stripMargin
                } else update
              printer
                .add(s"    case ${(field.getNumber << 3) + Types.wireType(field.getType)} =>")
                .add(body.split("\n").iterator.map("      " + _).toSeq: _*)
            }

            if (field.isPackable) {
              val read = {
                val tmp = s"""_input__.read${Types.capitalizedType(field.getType)}()"""
                if (field.isEnum)
                  s"${field.getEnumType.scalaType.fullName}.fromValue($tmp)"
                else tmp
              }
              val readExpr = toCustomType(field)(read)
              val update   =
                if (isClosedEnum(field))
                  readClosedEnum(
                    field,
                    s"__${field.scalaName} += ${toCustomType(field)("_enumValue__")}"
                  )
                else s"__${field.scalaName} += $readExpr"
              p.add(s"""    case ${(field.getNumber << 3) + Types.WIRETYPE_LENGTH_DELIMITED} => {
                       |      val length = _input__.readRawVarint32()
                       |      val oldLimit = _input__.pushLimit(length)
                       |      while (_input__.getBytesUntilLimit > 0)${message.V.Do} {
                       |        ${update.replace("\n", "\n        ")}
                       |      }
                       |      _input__.popLimit(oldLimit)
                       |    }""".stripMargin)
            } else p
          }
          .when(!message.preservesUnknownFields)(_.add("    case tag => _input__.skipField(tag)"))
          .when(message.preservesUnknownFields)(
            _.add(
              s"""    case tag =>
                 |      if (_unknownFields__ == null)${message.V.Then} {
                 |        _unknownFields__ = new _root_.scalapb.UnknownFieldSet.Builder()
                 |      }
                 |      val _ = _unknownFields__.parseField(tag, _input__)""".stripMargin
            )
          )
          .add("  }")
          .add("}")
          .when(requiredFieldMap.nonEmpty) { p =>
            val r = (0 until (requiredFieldMap.size + 63) / 64)
              .map(i => s"__requiredFields$i != 0L")
              .mkString(" || ")
            p.add(
              s"""if (${r})${message.V.Then} { throw new _root_.com.google.protobuf.InvalidProtocolBufferException("Message missing required fields.") } """
            )
          }
          .add(s"$myFullScalaName(")
          .indented(
            _.addWithDelimiter(",")(fields.map(e => s"  ${e.targetName} = ${e.builder}"))
          )
          .add(")")
      )
      .add("}")
  }
}

private[compiler] object ParseFromGenerator {
  def generateParseFrom(implicits: DescriptorImplicits, pb: ProtobufGenerator, message: Descriptor)(
      fp: FunctionalPrinter
  ): FunctionalPrinter =
    new ParseFromGenerator(implicits, pb, message).generateParseFrom(fp)
}
