package scalapb

import com.google.protobuf.InvalidProtocolBufferException
import scala.collection.compat.Factory

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

object CollectionAdapter {
  implicit def forIterable[T, Coll <: Iterable[T]](implicit
      cf: Factory[T, Coll]
  ): CollectionAdapter[T, Coll] =
    new CollectionAdapter[T, Coll] {
      def concat(first: Coll, second: Iterable[T]): Coll =
        internal.compat.convertTo(first.iterator ++ second)
      def empty: Coll                             = internal.compat.newBuilder[T, Coll].result()
      def foreach(coll: Coll)(f: T => Unit): Unit = coll.iterator.foreach(f)
      def newBuilder: scala.collection.mutable.Builder[T, scala.util.Either[
        com.google.protobuf.InvalidProtocolBufferException,
        Coll
      ]]                         = internal.compat.newBuilder[T, Coll].mapResult(Right(_))
      def size(value: Coll): Int = value.size
      def toIterator(value: Coll): Iterator[T] = value.iterator
    }
}
