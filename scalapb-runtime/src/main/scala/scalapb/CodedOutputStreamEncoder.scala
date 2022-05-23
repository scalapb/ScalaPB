package scalapb

import com.google.protobuf.{CodedInputStream, CodedOutputStream, InvalidProtocolBufferException}

import scala.language.implicitConversions

trait CodedOutputStreamEncoder[T] {
  def serializedSizeNoTag(value: T): Int
  def writeTo(output: CodedOutputStream, value: T): Unit
}

case object CodedOutputStreamEncoder {
  sealed trait Predef
  implicit case object StringCodedOutputStreamEncoder
      extends CodedOutputStreamEncoder[String]
      with Predef {
    override def serializedSizeNoTag(value: String): Int =
      CodedOutputStream.computeStringSizeNoTag(value)

    override def writeTo(output: CodedOutputStream, value: String): Unit =
      output.writeStringNoTag(value)
  }

  implicit def messageCodedOutputStreamEncoder[M <: GeneratedMessage]: CodedOutputStreamEncoder[M] = new CodedOutputStreamEncoder[M] with Predef {
    override def serializedSizeNoTag(value: M): Int = value.serializedSize

    override def writeTo(output: CodedOutputStream, value: M): Unit = value.writeTo(output)
  }
}

trait CodedOutputStreamDecoder[T] {
  final def read(input: CodedInputStream): T = {
    try {
      readFrom(input)
    } catch {
      case e: CodedOutputStreamDecoder.DecodeException => throw e
      case e: InvalidProtocolBufferException =>
        this match {
          case pr: CodedOutputStreamDecoder.Predef =>
            throw CodedOutputStreamDecoder.PredefFailure(pr, e)
          case gen: CodedOutputStreamDecoder.Generated =>
            throw CodedOutputStreamDecoder.GeneratedFailure(gen, e)
          case _ => throw e
        }
    }
  }
  protected def readFrom(input: CodedInputStream): T
}

case object CodedOutputStreamDecoder {
  sealed trait DecodeException { self: InvalidProtocolBufferException => }
  case class PredefFailure(decoder: Predef, cause: Throwable)
      extends InvalidProtocolBufferException(
        s"""ScalaPB predefined decoder <$decoder> failed to parse data from the CodedInputStream.
           |It may be a result of stream truncation or defect in the predefined decoder.
           |If you feel that this is a defect, please, fill the issue to the ScalaPB GitHub page:
           |https://github.com/scalapb/ScalaPB/issues""".stripMargin
      )
      with DecodeException {
    initCause(cause)
  }

  case class GeneratedFailure(decoder: Generated, cause: Throwable)
      extends InvalidProtocolBufferException(
        s"""ScalaPB generated decoder <$decoder> failed to parse data from the CodedInputStream.
           |It may be a result of stream truncation or defect in the generated decoder.
           |If you feel that this is a defect, please, fill the issue to the ScalaPB GitHub page:
           |https://github.com/scalapb/ScalaPB/issues""".stripMargin
      )
      with DecodeException {
    initCause(cause)
  }

  sealed trait Predef { self: Singleton with Serializable => }
  trait Generated

  implicit case object StringDecoder extends CodedOutputStreamDecoder[String] with Predef {
    override def readFrom(input: CodedInputStream): String = input.readStringRequireUtf8()
  }

  implicit def fromTypeMapper[T, U](typeMapper: TypeMapper[T, U])(implicit
      toT: CodedOutputStreamDecoder[T]
  ): CodedOutputStreamDecoder[U] = new CodedOutputStreamDecoder[U] {
    override protected def readFrom(input: CodedInputStream): U =
      typeMapper.toCustom(toT.read(input))
  }
}
