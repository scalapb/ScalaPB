package com.thesamet.pb

import cats.data.{NonEmptyList, NonEmptySet, NonEmptyMap}
import scala.collection.immutable.SortedSet
import scala.collection.immutable.SortedMap
import com.google.protobuf.InvalidProtocolBufferException
import scalapb.CollectionAdapter

class NonEmptyListAdapter[T] extends CollectionAdapter[T, NonEmptyList[T]] {
  def foreach(coll: NonEmptyList[T])(f: T => Unit) = { coll.map(f); () }

  def empty: NonEmptyList[T] =
    throw new InvalidProtocolBufferException(
      "No empty instance available for cats.Data.NonEmptyList"
    )

  def newBuilder: Builder =
    List
      .newBuilder[T]
      .mapResult(list =>
        NonEmptyList
          .fromList(list)
          .toRight(new InvalidProtocolBufferException("Could not build an empty NonEmptyList"))
      )

  def concat(first: NonEmptyList[T], second: Iterable[T]) = first ++ second.toList

  def toIterator(value: NonEmptyList[T]): Iterator[T] = value.iterator

  def size(value: NonEmptyList[T]): Int = value.size
}

object NonEmptyListAdapter {
  def apply[T](): NonEmptyListAdapter[T] = new NonEmptyListAdapter[T]
}

class NonEmptySetAdapter[T: Ordering] extends CollectionAdapter[T, NonEmptySet[T]] {
  def foreach(coll: NonEmptySet[T])(f: T => Unit) = { coll.map(f); {} }

  def empty: NonEmptySet[T] =
    throw new InvalidProtocolBufferException(
      "No empty instance available for cats.Data.NonEmptyList"
    )

  def newBuilder: Builder =
    SortedSet
      .newBuilder[T]
      .mapResult(set =>
        NonEmptySet
          .fromSet(set)
          .toRight(new InvalidProtocolBufferException("Could not build an empty NonEmptySet"))
      )

  def concat(first: NonEmptySet[T], second: Iterable[T]) =
    throw new InvalidProtocolBufferException(
      "No empty instance available for cats.Data.NonEmptyList"
    )

  def toIterator(value: NonEmptySet[T]): Iterator[T] = value.toSortedSet.iterator

  def size(value: NonEmptySet[T]): Int = value.length
}

object NonEmptySetAdapter {
  def apply[T: Ordering](): NonEmptySetAdapter[T] = new NonEmptySetAdapter[T]
}

class NonEmptyMapAdapter[K: Ordering, V] extends CollectionAdapter[(K, V), NonEmptyMap[K, V]] {
  def foreach(coll: NonEmptyMap[K, V])(f: ((K, V)) => Unit) = coll.toSortedMap.foreach(f)

  def empty: NonEmptyMap[K, V] =
    throw new InvalidProtocolBufferException(
      "No empty instance available for cats.Data.NonEmptyList"
    )

  def newBuilder: Builder =
    SortedMap
      .newBuilder[K, V]
      .mapResult(map =>
        NonEmptyMap
          .fromMap(map)
          .toRight(new InvalidProtocolBufferException("Could not build an empty NonEmptyMap"))
      )

  def concat(first: NonEmptyMap[K, V], second: Iterable[(K, V)]) =
    NonEmptyMap.fromMapUnsafe(first.toSortedMap ++ second.toMap)

  def toIterator(value: NonEmptyMap[K, V]): Iterator[(K, V)] = value.toSortedMap.iterator

  def size(value: NonEmptyMap[K, V]): Int = value.length
}

object NonEmptyMapAdapter {
  def apply[K: Ordering, V](): NonEmptyMapAdapter[K, V] =
    new NonEmptyMapAdapter[K, V]
}
