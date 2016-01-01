package com.trueaccord.scalapb.textformat

import utest._

object BasicsSpec extends TestSuite with ParserSuite {

  import Basics._

  val tests = TestSuite {
    'identifier {
      check(identifier, "abcd", "abcd")
      check(identifier, "abcd_2", "abcd_2")
      check(identifier, "abcd_2_vxyz", "abcd_2_vxyz")
      check(identifier, "abcd bar", "abcd")
      check(identifier, "2abc", "2abc")
      checkFail(identifier, "  abcd")
    }

    'number {
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

    'bigint {
      check(bigInt, "0", 0)
      check(bigInt, "-0", 0)
      check(bigInt, "0x14", 0x14)
      check(bigInt, "-0x14", -0x14)
      check(bigInt, "025", 21)
      check(bigInt, "-025", -21)
      check(bigInt, "1417", 1417)
      check(bigInt, "-1417", -1417)
      check(bigInt, "33353333333333333333333333333", BigInt("33353333333333333333333333333"))
      check(bigInt, "-33353333333333333333333333333", BigInt("-33353333333333333333333333333"))
      checkFail(bigInt, "abcd")
    }

    'literal {
      check(literal, "1.0f", "1.0f")
    }

    'boolean {
      check(boolean, "t", true)
      check(boolean, "1", true)
      check(boolean, "f", false)
      check(boolean, "0", false)
    }

    'bytes {
      check(bytesLiteral, "\"hello\"", "hello")
      check(bytesLiteral, "\"he'llo\"", "he'llo")
      check(bytesLiteral, "\"he\\\"llo\"", "he\\\"llo")
      check(bytesLiteral, "\"he\\'llo\"", "he\\'llo")
      check(bytesLiteral, "'he\"llo'", "he\"llo")
      check(bytesLiteral, "\"hello world\"", "hello world")
      check(bytesLiteral, "\"hello world\" \"boo\"", "hello worldboo")
      check(bytesLiteral, "\"hello world\" \'boo\'", "hello worldboo")
      check(bytesLiteral, "\"hello\\nfoo\"", "hello\\nfoo")
      check(bytesLiteral, "\"\\000\\001\\a\\n\"", "\\000\\001\\a\\n")
      check(bytesLiteral, "\"\\000\\001\\a\\n\"", "\\000\\001\\a\\n")
      check(bytesLiteral, "\"\u5d8b\u2367\u633d\"", "\u5d8b\u2367\u633d")
      checkFail(bytesLiteral, "\"hello\nfoo\"")
      checkFail(bytesLiteral, "\"hello\'")
    }
  }

}
