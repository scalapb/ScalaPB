package scalapb.textformat

/** Options for the text format [[Printer]].
  *
  * @param singleLineMode
  *   print the message on one line
  * @param escapeNonAscii
  *   escape non-ASCII characters in strings
  * @param omitDefaults
  *   skip a field that the binary encoding omits because it holds its default value
  */
final class PrinterOptions private (
    val singleLineMode: Boolean,
    val escapeNonAscii: Boolean,
    val omitDefaults: Boolean
) {
  def withSingleLineMode(value: Boolean): PrinterOptions = copy(singleLineMode = value)
  def withEscapeNonAscii(value: Boolean): PrinterOptions = copy(escapeNonAscii = value)
  def withOmitDefaults(value: Boolean): PrinterOptions   = copy(omitDefaults = value)

  private def copy(
      singleLineMode: Boolean = this.singleLineMode,
      escapeNonAscii: Boolean = this.escapeNonAscii,
      omitDefaults: Boolean = this.omitDefaults
  ): PrinterOptions = new PrinterOptions(singleLineMode, escapeNonAscii, omitDefaults)
}

object PrinterOptions {
  val default: PrinterOptions =
    new PrinterOptions(singleLineMode = false, escapeNonAscii = true, omitDefaults = false)

  val unicode: PrinterOptions           = default.withEscapeNonAscii(false)
  val unicodeSingleLine: PrinterOptions = unicode.withSingleLineMode(true)
}
