package scalapb

import com.google.protobuf.InvalidProtocolBufferException

abstract class CollectionAdapter[T, Coll] {
  type Builder = collection.mutable.Builder[T, Either[InvalidProtocolBufferException, Coll]]

  def foreach(coll: Coll)(f: T => Unit): Unit

  def empty: Coll

  def newBuilder: Builder

  def concat(first: Coll, second: Iterable[T]): Coll

  def fromIterator(x: Iterator[T]): Either[InvalidProtocolBufferException, Coll] =
    (newBuilder ++= x).result()

  def fromIteratorUnsafe(x: Iterator[T]) = fromIterator(x).fold(throw (_), identity(_))

  def toIterator(value: Coll): Iterator[T]

  def size(value: Coll): Int
}
