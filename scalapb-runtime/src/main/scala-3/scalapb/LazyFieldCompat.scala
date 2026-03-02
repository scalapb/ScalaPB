package scalapb

import com.google.protobuf.ByteString
import scala.language.implicitConversions

trait LazyFieldCompat {
  given [T]: Conversion[LazyField[T], T] = _.value

  given [T](using
      encoder: LazyEncoder[T],
      decoder: LazyDecoder[T]
  ): Conversion[T, LazyField[T]] =
    (value: T) => LazyField(encoder.encode(value))

  given [T]: CanEqual[LazyField[T], T] = CanEqual.derived
}
