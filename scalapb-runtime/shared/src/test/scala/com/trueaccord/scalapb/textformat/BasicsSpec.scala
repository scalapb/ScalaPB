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
    /*
    'double {
      check(ws("foo"), "fooz", ())
      check(double, "inf", Double.PositiveInfinity)
      check(double, "infinity", Double.PositiveInfinity)
      check(double, "-inf", Double.NegativeInfinity)
      check(double, "-infinity", Double.NegativeInfinity)
      check(double, "nan")(_.isNaN)
      check(double, "353", 353.0)
      check(double, "353.17", 353.17)
      check(double, "-353.17", -353.17)
      check(double, "-1.5e17", -1.5e17)
      checkFail(double, "17x3", """double:1:1 / double:1:1 ..."17x3"""")
      checkFail(double, "-17x3", """double:1:1 / double:1:1 ..."-17x3"""")
    }
    'float {
      check(float, "inf", Float.PositiveInfinity)
      check(float, "inff", Float.PositiveInfinity)
      check(float, "infinity", Float.PositiveInfinity)
      check(float, "infinityf", Float.PositiveInfinity)
      check(float, "-inf", Float.NegativeInfinity)
      check(float, "-inff", Float.NegativeInfinity)
      check(float, "-infinity", Float.NegativeInfinity)
      check(float, "-infinityf", Float.NegativeInfinity)
      check(float, "nan")(_.isNaN)
      check(float, "353", 353.0)
      check(float, "353.17", 353.17f)
      check(float, "-353.17", -353.17f)
      check(float, "-1.5e17", -1.5e17f)
      checkFail(float, "17x3", """float:1:1 / float:1:1 ..."17x3"""")
      checkFail(float, "-17x3", """float:1:1 / float:1:1 ..."-17x3"""")
    }
    */
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
      checkFail(bytesLiteral, "\"hello\nfoo\"")
      checkFail(bytesLiteral, "\"hello\'")
    }
  }

}
