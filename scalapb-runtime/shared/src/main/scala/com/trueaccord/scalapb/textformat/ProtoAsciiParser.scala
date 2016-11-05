package com.trueaccord.scalapb.textformat

import fastparse.WhitespaceApi

import scala.language.implicitConversions

private[scalapb] object ProtoAsciiParser {
  val White = WhitespaceApi.Wrapper {
    import fastparse.all._

    NoTrace(Basics.whiteSpace)
  }

  import fastparse.noApi._

  // This is needed due to https://github.com/lihaoyi/fastparse/issues/72
  protected implicit def strToParserApi(s: String): WhitespaceApi[Unit] = White.parserApi(s)
  protected implicit def parserToParserApi[T](s: Parser[T]): WhitespaceApi[T] = White.parserApi(s)

  val PrimitiveValue: P[PPrimitive] = P(
    (Index ~ Basics.fractional).map(PLiteral.tupled) |
    (Index ~ Basics.bigInt).map(PIntLiteral.tupled) |
      (Index ~ Basics.bytesLiteral).map(PBytes.tupled) |
      (Index ~ Basics.literal).map(PLiteral.tupled))

  val MessageValue: P[PMessage] = P(
    Index ~ "{" ~/ KeyValue.rep ~/ "}" |
      Index ~ "<" ~/ KeyValue.rep ~/ ">"
  ).map(PMessage.tupled)

  val ValueArray: P[PValue] = P((Index ~ "[" ~/ (PrimitiveValue | MessageValue).rep(0, ",".~/ ) ~/ "]")).map(PArray.tupled)

  val MessageArray: P[PValue] = P((Index ~ "[" ~/ MessageValue.rep(0, ",") ~/ "]")).map(PArray.tupled)

  val Value: P[PValue] = P(
    MessageValue | MessageArray |
      ":" ~/ (MessageValue | ValueArray | PrimitiveValue)).opaque("':', '{', '<', or '['")

  val KeyValue: P[PField] = P(
    Index ~ Basics.identifier ~/ Value
  ).map(PField.tupled)

  val Message: P[PMessage] = P(Index ~ KeyValue.rep ~ End).map(PMessage.tupled)
}
