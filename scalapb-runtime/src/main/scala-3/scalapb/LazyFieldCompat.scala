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

  given [T](using
      encoder: LazyEncoder[T],
      decoder: LazyDecoder[T]
  ): Conversion[Seq[T], Seq[LazyField[T]]] =
    (lfs: Seq[T]) => lfs.map(value => LazyField(encoder.encode(value)))

  given [T]: Conversion[Seq[LazyField[T]], Seq[T]] = _.map(_.value)

  given mapKeyToLazyField[K, V](using
      encoder: LazyEncoder[K],
      decoder: LazyDecoder[K]
  ): Conversion[Map[K, V], Map[LazyField[K], V]] = _.map { case (key, value) =>
    LazyField(encoder.encode(key)) -> value
  }
  given mapValueToLazyField[K, V](using
      encoder: LazyEncoder[V],
      decoder: LazyDecoder[V]
  ): Conversion[Map[K, V], Map[K, LazyField[V]]] = _.map { case (key, value) =>
    key -> LazyField(encoder.encode(value))
  }

  given mapKeyToValue[K, V]: Conversion[Map[LazyField[K], V], Map[K, V]] = _.map {
    case (key, value) => key.value -> value
  }
  given mapValueToValue[K, V]: Conversion[Map[K, LazyField[V]], Map[K, V]] = _.map {
    case (key, value) => key -> value.value
  }

  given [T]: CanEqual[LazyField[T], T] = CanEqual.derived
}
