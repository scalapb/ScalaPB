package scalapb

import com.google.protobuf.ByteString

/** A value backed by bytes, with decoding deferred until `value` is first used and then cached.
  *
  * Reading this wrapper or calling `toByteString` does not decode it. String conversions,
  * `toString`, `hashCode`, and equality between wrappers can trigger decoding. Decoding does not
  * discard the stored bytes: generated binary serializers continue to write them unchanged. A
  * decoded field can therefore retain both its bytes and its decoded value.
  *
  * A `LazyField` is equal only to another `LazyField` with an equal decoded value. Compare
  * `field.value` with a plain String explicitly. For example:
  *
  * {{{
  * val name = LazyField.from("Alice")
  * name.value == "Alice"           // true
  * name.equals("Alice")            // false
  * Set[Any](name, "Alice").size     // 2, regardless of insertion order
  * Set[String](name, "Alice").size  // 1, after implicit conversion to String
  * }}}
  *
  * Generated binary parsers apply the configured UTF-8 validation policy before constructing lazy
  * string fields. `LazyField.apply` itself does not validate its bytes; the default String decoder
  * replaces malformed UTF-8 with U+FFFD while preserving the stored bytes. `LazyField.from` encodes
  * its argument immediately and retains the supplied value.
  *
  * @param bytes
  *   the stored bytes, used by generated binary serializers.
  * @param decoder
  *   the decoder used on first access when no original value was supplied.
  * @tparam T
  *   the decoded value type.
  */
final class LazyField[T] private (val bytes: ByteString, decoder: LazyDecoder[T], raw: Option[T]) {
  lazy val value: T = raw.getOrElse(decoder.decode(bytes))

  def toByteString: ByteString = bytes

  override def toString: String = value.toString()
  // LazyField[T] is never equal to a plain value of type T.
  override def equals(other: Any): Boolean = other match {
    // Perform decoding for the purpose of comparing by value.
    // This leads to a loss of laziness in comparisons, but it
    // provides consistency in edge cases with non‑UTF‑8 strings.
    // Example:
    // val a = LazyField.from("\uD800")
    // val b = LazyField[String](a.bytes)
    // a != b                   <--- true
    // a.hashCode != b.hashCode <--- true
    case that: LazyField[?] => this.value == that.value
    // LazyField can be compared with other LazyField only
    case _ => false
  }
  override def hashCode(): Int = value.hashCode()
}

object LazyField extends LazyFieldCompat {
  def apply[T](bytes: ByteString)(implicit decoder: LazyDecoder[T]): LazyField[T] =
    new LazyField(bytes, decoder, None)

  def from[T](raw: T)(implicit decoder: LazyDecoder[T], encoder: LazyEncoder[T]): LazyField[T] =
    new LazyField(encoder.encode(raw), decoder, Some(raw))

  implicit val lazyStringMapper: TypeMapper[ByteString, LazyField[String]] =
    TypeMapper[ByteString, LazyField[String]](LazyField.apply[String])(_.toByteString)

  implicit val lazyStringMapperFromString: TypeMapper[String, LazyField[String]] =
    TypeMapper[String, LazyField[String]](LazyField.from)(_.toString)
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
