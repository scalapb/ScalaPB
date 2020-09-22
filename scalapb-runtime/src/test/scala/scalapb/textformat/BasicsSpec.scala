package scalapb.textformat

import munit._

class BasicsSpec extends FunSuite with ParserSuite {
  test("_.isIdentifier") {
    assert(Tokenizer.isIdentifier("abcd"))
    assert(Tokenizer.isIdentifier("abcd_2"))
    assert(Tokenizer.isIdentifier("abcd_2_vxyz"))
    assert(!Tokenizer.isIdentifier("abcd bar"))
    assert(Tokenizer.isIdentifier("2abc"))
    assert(!Tokenizer.isIdentifier("  abc"))
  }

  def fractional(p: Parser): String => Option[String] = t => p.tryFractional(t).map(_.value)

  def bigInt(p: Parser): String => Option[BigInt] = t => p.tryBigInt(t)

  def literal(p: Parser): String => Option[String] = t => p.tryLiteral(t).map(_.value)

  def bytesLiteral(p: Parser): String => Option[String] = t => p.tryBytesLiteral(t).map(_.value)

  test("number") {
    check(fractional, "0.2", "0.2")
    check(fractional, "2.0", "2.0")
    check(fractional, "2.0f", "2.0f")
    check(fractional, "-12.34f", "-12.34f")
    check(fractional, ".2", ".2")
    check(fractional, "2.3e+14", "2.3e+14")
    check(fractional, "2.3e-14", "2.3e-14")
    check(fractional, ".3e-14", ".3e-14")
    check(fractional, ".3e+14", ".3e+14")
    check(fractional, ".3e+14F", ".3e+14F")
    check(fractional, "2.", "2.")
    check(fractional, "2.e+5", "2.e+5")
    check(fractional, "2.e+5f", "2.e+5f")
    checkFail(fractional, ".")
    checkFail(fractional, "17")
    checkFail(fractional, "0")
  }

  test("bigint") {
    check[BigInt](bigInt, "0", 0)
    check[BigInt](bigInt, "-0", 0)
    check[BigInt](bigInt, "0x14", 0x14)
    check[BigInt](bigInt, "-0x14", -0x14)
    check[BigInt](bigInt, "025", 21)
    check[BigInt](bigInt, "-025", -21)
    check[BigInt](bigInt, "1417", 1417)
    check[BigInt](bigInt, "-1417", -1417)
    check(bigInt, "33353333333333333333333333333", BigInt("33353333333333333333333333333"))
    check(bigInt, "-33353333333333333333333333333", BigInt("-33353333333333333333333333333"))
    checkFail(bigInt, "abcd")
  }

  test("literal") {
    check(literal, "true", "true")
  }

  test("bytes") {
    check(bytesLiteral(_), "\"hello\"", "hello")
    check(bytesLiteral(_), "\"he'llo\"", "he'llo")
    check(bytesLiteral(_), "\"he\\\"llo\"", "he\\\"llo")
    check(bytesLiteral(_), "\"he\\'llo\"", "he\\'llo")
    check(bytesLiteral(_), "'he\"llo'", "he\"llo")
    check(bytesLiteral(_), "\"hello world\"", "hello world")
    check(bytesLiteral(_), "\"hello world\" \"boo\"", "hello worldboo")
    check(bytesLiteral(_), "\"hello world\" \'boo\'", "hello worldboo")
    check(bytesLiteral(_), "\"hello\\nfoo\"", "hello\\nfoo")
    check(bytesLiteral(_), "\"\\000\\001\\a\\n\"", "\\000\\001\\a\\n")
    check(bytesLiteral(_), "\"\\000\\001\\a\\n\"", "\\000\\001\\a\\n")
    check(bytesLiteral(_), "\"\u5d8b\u2367\u633d\"", "\u5d8b\u2367\u633d")
    checkFail(bytesLiteral(_), "\"hello\nfoo\"")
    checkFail(bytesLiteral(_), "\"hello\'")
  }
}
