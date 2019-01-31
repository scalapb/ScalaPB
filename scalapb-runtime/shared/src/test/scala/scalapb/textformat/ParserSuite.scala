package scalapb.textformat

import utest.assert

trait ParserSuite {
  import fastparse._

  def check[T](parser: P[_] => P[T], input: String, expected: T) = {
    val Parsed.Success(value, _) = parse(input, parser(_))
    assert(value == expected)
  }

  def check[T](parser: P[_] => P[T], input: String)(checker: T => Boolean) = {
    val Parsed.Success(value, _) = parse(input, parser(_))
    assert(checker(value))
  }

  def checkFail[T](parser: P[_] => P[T], input: String) = {
    assert(parse(input, parser(_)).isInstanceOf[Parsed.Failure])
  }

  def checkFail[T](parser: P[_] => P[T], input: String, expectedTrace: String) = {
    val failure     = parse(input, parser(_)).asInstanceOf[Parsed.Failure]
    val actualTrace = failure.extra.trace(true).msg
    assert(expectedTrace.trim == actualTrace.trim)
  }
}
