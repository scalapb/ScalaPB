package scalapb.textformat

import scalapb.{GeneratedMessage, GeneratedMessageCompanion, TextFormatError}

trait ProtoAsciiParsing {
  def fromAscii[T <: GeneratedMessage](
      d: GeneratedMessageCompanion[T],
      s: String
  ): Either[TextFormatError, T] = ???
}
