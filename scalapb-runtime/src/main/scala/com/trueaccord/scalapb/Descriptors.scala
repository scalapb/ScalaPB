package com.trueaccord.scalapb

import com.google.protobuf.Descriptors.FieldDescriptor.{JavaType, Type}

object Descriptors {

  object WireType {
    val WIRETYPE_VARINT = 0
    val WIRETYPE_FIXED64 = 1
    val WIRETYPE_LENGTH_DELIMITED = 2
    val WIRETYPE_START_GROUP = 3
    val WIRETYPE_END_GROUP = 4
    val WIRETYPE_FIXED32 = 5
  }

  class FileDescriptor(m: => Seq[MessageDescriptor], e: => Seq[EnumDescriptor]) {
    lazy val messages = m
    lazy val enums = e
  }

  class MessageDescriptor(val name: String,
                          val companion: GeneratedMessageCompanion[_],
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

  sealed trait FieldType

  case class PrimitiveType(javaType: JavaType, fieldType: Type) extends FieldType

  case class MessageType(m: MessageDescriptor) extends FieldType

  case class EnumType(companion: EnumDescriptor) extends FieldType

  case class FieldDescriptor(index: Int, number: Int, name: String, label: Label,
                             fieldType: FieldType, isPacked: Boolean = false)

  case class EnumDescriptor(index: Int, name: String, scalaType: Enumeration)
}
