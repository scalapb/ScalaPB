package com.trueaccord.scalapb

import scala.util.Try

trait Message {
  def serialize: Array[Byte]
}

trait MessageCompanion[A <: Message] {
  def parse(s: Array[Byte]): A

  def validate(s: Array[Byte]): Try[A] = Try(parse(s))

  def serialize(a: A): Array[Byte] = a.serialize
}
