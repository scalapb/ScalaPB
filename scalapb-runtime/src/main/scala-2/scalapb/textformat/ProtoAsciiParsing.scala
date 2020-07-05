package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TextFormatError}
import fastparse._

trait ProtoAsciiParsing {
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
}
