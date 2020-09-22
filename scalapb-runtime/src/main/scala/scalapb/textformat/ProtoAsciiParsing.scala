package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TextFormatError}
import scalapb.TextFormatException

trait ProtoAsciiParsing {
  def fromAscii[T <: GeneratedMessage](
      d: GeneratedMessageCompanion[T],
      s: String
  ): Either[TextFormatError, T] = {
    val p = new Parser(s)
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
