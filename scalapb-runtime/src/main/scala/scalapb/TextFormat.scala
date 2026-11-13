package scalapb

import scalapb.textformat.Printer
import scalapb.textformat.ProtoAsciiParsing

case class TextFormatError(msg: String)

class TextFormatException(msg: String) extends RuntimeException(msg)

object TextFormat extends ProtoAsciiParsing {
  def printToString(m: GeneratedMessage): String = {
    Printer.printToString(m, singleLineMode = false, escapeNonAscii = true)
  }

  /** Optionally includes preserved unknown wire fields using their numeric tags.
    */
  def printToString(m: GeneratedMessage, printUnknownFields: Boolean): String =
    Printer.printToString(
      m,
      singleLineMode = false,
      escapeNonAscii = true,
      printUnknownFields = printUnknownFields
    )

  def printToUnicodeString(m: GeneratedMessage) = {
    Printer.printToString(m, singleLineMode = false, escapeNonAscii = false)
  }

  def printToSingleLineUnicodeString(m: GeneratedMessage) = {
    Printer.printToString(m, singleLineMode = true, escapeNonAscii = false)
  }
}
