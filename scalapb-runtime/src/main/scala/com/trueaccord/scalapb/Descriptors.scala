package com.trueaccord.scalapb

import com.google.protobuf.CodedInputStream
import com.google.protobuf.Descriptors.FieldDescriptor.{JavaType, Type}

object Descriptors {

  object WireType {
    val WIRETYPE_VARINT = 0
    val WIRETYPE_FIXED64 = 1
    val WIRETYPE_LENGTH_DELIMITED = 2
    val WIRETYPE_START_GROUP = 3
    val WIRETYPE_END_GROUP = 4
    val WIRETYPE_FIXED32 = 5

    def fromType(fieldType: Type) = fieldType match {
      case Type.DOUBLE => WIRETYPE_FIXED64
      case Type.FLOAT => WIRETYPE_FIXED32
      case Type.INT64 => WIRETYPE_VARINT
      case Type.UINT64 => WIRETYPE_VARINT
      case Type.INT32 => WIRETYPE_VARINT
      case Type.FIXED64 => WIRETYPE_FIXED64
      case Type.FIXED32 => WIRETYPE_FIXED32
      case Type.BOOL => WIRETYPE_VARINT
      case Type.STRING => WIRETYPE_LENGTH_DELIMITED
      case Type.GROUP => WIRETYPE_START_GROUP
      case Type.MESSAGE => WIRETYPE_LENGTH_DELIMITED
      case Type.BYTES => WIRETYPE_LENGTH_DELIMITED
      case Type.UINT32 => WIRETYPE_VARINT
      case Type.ENUM => WIRETYPE_VARINT
      case Type.SFIXED32 => WIRETYPE_FIXED32
      case Type.SFIXED64 => WIRETYPE_FIXED64
      case Type.SINT32 => WIRETYPE_VARINT
      case Type.SINT64 => WIRETYPE_VARINT
    }

    def read(ci: CodedInputStream, fieldType: Type): Any = fieldType match {
      case Type.DOUBLE => ci.readDouble()
      case Type.FLOAT => ci.readFloat()
      case Type.INT64 => ci.readInt64()
      case Type.UINT64 => ci.readUInt64()
      case Type.INT32 => ci.readInt32()
      case Type.FIXED64 => ci.readFixed64()
      case Type.FIXED32 => ci.readFixed32()
      case Type.BOOL => ci.readBool()
      case Type.STRING => ci.readString()
      case Type.GROUP => throw new IllegalArgumentException("Unsupported: group")
      case Type.MESSAGE => throw new IllegalArgumentException("Unsupported: message")
      case Type.BYTES => ci.readBytes()
      case Type.UINT32 => ci.readUInt32()
      case Type.ENUM => ci.readEnum()
      case Type.SFIXED32 => ci.readSFixed32()
      case Type.SFIXED64 => ci.readSFixed64()
      case Type.SINT32 => ci.readSInt32()
      case Type.SINT64 => ci.readSInt64()
    }
  }

  class FileDescriptor(m: => Seq[MessageDescriptor], e: => Seq[EnumDescriptor]) {
    lazy val messages = m
    lazy val enums = e
  }

  class EnumDescriptor(val index: Int, val name: String,
                       val companion: GeneratedEnumCompanion[_ <: GeneratedEnum])

  class MessageDescriptor(val name: String,
                          val companion: GeneratedMessageCompanion[_ <: GeneratedMessage],
                          c: => Option[MessageDescriptor],
                          m: => Seq[MessageDescriptor],
                          e: => Seq[EnumDescriptor],
                          f: => Seq[FieldDescriptor]) {
    lazy val container = c
    lazy val messages = m
    lazy val enums = e
    lazy val fields = f
  }

  sealed trait Label

  final case object Repeated extends Label

  final case object Required extends Label

  final case object Optional extends Label

  sealed trait FieldType {
    def fieldType: Type
  }

  case class PrimitiveType(javaType: JavaType, fieldType: Type) extends FieldType

  case class MessageType(m: MessageDescriptor) extends FieldType {
    def fieldType = Type.MESSAGE
  }

  case class EnumType(descriptor: EnumDescriptor) extends FieldType {
    def fieldType = Type.ENUM
  }

  case class FieldDescriptor(index: Int, number: Int, name: String, label: Label,
                             fieldType: FieldType, isPacked: Boolean = false,
                             containingOneofName: Option[String] = None)
}
