package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TextFormatError}
import scalapb.TextFormatException

trait ProtoAsciiParsing {
  def fromAscii[T <: GeneratedMessage](
      d: GeneratedMessageCompanion[T],
      s: String
  ): Either[TextFormatError, T] = {
    fromAscii(d, s, 100)
  }

  /** Parses text with a maximum number of nested messages (the root is not counted). Repeated
    * message elements count individually, not cumulatively.
    */
  def fromAscii[T <: GeneratedMessage](
      d: GeneratedMessageCompanion[T],
      s: String,
      recursionLimit: Int
  ): Either[TextFormatError, T] = {
    val p = new Parser(s, recursionLimit)
    try {
      val msg = p.parseMessage
      AstUtils.parseMessage(d, msg).left.map { a =>
        TextFormatError(s"${a.error} (line ${a.position.line + 1}, column ${a.position.col + 1})")
      }
    } catch {
      case e: TextFormatException => Left(TextFormatError(e.getMessage()))
    }
  }
}
