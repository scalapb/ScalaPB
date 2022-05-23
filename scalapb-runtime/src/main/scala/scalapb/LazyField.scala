package scalapb

import com.google.protobuf.{ByteString, CodedOutputStream}

import scala.language.implicitConversions

sealed trait LazyField[+T] {
  def value: T
  private[scalapb] def writeTo(output: CodedOutputStream, fieldNumber: Int): Unit
  private[scalapb] def serializedSizeNoTag: Int
}

case object LazyField {
  final case class Serialized[T](byteString: ByteString)(implicit decoder: ByteStringDecoder[T]) extends LazyField[T] {
    override lazy val value: T = decoder(byteString)

    override private[scalapb] def writeTo(output: CodedOutputStream, fieldNumber: Int): Unit =
      output.writeBytes(fieldNumber, byteString)

    override private[scalapb] def serializedSizeNoTag: Int = CodedOutputStream.computeBytesSizeNoTag(byteString)
  }

  final case class Value[T](value: T)(implicit encoder: CodedOutputStreamEncoder[T]) extends LazyField[T] {
    override private[scalapb] def writeTo(output: CodedOutputStream, fieldNumber: Int): Unit =
      encoder.writeTo(output, value)

    override private[scalapb] def serializedSizeNoTag: Int = encoder.serializedSizeNoTag(value)
  }

  implicit def lazyFieldOptionUnwrap[T](lazyField: Option[LazyField[T]]): Option[T] =
    lazyField.map(_.value)

  implicit def lazyFieldOptionWrap[T: CodedOutputStreamEncoder](option: Option[T]): Option[LazyField[T]] =
    option.map(lazyFieldWrap[T])

  implicit def lazyFieldSeqUnwrap[T](lazyField: Iterable[LazyField[T]]): Iterable[T] =
    lazyField.map(_.value)

  implicit def lazyFieldSeqWrap[T: CodedOutputStreamEncoder](iterable: Iterable[T]): Iterable[LazyField[T]] =
    iterable.map(lazyFieldWrap[T])

  implicit def lazyFieldUnwrap[T](lazyField: LazyField[T]): T = lazyField.value

  implicit def lazyFieldWrap[T: CodedOutputStreamEncoder](value: T): LazyField[T] = Value[T](value)

}
