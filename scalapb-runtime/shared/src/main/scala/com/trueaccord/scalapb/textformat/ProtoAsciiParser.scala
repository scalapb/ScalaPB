package com.trueaccord.scalapb.textformat

import fastparse.WhitespaceApi

private[scalapb] object ProtoAsciiParser {
  val White = WhitespaceApi.Wrapper {
    import fastparse.all._

    NoTrace(Basics.whiteSpace)
  }

  import White._
  import fastparse.noApi._

  val PrimitiveValue: P[PPrimitive] = P(
    (Index ~ Basics.bigInt).map(PIntLiteral.tupled) |
      (Index ~ Basics.bytesLiteral).map(PBytes.tupled) |
      (Index ~ Basics.literal).map(PLiteral.tupled))

  val PrimitiveArray: P[PValue] = P((Index ~ "[" ~/ PrimitiveValue.rep(0, ",") ~/ "]")).map(PPrimitiveArray.tupled)

  val MessageValue: P[PMessage] = P(
    Index ~ "{" ~/ KeyValue.rep ~/ "}" |
      Index ~ "<" ~/ KeyValue.rep ~/ ">"
  ).map(PMessage.tupled)

  val MessageArray: P[PValue] = P((Index ~ "[" ~/ MessageValue.rep(0, ",") ~/ "]")).map(PMessageArray.tupled)

  val Value: P[PValue] = P(
    ":" ~/ (PrimitiveValue | PrimitiveArray) |
      MessageValue | MessageArray)

  val KeyValue: P[PField] = P(
    Index ~ Basics.identifier ~/ Value
  ).map(PField.tupled)

  val Message: P[PMessage] = P(Index ~ KeyValue.rep).map(PMessage.tupled)
}
