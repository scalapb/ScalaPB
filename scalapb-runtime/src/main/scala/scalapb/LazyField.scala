package scalapb

import com.google.protobuf.ByteString
import scala.language.implicitConversions
import scalapb.lenses.Lens

sealed trait LazyField[T] {
  def byteString: ByteString
  def value: T
}

case object LazyField {
  final case class Serialized[T: ByteStringDecoder](byteString: ByteString) extends LazyField[T] {
    final override lazy val value: T = ByteStringDecoder[T].apply(byteString)
  }

  final case class Value[T: ByteStringEncoder](value: T) extends LazyField[T] {
    final override lazy val byteString: ByteString = ByteStringEncoder[T].apply(value)
  }

  implicit def lazyFieldOptionUnwrap[T](lazyField: Option[LazyField[T]]): Option[T] =
    lazyField.map(_.value)

  implicit def lazyFieldOptionWrap[T: ByteStringEncoder](option: Option[T]): Option[LazyField[T]] =
    option.map(lazyFieldWrap[T])

  implicit def lazyFieldUnwrap[T](lazyField: LazyField[T]): T = lazyField.value

  implicit def lazyFieldWrap[T: ByteStringEncoder](value: T): LazyField[T] = Value[T](value)

  implicit def lazyLens[Container, T: ByteStringEncoder](
      lens: Lens[Container, T]
  ): Lens[Container, LazyField[T]] =
    lens.compose(Lens[T, LazyField[T]](Value(_))((_, l) => l.value))
}
