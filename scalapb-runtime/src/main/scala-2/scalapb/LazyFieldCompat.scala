package scalapb

import scala.language.implicitConversions

trait LazyFieldCompat {
  implicit def lazyFieldToValue[T](lf: LazyField[T]): T = lf.value

  implicit def valueToLazyField[T](value: T)(implicit
      encoder: LazyEncoder[T],
      decoder: LazyDecoder[T]
  ): LazyField[T] =
    LazyField(encoder.encode(value))
}
