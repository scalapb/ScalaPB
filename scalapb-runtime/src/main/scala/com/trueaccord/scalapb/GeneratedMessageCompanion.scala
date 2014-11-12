package com.trueaccord.scalapb

import com.google.protobuf.CodedOutputStream

import scala.util.Try

trait GeneratedEnum {
  def id: Int
  def name: String
  override def toString = name
}

trait GeneratedEnumCompanion[A <: GeneratedEnum] {
  def fromValue(id: Int): A
}

trait GeneratedMessage {
  def writeTo(output: CodedOutputStream): Unit

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

   def toByteArray: Array[Byte] = {
     val a = new Array[Byte](serializedSize)
     writeTo(com.google.protobuf.CodedOutputStream.newInstance(a))
     a
   }

  def serializedSize: Int
}

trait GeneratedMessageCompanion[A <: GeneratedMessage] {
  def parseFrom(s: Array[Byte]): A

  def validate(s: Array[Byte]): Try[A] = Try(parseFrom(s))

  def toByteArray(a: A): Array[Byte] = a.toByteArray

  def fromFieldsMap(fields: Map[Int, Any]): A

  def fromAscii(ascii: String): A

  def descriptor: Descriptors.MessageDescriptor
}

