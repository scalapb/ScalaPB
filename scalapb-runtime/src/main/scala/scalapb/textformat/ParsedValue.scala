package scalapb.textformat

import com.google.protobuf.{ByteString, CodedOutputStream}
import com.google.protobuf.descriptor.FieldDescriptorProto.Type
import scalapb.descriptors._

/** Text-parser state that retains extensions without extending the public PValue hierarchy. */
private[textformat] object ParsedValue {
  sealed trait Value {
    def toPValue: PValue
    def hasExtensions: Boolean
  }

  final case class Scalar(toPValue: PValue) extends Value {
    def hasExtensions: Boolean = false
  }

  final case class Repeated(values: Vector[Value]) extends Value {
    def toPValue: PValue       = PRepeated(values.map(_.toPValue))
    def hasExtensions: Boolean = values.exists(_.hasExtensions)
  }

  final case class Message(
      fields: Map[FieldDescriptor, Value],
      extensions: Vector[Array[Byte]] = Vector.empty
  ) extends Value {
    def toPValue: PMessage     = PMessage(fields.map { case (fd, value) => fd -> value.toPValue })
    def hasExtensions: Boolean = extensions.nonEmpty || fields.values.exists(_.hasExtensions)

    def toByteArray: Array[Byte] = {
      val bytes  = new java.io.ByteArrayOutputStream()
      val output = CodedOutputStream.newInstance(bytes)
      writeTo(output)
      output.flush()
      bytes.toByteArray
    }

    private[ParsedValue] def writeTo(output: CodedOutputStream): Unit = {
      fields.toVector.sortBy(_._1.number).foreach { case (fd, value) =>
        writeField(fd, value, output)
      }
      extensions.foreach(output.writeRawBytes)
    }
  }

  // Unpacked repeated values are valid for both packed and unpacked fields. Keeping this writer
  // private to text parsing lets nested messages/maps retain extensions until binary construction.
  private def writeField(fd: FieldDescriptor, value: Value, output: CodedOutputStream): Unit = {
    val number = fd.number
    value match {
      case Repeated(values) => values.foreach(writeField(fd, _, output))
      case message: Message =>
        if (fd.messageEncoding.isDelimited) {
          output.writeTag(number, scalapb.WireType.WIRETYPE_START_GROUP)
          message.writeTo(output)
          output.writeTag(number, scalapb.WireType.WIRETYPE_END_GROUP)
        } else output.writeBytes(number, ByteString.copyFrom(message.toByteArray))
      case Scalar(primitive) =>
        writePrimitive(number, fd.protoType, primitive, output)
    }
  }

  private[textformat] def writePrimitive(
      number: Int,
      protoType: Type,
      primitive: PValue,
      output: CodedOutputStream
  ): Unit = {
    (protoType, primitive) match {
      case (Type.TYPE_DOUBLE, PDouble(v))    => output.writeDouble(number, v)
      case (Type.TYPE_FLOAT, PFloat(v))      => output.writeFloat(number, v)
      case (Type.TYPE_INT64, PLong(v))       => output.writeInt64(number, v)
      case (Type.TYPE_UINT64, PLong(v))      => output.writeUInt64(number, v)
      case (Type.TYPE_INT32, PInt(v))        => output.writeInt32(number, v)
      case (Type.TYPE_FIXED64, PLong(v))     => output.writeFixed64(number, v)
      case (Type.TYPE_FIXED32, PInt(v))      => output.writeFixed32(number, v)
      case (Type.TYPE_BOOL, PBoolean(v))     => output.writeBool(number, v)
      case (Type.TYPE_STRING, PString(v))    => output.writeString(number, v)
      case (Type.TYPE_BYTES, PByteString(v)) => output.writeBytes(number, v)
      case (Type.TYPE_UINT32, PInt(v))       => output.writeUInt32(number, v)
      case (Type.TYPE_SFIXED32, PInt(v))     => output.writeSFixed32(number, v)
      case (Type.TYPE_SFIXED64, PLong(v))    => output.writeSFixed64(number, v)
      case (Type.TYPE_SINT32, PInt(v))       => output.writeSInt32(number, v)
      case (Type.TYPE_SINT64, PLong(v))      => output.writeSInt64(number, v)
      case (Type.TYPE_ENUM, PEnum(v))        => output.writeEnum(number, v.number)
      case _ => throw new IllegalArgumentException(s"Invalid parsed value for ${number}")
    }
  }
}
