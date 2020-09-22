package scalapb.textformat

sealed trait TValue {
  def position: Position
}

sealed trait TPrimitive extends TValue {
  def asString: String
}

final case class TField(position: Position, name: String, value: TValue)

final case class TIntLiteral(position: Position, value: BigInt) extends TPrimitive {
  def asString = value.toString()
}

final case class TLiteral(position: Position, value: String) extends TPrimitive {
  def asString = value
}

final case class TBytes(position: Position, value: String) extends TPrimitive {
  def asString = value
}

final case class TMessage(position: Position, fields: Seq[TField]) extends TValue

final case class TArray(position: Position, values: Seq[TValue]) extends TValue
