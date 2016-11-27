package com.trueaccord.scalapb

import com.trueaccord.scalapb.textformat.{ AstUtils, Printer, ProtoAsciiParser }
import fastparse.core.{ ParseError, Parsed }

case class TextFormatError(msg: String)

class TextFormatException(msg: String) extends RuntimeException(msg)

object TextFormat {
  private def indexToLineCol(input: String, index: Int) = {
    val lines = input.take(1 + index).lines.toVector
    val line = lines.length
    val col = lines.lastOption.map(_.length).getOrElse(0)
    s"line $line, column $col"
  }

  def fromAscii[T <: GeneratedMessage with Message[T]](d: GeneratedMessageCompanion[T], s: String): Either[TextFormatError, T] = {
    ProtoAsciiParser.Message.parse(s) match {
      case Parsed.Success(msg, _) =>
        AstUtils.parseMessage(d, msg).left.map {
          a => TextFormatError(s"${a.error} (${indexToLineCol(s, a.index)})")
        }
      case f: Parsed.Failure[Char, String] =>
        Left(TextFormatError(ParseError(f).getMessage))
    }
  }

  def printToString(m: GeneratedMessage) = {
    Printer.printToString(m, singleLineMode = false, escapeNonAscii = true)
  }

  def printToUnicodeString(m: GeneratedMessage) = {
    Printer.printToString(m, singleLineMode = false, escapeNonAscii = false)
  }

  def printToSingleLineUnicodeString(m: GeneratedMessage) = {
    Printer.printToString(m, singleLineMode = true, escapeNonAscii = false)
  }
}

