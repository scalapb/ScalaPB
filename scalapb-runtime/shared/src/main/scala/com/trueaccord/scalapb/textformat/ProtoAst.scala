package com.trueaccord.scalapb.textformat

sealed trait TValue {
  def index: Int
}

sealed trait TPrimitive extends TValue {
  def asString: String
}

final case class TField(index: Int, name: String, value: TValue)

final case class TIntLiteral(index: Int, value: BigInt) extends TPrimitive {
  def asString = value.toString()
}

final case class TLiteral(index: Int, value: String) extends TPrimitive {
  def asString = value
}

final case class TBytes(index: Int, value: String) extends TPrimitive {
  def asString = value
}

final case class TMessage(index: Int, fields: Seq[TField]) extends TValue

final case class TArray(index: Int, values: Seq[TValue]) extends TValue
