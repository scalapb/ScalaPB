package scalapb.textformat

import com.google.protobuf.{CodedInputStream, CodedOutputStream}
import com.google.protobuf.descriptor.FieldDescriptorProto
import com.google.protobuf.descriptor.FieldDescriptorProto.Type
import scalapb.UnknownFieldSet
import scalapb.descriptors._
import AstUtils.AstError

/** Text codecs operate on wire values, independently of generated extension lenses/custom types. */
private[textformat] case class ScalarExtension(
    name: String,
    field: FieldDescriptorProto,
    enumDescriptor: Option[EnumDescriptor],
    verifyUtf8: Boolean
) {
  private val number   = field.getNumber
  private val repeated = field.getLabel.isLabelRepeated
  private val packable = !field.getType.isTypeString && !field.getType.isTypeBytes
  private val wireType = field.getType match {
    case Type.TYPE_DOUBLE | Type.TYPE_FIXED64 | Type.TYPE_SFIXED64 => 1
    case Type.TYPE_STRING | Type.TYPE_BYTES                        => 2
    case Type.TYPE_FLOAT | Type.TYPE_FIXED32 | Type.TYPE_SFIXED32  => 5
    case _                                                         => 0
  }

  def parse(input: TField): Either[AstError, Array[Byte]] = {
    val values = input.value match {
      case TArray(_, values) if repeated => values
      case value                         => Seq(value)
    }
    val parsed = values.foldLeft[Either[AstError, Vector[PValue]]](Right(Vector.empty)) {
      case (acc, primitive: TPrimitive) =>
        for {
          previous <- acc
          value    <- AstUtils.parsePrimitive(field.getType, enumDescriptor, primitive)
        } yield previous :+ value
      case (_, value) => Left(AstError(value.position, "Expected scalar extension value"))
    }
    parsed.map { values =>
      val bytes  = new java.io.ByteArrayOutputStream()
      val output = CodedOutputStream.newInstance(bytes)
      // Unpacked encoding is legal for repeated packable extensions regardless of their preference.
      values.foreach(ParsedValue.writePrimitive(number, field.getType, _, output))
      output.flush()
      bytes.toByteArray
    }
  }

  /** Returns interpreted values and leaves unrelated wire types/unknown closed enum values intact.
    */
  def read(fieldValue: UnknownFieldSet.Field): (Vector[PValue], UnknownFieldSet.Field) = {
    val bytes  = new java.io.ByteArrayOutputStream()
    val output = CodedOutputStream.newInstance(bytes)
    fieldValue.writeTo(number, output)
    output.flush()
    val input     = CodedInputStream.newInstance(bytes.toByteArray)
    val values    = Vector.newBuilder[PValue]
    val remaining = new UnknownFieldSet.Builder

    def readValue(): Unit = {
      if (field.getType.isTypeEnum) {
        val raw        = input.readInt64()
        val descriptor = enumDescriptor.get
        if (descriptor.isClosed) descriptor.findValueByNumber(raw.toInt) match {
          case Some(value) => values += PEnum(value)
          case None        => remaining.addVarint(number, raw)
        }
        else values += PEnum(descriptor.findValueByNumberCreatingIfUnknown(raw.toInt))
      } else values += readPrimitive(input)
    }

    var tag = input.readTag()
    while (tag != 0) {
      if ((tag & 7) == wireType) readValue()
      else if (repeated && packable && (tag & 7) == 2) {
        val limit = input.pushLimit(input.readRawVarint32())
        while (input.getBytesUntilLimit > 0) readValue()
        input.popLimit(limit)
      } else remaining.parseField(tag, input)
      tag = input.readTag()
    }
    val result = values.result()
    (
      if (repeated) result else result.takeRight(1),
      remaining.result().getField(number).getOrElse(UnknownFieldSet.Field())
    )
  }

  private def readPrimitive(input: CodedInputStream): PValue = field.getType match {
    case Type.TYPE_DOUBLE  => PDouble(input.readDouble())
    case Type.TYPE_FLOAT   => PFloat(input.readFloat())
    case Type.TYPE_INT64   => PLong(input.readInt64())
    case Type.TYPE_UINT64  => PLong(input.readUInt64())
    case Type.TYPE_INT32   => PInt(input.readInt32())
    case Type.TYPE_FIXED64 => PLong(input.readFixed64())
    case Type.TYPE_FIXED32 => PInt(input.readFixed32())
    case Type.TYPE_BOOL    => PBoolean(input.readBool())
    case Type.TYPE_STRING  =>
      PString(if (verifyUtf8) input.readStringRequireUtf8() else input.readString())
    case Type.TYPE_BYTES    => PByteString(input.readBytes())
    case Type.TYPE_UINT32   => PInt(input.readUInt32())
    case Type.TYPE_SFIXED32 => PInt(input.readSFixed32())
    case Type.TYPE_SFIXED64 => PLong(input.readSFixed64())
    case Type.TYPE_SINT32   => PInt(input.readSInt32())
    case Type.TYPE_SINT64   => PLong(input.readSInt64())
    case other => throw new IllegalArgumentException(s"Not a scalar extension type: $other")
  }
}

private[textformat] object ScalarExtension {
  def fromDeclaration(declaration: ExtensionDeclaration): Option[ScalarExtension] = {
    val field = declaration.field
    if (field.getType.isTypeMessage || field.getType.isTypeGroup) None
    else {
      val enumDescriptor =
        if (field.getType.isTypeEnum)
          declaration.file.findSymbol(field.getTypeName.stripPrefix(".")) match {
            case Some(descriptor: EnumDescriptor) => Some(descriptor)
            case _                                =>
              throw new DescriptorValidationException(
                declaration.file,
                s"Unknown enum type for extension ${declaration.name}: ${field.getTypeName}"
              )
          }
        else None
      Some(
        ScalarExtension(
          declaration.name,
          field,
          enumDescriptor,
          declaration.features.getUtf8Validation.isVerify
        )
      )
    }
  }
}
