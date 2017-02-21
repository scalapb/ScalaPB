package com.trueaccord.pb

import scala.collection.mutable
import scala.collection.generic.CanBuildFrom

case class MyVector[T](stuff: Vector[T]) {
  def foreach[U](f: T => U): Unit = stuff.foreach(f)

  def map[B, That](f: T => B)(implicit cbf: CanBuildFrom[MyVector[T], B, That]): That = {
    val b = cbf.apply()
    stuff.foreach(x => b += f(x))
    b.result()
  }

  def ++(s: TraversableOnce[T]): MyVector[T] = MyVector(stuff ++ s)
}

object MyVector {
  def empty[T] = new MyVector[T](Vector())

  implicit def cbf[From, T](implicit vcbf: CanBuildFrom[From, T, Vector[T]]): CanBuildFrom[From, T, MyVector[T]] =
    new CanBuildFrom[From, T, MyVector[T]] {
      override def apply(from: From): mutable.Builder[T, MyVector[T]] =
        vcbf(from).mapResult(MyVector(_))

      override def apply(): mutable.Builder[T, MyVector[T]] = vcbf().mapResult(MyVector(_))
    }

  class Builder[T] {
    private val underlying = Vector.newBuilder[T]

    def ++=(other: MyVector[T]): Builder[T] = {
      underlying ++= other.stuff;
      this
    }

    def +=(other: T): Unit = underlying += other

    def result(): MyVector[T] = MyVector(underlying.result())
  }

  def newBuilder[T] = new Builder[T]
}
