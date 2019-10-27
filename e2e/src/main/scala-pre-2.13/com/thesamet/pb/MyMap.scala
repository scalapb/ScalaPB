package com.thesamet.pb

import scala.collection.mutable
import scala.collection.generic.CanBuildFrom

case class MyMap[K, V](stuff: Map[K, V]) {
  def foreach[U](f: ((K, V)) => U): Unit = stuff.foreach(f)

  def map[B, That](f: ((K, V)) => B)(implicit cbf: CanBuildFrom[MyMap[K, V], B, That]): That = {
    val b = cbf.apply()
    stuff.foreach(x => b += f(x))
    b.result()
  }

  def ++(s: TraversableOnce[(K, V)]): MyMap[K, V] = MyMap(stuff ++ s)

  def iterator: Iterator[(K, V)] = stuff.iterator
}

object MyMap {
  def empty[K, V] = new MyMap[K, V](Map.empty)

  implicit def cbf[From, K, V](
      implicit vcbf: CanBuildFrom[From, (K, V), Map[K, V]]
  ): CanBuildFrom[From, (K, V), MyMap[K, V]] =
    new CanBuildFrom[From, (K, V), MyMap[K, V]] {
      override def apply(from: From): mutable.Builder[(K, V), MyMap[K, V]] =
        vcbf(from).mapResult(MyMap(_))

      override def apply(): mutable.Builder[(K, V), MyMap[K, V]] = vcbf().mapResult(MyMap(_))
    }

  class Builder[K, V] {
    private val underlying = Map.newBuilder[K, V]

    def ++=(other: MyMap[K, V]): Builder[K, V] = {
      underlying ++= other.stuff;
      this
    }

    def +=(other: (K, V)): Unit = underlying += other

    def result(): MyMap[K, V] = MyMap(underlying.result())
  }

  def newBuilder[K, V] = new Builder[K, V]
}
