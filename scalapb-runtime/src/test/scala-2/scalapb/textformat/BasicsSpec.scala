package scalapb.textformat

import munit._

class BasicsSpec extends FunSuite with ParserSuite {
  import Basics._

  test("identifier") {
    check(identifier(_), "abcd", "abcd")
    check(identifier(_), "abcd_2", "abcd_2")
    check(identifier(_), "abcd_2_vxyz", "abcd_2_vxyz")
    check(identifier(_), "abcd bar", "abcd")
    check(identifier(_), "2abc", "2abc")
    checkFail(identifier(_), "  abcd")
  }

  test("number") {
    check(fractional(_), "0.2", "0.2")
    check(fractional(_), "2.0", "2.0")
    check(fractional(_), "2.0f", "2.0f")
    check(fractional(_), "-12.34f", "-12.34f")
    check(fractional(_), ".2", ".2")
    check(fractional(_), "2.3e+14", "2.3e+14")
    check(fractional(_), "2.3e-14", "2.3e-14")
    check(fractional(_), ".3e-14", ".3e-14")
    check(fractional(_), ".3e+14", ".3e+14")
    check(fractional(_), ".3e+14F", ".3e+14F")
    check(fractional(_), "2.", "2.")
    check(fractional(_), "2.e+5", "2.e+5")
    check(fractional(_), "2.e+5f", "2.e+5f")
    checkFail(fractional(_), ".")
    checkFail(fractional(_), "17")
    checkFail(fractional(_), "0")
  }

  test("bigint") {
    check(bigInt(_), "0", 0)
    check(bigInt(_), "-0", 0)
    check(bigInt(_), "0x14", 0x14)
    check(bigInt(_), "-0x14", -0x14)
    check(bigInt(_), "025", 21)
    check(bigInt(_), "-025", -21)
    check(bigInt(_), "1417", 1417)
    check(bigInt(_), "-1417", -1417)
    check(bigInt(_), "33353333333333333333333333333", BigInt("33353333333333333333333333333"))
    check(bigInt(_), "-33353333333333333333333333333", BigInt("-33353333333333333333333333333"))
    checkFail(bigInt(_), "abcd")
  }

  test("literal") {
    check(literal(_), "1.0f", "1.0f")
  }

  test("boolean") {
    check(boolean(_), "t", true)
    check(boolean(_), "1", true)
    check(boolean(_), "f", false)
    check(boolean(_), "0", false)
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
