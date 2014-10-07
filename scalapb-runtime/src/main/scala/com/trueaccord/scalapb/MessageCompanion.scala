package com.trueaccord.scalapb

import com.google.protobuf.Descriptors.Descriptor

import scala.util.Try

trait GeneratedMessage {
  def serialize: Array[Byte]

  def allFields: Map[Int, Any]
}

trait MessageCompanion[A <: GeneratedMessage] {
  def parse(s: Array[Byte]): A

  def validate(s: Array[Byte]): Try[A] = Try(parse(s))

  def serialize(a: A): Array[Byte] = a.serialize

  def fromFieldsMap(fields: Map[Int, Any]): A

  def companionForField(tagNumber: Int): MessageCompanion[_]

  def fromAscii(ascii: String): A

  def descriptor: Descriptor
}
