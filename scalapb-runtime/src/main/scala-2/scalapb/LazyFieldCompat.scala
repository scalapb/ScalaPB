package scalapb

import scala.language.implicitConversions

trait LazyFieldCompat {
  implicit def lazyFieldToValue[T](lf: LazyField[T]): T = lf.value

  implicit def valueToLazyField[T](value: T)(implicit
      encoder: LazyEncoder[T],
      decoder: LazyDecoder[T]
  ): LazyField[T] =
    LazyField(encoder.encode(value))

  implicit def seqValueToLazyField[T: LazyDecoder](lfs: Seq[T])(implicit
      encoder: LazyEncoder[T]
  ): Seq[LazyField[T]] = lfs.map(value => LazyField(encoder.encode(value)))
  implicit def seqLazyFieldToValue[T](lfs: Seq[LazyField[T]]): Seq[T] = lfs.map(_.value)

  implicit def mapKeyToLazyField[K: LazyDecoder, V](lfm: Map[K, V])(implicit
      encoder: LazyEncoder[K]
  ): Map[LazyField[K], V] = lfm.map { case (key, value) => LazyField(encoder.encode(key)) -> value }
  implicit def mapValueToLazyField[K, V: LazyDecoder](lfm: Map[K, V])(implicit
      encoder: LazyEncoder[V]
  ): Map[K, LazyField[V]] = lfm.map { case (key, value) => key -> LazyField(encoder.encode(value)) }

  implicit def mapKeyToValue[K, V](lfs: Map[LazyField[K], V]): Map[K, V] = lfs.map {
    case (key, value) => key.value -> value
  }
  implicit def mapValueToValue[K, V](lfs: Map[K, LazyField[V]]): Map[K, V] = lfs.map {
    case (key, value) => key -> value.value
  }
}
