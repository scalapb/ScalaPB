package com.trueaccord.scalapb.textformat

import fastparse.core.ParserApi

import scala.language.implicitConversions

object Basics {

  import fastparse.all._

  protected implicit def strToParserApi(s: String): ParserApi[Unit, Char, String] = parserApi(s)

  protected implicit def parserToParserApi[T](s: Parser[T]): ParserApi[T, Char, String] = parserApi(s)

  val Newline = P(StringIn("\r\n", "\n"))

  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V) {
    def apply(t: T) = f(t)

    override def toString() = name
  }

  private val hexDigitStr = "0123456789abcdefABCDEF"

  val Digits = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val HexDigits = NamedFunction(hexDigitStr contains (_: Char), "HexDigits")
  val OctDigits = NamedFunction('0' to '7' contains (_: Char), "OctDigits")
  val CharChunk = NamedFunction((c: Char) => !"\n\r".contains(c), "CharChunk")


  // TODO(nadavsr): figure out this
  val sameLineCharChunks = P(CharsWhile(CharChunk) | !Newline ~ AnyChar)

  val lineComment = P("#" ~ sameLineCharChunks.rep ~ &(Newline | End))

  val whiteSpace = (CharIn(" \n\r\t\f") | lineComment).opaque("whitespace").rep

  val identifier = P(CharIn('a' to 'z', 'A' to 'Z', '0' to '9', "_").rep(1).!).opaque("identifier")

  val literal = P(CharIn('a' to 'z', 'A' to 'Z', '0' to '9', "_-.").rep(1).!).opaque("literal")

  val digits = P(CharsWhile(Digits))
  val hexDigits = P(CharsWhile(HexDigits))
  val octDigits = P(CharsWhile(OctDigits))

  val exponent = P(CharIn("eE") ~ CharIn("+-").? ~ digits)
  val fractional = (CharIn("+-").? ~ (digits ~ "." ~ digits.? | "." ~ digits) ~ exponent.? ~ CharIn("fF").?).!

  val decIntegral = P("0" | CharIn('1' to '9') ~ digits.?).!.map(p => BigInt(p))
  val hexIntegral = P("0x" ~/ hexDigits.!).map(p => BigInt(p, 16))
  val octIntegral = P("0" ~ octDigits.!).map(p => BigInt(p, 8))

  val integral: P[BigInt] = P(hexIntegral | octIntegral | decIntegral)

  val bigInt: P[BigInt] = P(CharIn("+-").!.? ~ integral).map({
    case (Some("-"), number) => -number
    case (_, number) => number
  })

  val strNoDQChars = P(CharsWhile(!"\"\n\\".contains(_: Char)))
  val strNoQChars = P(CharsWhile(!"'\n\\".contains(_: Char)))
  val escape = P("\\" ~ AnyChar)
  val singleBytesLiteral = P(
    "\"" ~/ (strNoDQChars | escape).rep.! ~ "\"" |
      "'" ~/ (strNoQChars | escape).rep.! ~ "'").opaque("string")

  val bytesLiteral = P(singleBytesLiteral.rep(1, whiteSpace)).map(_.mkString)

  val boolean: P[Boolean] = P(
    ("true" | "t" | "1").map(_ => true) |
      ("false" | "f" | "0").map(_ => false)).opaque("'true' or 'false'")

  def ws(s: String): P[Unit] = P(s ~ &(whiteSpace))
}
