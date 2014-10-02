package com.trueaccord.scalapb

trait MessageCompanion[A] {
  def parseFrom(s: Array[Byte]): A

  def serialize(a: A): Array[Byte]
}
