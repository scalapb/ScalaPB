package scalapb.textformat

import fastparse.core.ParserApi

trait ParserCompat {
  import fastparse.all._

  protected implicit def strToParserApi(s: String): ParserApi[Unit, Char, String] = parserApi(s)

  protected implicit def parserToParserApi[T](s: Parser[T]): ParserApi[T, Char, String] = parserApi(s)
}
