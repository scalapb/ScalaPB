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

  val PrimitiveValue: P[TPrimitive] = P(
    (Index ~ Basics.fractional).map(TLiteral.tupled) |
    (Index ~ Basics.bigInt).map(TIntLiteral.tupled) |
      (Index ~ Basics.bytesLiteral).map(TBytes.tupled) |
      (Index ~ Basics.literal).map(TLiteral.tupled))

  val MessageValue: P[TMessage] = P(
    Index ~ "{" ~/ KeyValue.rep ~/ "}" |
      Index ~ "<" ~/ KeyValue.rep ~/ ">"
  ).map(TMessage.tupled)

  val ValueArray: P[TValue] = P((Index ~ "[" ~/ (PrimitiveValue | MessageValue).rep(0, ",".~/ ) ~/ "]")).map(TArray.tupled)

  val MessageArray: P[TValue] = P((Index ~ "[" ~/ MessageValue.rep(0, ",") ~/ "]")).map(TArray.tupled)

  val Value: P[TValue] = P(
    MessageValue | MessageArray |
      ":" ~/ (MessageValue | ValueArray | PrimitiveValue)).opaque("':', '{', '<', or '['")

  val KeyValue: P[TField] = P(
    Index ~ Basics.identifier ~/ Value
  ).map(TField.tupled)

  val Message: P[TMessage] = P(Index ~ KeyValue.rep ~ End).map(TMessage.tupled)
}
