package scalapb.textformat

import fastparse.NoWhitespace._

import scala.language.implicitConversions

object Basics extends ParserCompat {

  import fastparse._

  def Newline[_: P] = P(StringIn("\r\n", "\n"))

  case class NamedFunction[T, V](f: T => V, name: String) extends (T => V) {
    def apply(t: T) = f(t)

    override def toString() = name
  }

  private val hexDigitStr = "0123456789abcdefABCDEF"

  val Digits    = NamedFunction('0' to '9' contains (_: Char), "Digits")
  val HexDigits = NamedFunction(hexDigitStr contains (_: Char), "HexDigits")
  val OctDigits = NamedFunction('0' to '7' contains (_: Char), "OctDigits")
  val CharChunk = NamedFunction((c: Char) => !"\n\r".contains(c), "CharChunk")

  // TODO(nadavsr): figure out this
  def sameLineCharChunks[_: P] = P(CharsWhile(CharChunk) | (!Newline) ~ AnyChar)

  def lineComment[_: P] = P("#" ~ sameLineCharChunks.rep ~ &(Newline | End))

  def whiteSpace[_: P] = (CharIn(" \n\r\t\f") | lineComment).opaque("whitespace").rep

  def identifier[_: P] = P(CharIn("a-z", "A-Z", "0-9", "_").rep(1).!).opaque("identifier")

  def literal[_: P] = P(CharIn("a-z", "A-Z", "0-9", "_\\-.").rep(1).!).opaque("literal")

  def digits[_: P]    = P(CharsWhile(Digits))
  def hexDigits[_: P] = P(CharsWhile(HexDigits))
  def octDigits[_: P] = P(CharsWhile(OctDigits))

  def exponent[_: P] = P(CharIn("eE") ~ CharIn("+\\-").? ~ digits)
  def fractional[_: P] =
    (CharIn("+\\-").? ~ (digits ~ "." ~ digits.? | "." ~ digits) ~ exponent.? ~ CharIn("fF").?).!

  def decIntegral[_: P] = P("0" | CharIn("1-9") ~ digits.?).!.map(p => BigInt(p))
  def hexIntegral[_: P] = P("0x" ~/ hexDigits.!).map(p => BigInt(p, 16))
  def octIntegral[_: P] = P("0" ~ octDigits.!).map(p => BigInt(p, 8))

  def integral[_: P]: P[BigInt] = P(hexIntegral | octIntegral | decIntegral)

  def bigInt[_: P]: P[BigInt] =
    P(CharIn("+\\-").!.? ~ integral).map({
      case (Some("-"), number) => -number
      case (_, number)         => number
    })

  def strNoDQChars[_: P] = P(CharsWhile(!"\"\n\\".contains(_: Char)))
  def strNoQChars[_: P]  = P(CharsWhile(!"'\n\\".contains(_: Char)))
  def escape[_: P]       = P("\\" ~ AnyChar)
  def singleBytesLiteral[_: P] =
    P(
      "\"" ~/ (strNoDQChars | escape).rep.! ~ "\"" |
        "'" ~/ (strNoQChars | escape).rep.! ~ "'"
    ).opaque("string")

  def bytesLiteral[_: P] = P(singleBytesLiteral.rep(1, whiteSpace)).map(_.mkString)

  def boolean[_: P]: P[Boolean] =
    P(
      ("true" | "t" | "1").map(_ => true) |
        ("false" | "f" | "0").map(_ => false)
    ).opaque("'true' or 'false'")

  def ws[_: P](s: String): P[Unit] = P(s ~ &(whiteSpace))
}
