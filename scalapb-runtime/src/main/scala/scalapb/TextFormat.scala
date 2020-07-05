package scalapb

import scalapb.textformat.Printer
import scalapb.textformat.ProtoAsciiParsing

case class TextFormatError(msg: String)

class TextFormatException(msg: String) extends RuntimeException(msg)

object TextFormat extends ProtoAsciiParsing {
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
