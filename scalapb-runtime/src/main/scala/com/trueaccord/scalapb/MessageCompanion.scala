package com.trueaccord.scalapb

import scala.util.Try

trait GeneratedMessage {
  def serialize: Array[Byte]
}

trait MessageCompanion[A <: GeneratedMessage] {
  def parse(s: Array[Byte]): A

  def validate(s: Array[Byte]): Try[A] = Try(parse(s))

  def serialize(a: A): Array[Byte] = a.serialize
}
