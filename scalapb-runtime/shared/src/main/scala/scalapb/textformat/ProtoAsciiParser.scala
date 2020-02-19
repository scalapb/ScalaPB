package scalapb.textformat

import fastparse._
import fastparse.ScriptWhitespace._

private[scalapb] object ProtoAsciiParser {
  def PrimitiveValue[_: P]: P[TPrimitive] = P(
    (Index ~ Basics.fractional).map(TLiteral.tupled) |
      (Index ~ Basics.bigInt).map(TIntLiteral.tupled) |
      (Index ~ Basics.bytesLiteral).map(TBytes.tupled) |
      (Index ~ Basics.literal).map(TLiteral.tupled)
  )

  def MessageValue[_: P]: P[TMessage] =
    P(
      Index ~ "{" ~/ KeyValue.rep ~/ "}" |
        Index ~ "<" ~/ KeyValue.rep ~/ ">"
    ).map(TMessage.tupled)

  def ValueArray[_: P]: P[TValue] =
    P((Index ~ "[" ~/ (PrimitiveValue | MessageValue).rep(0, ",") ~/ "]")).map(TArray.tupled)

  def MessageArray[_: P]: P[TValue] =
    P((Index ~ "[" ~/ MessageValue.rep(0, ",") ~/ "]")).map(TArray.tupled)

  def Value[_: P]: P[TValue] =
    P(
      MessageValue | MessageArray |
        ":" ~/ (MessageValue | ValueArray | PrimitiveValue)
    ).opaque("':', '{', '<', or '['")

  def KeyValue[_: P]: P[TField] =
    P(
      Index ~ Basics.identifier ~/ Value
    ).map(TField.tupled)

  def Message[_: P]: P[TMessage] = P(Index ~ KeyValue.rep ~ End).map(TMessage.tupled)
}
