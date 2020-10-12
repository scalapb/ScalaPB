package scalapb.textformat

import munit.{FunSuite, Location}
import scalapb.TextFormatException

trait ParserSuite {
  self: FunSuite =>

  def check[T](parser: Parser => String => Option[T], input: String, expected: T)(implicit
      loc: Location
  ): Unit = {
    val s     = new Parser(input)
    val value = parser(s)(s.it.next()).get
    assertEquals(value, expected)
  }

  def check2[T](parser: Parser => Option[T], input: String, expected: T)(implicit
      loc: Location
  ): Unit = {
    val s     = new Parser(input)
    val value = parser(s).get
    assertEquals(value, expected)
  }

  def checkFail[T](parser: Parser => String => Option[T], input: String)(implicit
      loc: Location
  ): Unit = {
    val s = new Parser(input)
    try {
      val v = parser(s)(s.it.next())
      assert(v.isEmpty, s"Expected parse to fail, but got: $v")
    } catch {
      case _: TextFormatException => // expected
    }
  }

  def checkFail2[T](parser: Parser => Option[T], input: String)(implicit loc: Location): Unit = {
    val s = new Parser(input)
    try {
      val v = parser(s)
      assert(v.isEmpty, s"Expected parse to fail, but got: $v")
    } catch {
      case _: TextFormatException => // expected
    }
  }

  def checkFail[T](parser: Parser => String => Option[T], input: String, message: String)(implicit
      loc: Location
  ): Unit = {
    val s = new Parser(input)
    interceptMessage[TextFormatException](message)(parser(s)(s.it.next()))
    ()
  }
}
