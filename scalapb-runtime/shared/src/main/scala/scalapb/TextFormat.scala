package scalapb

import scalapb.textformat.{AstUtils, Printer, ProtoAsciiParser}
import fastparse._

case class TextFormatError(msg: String)

class TextFormatException(msg: String) extends RuntimeException(msg)

object TextFormat {
  private def indexToLineCol(input: String, index: Int) = {
    // https://github.com/scala/bug/issues/11125
    val lines = Predef.augmentString(input.take(1 + index)).linesIterator.toVector
    val line  = lines.length
    val col   = lines.lastOption.map(_.length).getOrElse(0)
    s"line $line, column $col"
  }

  def fromAscii[T <: GeneratedMessage](
      d: GeneratedMessageCompanion[T],
      s: String
  ): Either[TextFormatError, T] = {
    parse(s, ProtoAsciiParser.Message(_)) match {
      case Parsed.Success(msg, _) =>
        AstUtils.parseMessage(d, msg).left.map { a =>
          TextFormatError(s"${a.error} (${indexToLineCol(s, a.index)})")
        }
      case f: Parsed.Failure =>
        Left(TextFormatError(f.trace(true).longMsg))
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
