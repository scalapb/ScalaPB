package com.trueaccord.scalapb

import scala.util.Try

trait GeneratedMessage {
  def serialize: Array[Byte]

  def getField(field: Descriptors.FieldDescriptor): Any
  
  def companion: GeneratedMessageCompanion[_]

  def getAllFields: Seq[(Descriptors.FieldDescriptor, Any)] =
    companion.descriptor.fields.flatMap {
      f =>
        getField(f) match {
          case None => None
          case Nil => None
          case v => Some(f -> v)
        }
    }
}

trait GeneratedMessageCompanion[A <: GeneratedMessage] {
  def parse(s: Array[Byte]): A

  def validate(s: Array[Byte]): Try[A] = Try(parse(s))

  def serialize(a: A): Array[Byte] = a.serialize

  def fromFieldsMap(fields: Map[Int, Any]): A

  def fromAscii(ascii: String): A

  def descriptor: Descriptors.MessageDescriptor
}

