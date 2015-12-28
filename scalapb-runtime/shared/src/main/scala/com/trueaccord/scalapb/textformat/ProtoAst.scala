package com.trueaccord.scalapb.textformat

sealed trait PValue {
  def index: Int
}

sealed trait PPrimitive extends PValue

final case class PField(index: Int, name: String, value: PValue)

final case class PIntLiteral(index: Int, value: BigInt) extends PPrimitive

final case class PLiteral(index: Int, value: String) extends PPrimitive

final case class PBytes(index: Int, value: String) extends PPrimitive

final case class PMessage(index: Int, fields: Seq[PField]) extends PValue

final case class PPrimitiveArray(index: Int, values: Seq[PPrimitive]) extends PValue

final case class PMessageArray(index: Int, values: Seq[PMessage]) extends PValue
