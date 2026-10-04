package scalapb

import com.google.protobuf.ByteString

/** A field that is lazily parsed from a ByteString.
  *
  * Inspired by
  * https://github.com/protocolbuffers/protobuf/blob/main/java/core/src/main/java/com/google/protobuf/LazyField.java
  *
  * Non-commutative equals
  *
  * `equals` on `LazyField[T]` is '''not commutative''':
  *   - `lazyField.equals(plainValue)` returns `true` when the decoded value equals `plainValue`.
  *   - `plainValue.equals(lazyField)` returns `false` because the plain type has no knowledge of `LazyField`.
  *
  * Compiler protection:
  *   - Scala 3 — enable `-language:strictEquality`
  *   - Scala 2 — there is no full compile-time protection, but `-Xfatal-warnings` would be helpful in some cases
  *
  * Any-typed collections problem:
  * In typed collections (`Set[String]`, `Map[String, V]`) this asymmetry is hidden by implicit
  * conversions that resolve `LazyField[T]` to `T` before insertion. With `Any`-typed collections
  * the conversions are bypassed and insertion order silently changes the result:
  *
  * {{{
  * val s:  String             = "hello"
  * val ls: LazyField[String]  = LazyField.from("hello")
  *
  * Set[Any](ls, s).size  // 2 — s.equals(ls) is false, both are kept
  * Set[Any](s, ls).size  // 1 — ls.equals(s)  is true, ls is deduplicated away
  * }}}
  *
  * @param bytes
  *   the raw bytes of the field.
  * @param decoder
  *   the decoder to use to parse the bytes.
  * @tparam T
  *   the type of the field.
  */
final class LazyField[T] private (val bytes: ByteString, decoder: LazyDecoder[T], raw: Option[T]) {
  lazy val value: T = raw.getOrElse(decoder.decode(bytes))

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
