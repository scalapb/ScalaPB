package scalapb.textformat

import java.util.regex.Pattern

case class Position(line: Int, col: Int)

class Tokenizer(s: String) {
  import Tokenizer._
  var pos                  = 0
  var line                 = 0
  var column               = 0
  var tokenStartLine       = 0
  var tokenStartCol        = 0
  var prevTokenStartLine   = 0
  var prevTokenStartCol    = 0
  var currentToken: String = ""

  nextToken()

  def lastPosition: Position = Position(prevTokenStartLine, prevTokenStartCol)

  def position: Position = Position(tokenStartLine, tokenStartCol)

  private def skipWhitespace(): Unit = {
    while (pos < s.length() && WHITESPACE.indexOf(s(pos).toInt) != -1) {
      if (s(pos) == '#') {
        while (pos < s.length() && s(pos) != '\n') { pos += 1 }
        // process \n on next iteration
      } else if (s(pos) == '\n') {
        line += 1
        column = 0
        pos += 1
      } else {
        column += 1
        pos += 1
      }
    }
  }

  def next(): String = {
    val r = currentToken
    nextToken()
    r
  }

  def hasNext: Boolean = currentToken.nonEmpty

  def findEndOfQuotedString(pos: Int, quote: Char): Int = {
    var current = pos
    var break   = false
    while (current < s.length() && s(current) != '\n' && !break) {
      if (s(current) == quote) {
        current += 1
        break = true
      } else if (s(current) == '\\') {
        current += 1
        if (current < s.length() && s(current) != '\n') {
          current += 1
        }
      } else {
        current += 1
      }
    }
    current

  }

  def nextToken(): Unit = {
    skipWhitespace()
    prevTokenStartLine = tokenStartLine
    prevTokenStartCol = tokenStartCol
    tokenStartLine = line
    tokenStartCol = column
    if (pos == s.length()) { currentToken = "" }
    else {
      val m = TEXT.matcher(s).region(pos, s.length())
      if (m.lookingAt()) {
        currentToken = m.group()
        pos += currentToken.length()
        column += currentToken.length()
      } else if (s(pos) == '"' || s(pos) == '\'') {
        val end = findEndOfQuotedString(pos + 1, s(pos))
        currentToken = s.substring(pos, end)
        column += (end - pos)
        pos = end
      } else {
        currentToken = s.substring(pos, pos + 1)
        pos += 1
        column += 1
      }
    }
  }
}

object Tokenizer {
  val WHITESPACE = "\n\r\t\f #"
  val TEXT       = Pattern.compile("[.a-zA-Z0-9_+-]+")

  val FRACTIONAL: Pattern =
    Pattern.compile("[+-]?([0-9]+[.][0-9]*|[.][0-9]+)([eE][+-]?[0-9]+)?[fF]?")

  def isIdentifier(token: String): Boolean = {
    token.forall(c =>
      (c >= 'a' && c <= 'z') ||
        (c >= 'A' && c <= 'Z') ||
        (c >= '0' && c <= '9') ||
        (c == '_') ||
        (c == '.')
    )
  }
}
