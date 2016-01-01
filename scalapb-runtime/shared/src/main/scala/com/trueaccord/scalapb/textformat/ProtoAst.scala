package com.trueaccord.scalapb.textformat

sealed trait PValue {
  def index: Int
}

sealed trait PPrimitive extends PValue {
  def asString: String
}

final case class PField(index: Int, name: String, value: PValue)

final case class PIntLiteral(index: Int, value: BigInt) extends PPrimitive {
  def asString = value.toString()
}

final case class PLiteral(index: Int, value: String) extends PPrimitive {
  def asString = value
}

final case class PBytes(index: Int, value: String) extends PPrimitive {
  def asString = value
}

final case class PMessage(index: Int, fields: Seq[PField]) extends PValue

final case class PArray(index: Int, values: Seq[PValue]) extends PValue
