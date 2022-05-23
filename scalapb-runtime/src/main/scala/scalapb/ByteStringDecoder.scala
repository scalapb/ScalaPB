package scalapb

import com.google.protobuf.ByteString

@FunctionalInterface
trait ByteStringDecoder[T] {
  def apply(bs: ByteString): T
}

case object ByteStringDecoder {
  def apply[T: ByteStringDecoder]: ByteStringDecoder[T] = implicitly

  implicit val stringFromByteStringDecoder: ByteStringDecoder[String] = _.toStringUtf8()
}
