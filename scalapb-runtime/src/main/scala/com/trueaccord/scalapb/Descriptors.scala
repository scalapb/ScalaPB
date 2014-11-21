package com.trueaccord.scalapb

import com.google.protobuf.CodedInputStream
import com.google.protobuf.Descriptors.FieldDescriptor.{JavaType, Type}

object Descriptors {

  object WireType {
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
