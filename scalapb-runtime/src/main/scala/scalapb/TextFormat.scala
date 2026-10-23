package scalapb

import scalapb.textformat.Printer
import scalapb.textformat.PrinterOptions
import scalapb.textformat.ProtoAsciiParsing

case class TextFormatError(msg: String)

class TextFormatException(msg: String) extends RuntimeException(msg)

object TextFormat extends ProtoAsciiParsing {
  def printToString(m: GeneratedMessage, options: PrinterOptions): String =
    Printer.printToString(m, options)

  def printToString(m: GeneratedMessage): String = printToString(m, PrinterOptions.default)

  def printToUnicodeString(m: GeneratedMessage): String =
    printToString(m, PrinterOptions.unicode)

  def printToSingleLineUnicodeString(m: GeneratedMessage): String =
    printToString(m, PrinterOptions.unicodeSingleLine)
}
