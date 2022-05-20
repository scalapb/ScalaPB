package scalapb

import com.google.protobuf.ByteString

@FunctionalInterface
trait ByteStringEncoder[T] {
  def apply(value: T): ByteString
}

case object ByteStringEncoder {
  def apply[T: ByteStringEncoder]: ByteStringEncoder[T]             = implicitly

  implicit val stringToByteStringEncoder: ByteStringEncoder[String] = ByteString.copyFromUtf8
}

@FunctionalInterface
trait ByteStringDecoder[T] {
  def apply(bs: ByteString): T
}

case object ByteStringDecoder {
  def apply[T: ByteStringDecoder]: ByteStringDecoder[T] = implicitly

  implicit val stringFromByteStringDecoder: ByteStringDecoder[String] = _.toStringUtf8()
}

trait ByteStringCodec[T] extends ByteStringEncoder[T] with ByteStringDecoder[T]

case object ByteStringCodec {
  implicit def codec[T](implicit
      encoder: ByteStringEncoder[T],
      decoder: ByteStringDecoder[T]
  ): ByteStringCodec[T] = {
    new ByteStringCodec[T] {
      override def apply(value: T): ByteString = encoder(value)

      override def apply(bs: ByteString): T = decoder(bs)
    }
  }
}
