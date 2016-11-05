package com.trueaccord.scalapb.textformat

import fastparse.core.Parsed
import utest.asserts.assert

trait ParserSuite {
  import fastparse.all.P

  def check[T](parser: P[T], input: String, expected: T) = {
    val Parsed.Success(value, _) = parser.parse(input)
    assert(value == expected)
  }

  def check[T](parser: P[T], input: String)(checker: T => Boolean) = {
    val Parsed.Success(value, _) = parser.parse(input)
    assert(checker(value))
  }

  def checkFail[T](parser: P[T], input: String) = {
    assert(parser.parse(input).isInstanceOf[Parsed.Failure[_, _]])
  }

  def checkFail[T](parser: P[T], input: String, expectedTrace: String) = {
    val failure = parser.parse(input).asInstanceOf[Parsed.Failure[_, _]]
    val actualTrace = failure.extra.traced.trace
    assert(expectedTrace.trim == actualTrace.trim)
  }
}
