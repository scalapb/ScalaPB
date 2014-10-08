package com.trueaccord.scalapb

import com.google.protobuf.Descriptors.FieldDescriptor.JavaType

object Descriptors {
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
  case class PrimitiveType(javaType: JavaType) extends FieldType
  case class MessageType(m: MessageDescriptor) extends FieldType
  case class EnumType(companion: EnumDescriptor) extends FieldType

  case class FieldDescriptor(index: Int, number: Int, name: String, label: Label, fieldType: FieldType)

  case class EnumDescriptor(index: Int, name: String, scalaType: Enumeration)
}
