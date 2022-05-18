package com.thesamet.pb

import scala.collection.{MapFactory, mutable}

case class MyMap[K, V](stuff: Map[K, V]) extends Iterable[(K, V)] {
  override def foreach[U](f: ((K, V)) => U): Unit = stuff.foreach(f)

  def ++(s: Iterable[(K, V)]): MyMap[K, V] = MyMap(stuff ++ s)

  override def map[B](f: ((K, V)) => B): Iterable[B] = stuff.map(f)

  override def iterator: _root_.scala.collection.Iterator[(K, V)] = stuff.iterator
}

object MyMap extends MapFactory[MyMap] {
  def empty[K, V] = new MyMap[K, V](Map.empty)

  class Builder[K, V] extends mutable.Builder[(K, V), MyMap[K, V]] {
    private val underlying = Map.newBuilder[K, V]

    override def clear(): Unit = underlying.clear()

    override def result(): MyMap[K, V] = MyMap(underlying.result())

    override def addOne(elem: (K, V)): Builder.this.type = { underlying += elem; this }
  }

  def newBuilder[K, V] = new Builder[K, V]

  override def from[K, V](it: _root_.scala.collection.IterableOnce[(K, V)]): MyMap[K, V] = MyMap(
    Map.from(it)
  )
}
