package com.trueaccord.scalapb

import scala.util.Try

object TextFormat {
  def parseFromDescriptor[T <: GeneratedMessage with Message[T]](d: GeneratedMessageCompanion[T], s: String): Try[T] = {
    ???
    /*
  val parser = new TextFormat(s)
  parser.Root(d).run().map(_.asInstanceOf[T]).recoverWith {
    case error: ParseError => Failure[T](TextFormatError(parser.formatError(error)))
  }
  */
  }
}

