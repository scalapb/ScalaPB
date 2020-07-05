package scalapb.textformat

import munit.{FunSuite, Location}

trait ParserSuite {
  self: FunSuite =>
  import fastparse._

  def check[T](parser: P[_] => P[T], input: String, expected: T)(implicit loc: Location) = {
    val Parsed.Success(value, _) = parse(input, parser(_))
    assertEquals(value, expected)
  }

  def check[T](parser: P[_] => P[T], input: String)(
      checker: T => Boolean
  )(implicit loc: Location) = {
    val Parsed.Success(value, _) = parse(input, parser(_))
    assert(checker(value))
  }

  def checkFail[T](parser: P[_] => P[T], input: String)(implicit loc: Location) = {
    assert(parse(input, parser(_)).isInstanceOf[Parsed.Failure])
  }

  def checkFail[T](parser: P[_] => P[T], input: String, expectedTrace: String)(
      implicit loc: Location
  ) = {
    val failure     = parse(input, parser(_)).asInstanceOf[Parsed.Failure]
    val actualTrace = failure.extra.trace(true).msg
    assertEquals(expectedTrace.trim, actualTrace.trim)
  }
}
