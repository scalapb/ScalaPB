package scalapb

import com.google.protobuf.ByteString

/** A field that is lazily parsed from a ByteString.
  *
  * Inspired by
  * https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/LazyField.java
  *
  * @param bytes
  *   the raw bytes of the field.
  * @param decoder
  *   the decoder to use to parse the bytes.
  * @tparam T
  *   the type of the field.
  */
final class LazyField[T] private (val bytes: ByteString, decoder: LazyDecoder[T]) {
  lazy val value: T = decoder.decode(bytes)

  def toByteString: ByteString = bytes

  override def toString: String = value.toString()
  // Equality for LazyField[T] is not commutative!
  // It is extremely important to use LazyField[T] only with the `-language:strictEquality` enabled for Scala 3 or `-Xfatal-warnings` for Scala 2.
  override def equals(other: Any): Boolean = other match {
    case that: LazyField[?] => this.bytes == that.bytes
    case _                  => this.value == other
  }
  override def hashCode(): Int = value.hashCode()
}

object LazyField extends LazyFieldCompat {
  def apply[T](bytes: ByteString)(implicit decoder: LazyDecoder[T]): LazyField[T] =
    new LazyField(bytes, decoder)

  implicit val lazyStringMapper: TypeMapper[ByteString, LazyField[String]] =
    TypeMapper[ByteString, LazyField[String]](LazyField.apply[String])(_.toByteString)
}

trait LazyDecoder[T] {
  def decode(bytes: ByteString): T
}

object LazyDecoder {
  implicit val stringDecoder: LazyDecoder[String] = _.toStringUtf8()
}

trait LazyEncoder[T] {
  def encode(value: T): ByteString
}

object LazyEncoder {
  implicit val stringEncoder: LazyEncoder[String] = ByteString.copyFromUtf8(_)
}
