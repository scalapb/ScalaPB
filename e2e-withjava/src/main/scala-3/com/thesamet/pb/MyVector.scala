package com.thesamet.pb

import scala.collection._

// IterableOnce is required so our builder has ++= which can take another MyVector
case class MyVector[T](stuff: Vector[T]) extends IterableOnce[T] {
  def ++(s: IterableOnce[T]): MyVector[T] = MyVector(stuff ++ s)

  def foreach[U](f: T => U): Unit = stuff.foreach(f)

  // For IterableOnce
  override def iterator: _root_.scala.collection.Iterator[T] = stuff.iterator

  override def knownSize: Int = stuff.knownSize
}

object MyVector extends IterableFactory[MyVector] {
  override def newBuilder[A]: mutable.Builder[A, MyVector[A]] = new Builder[A]

  override def from[A](source: IterableOnce[A]): MyVector[A] = MyVector(Vector.from(source))

  def empty[T] = new MyVector[T](Vector())

  class Builder[A] extends mutable.Builder[A, MyVector[A]] {
    val underlying = Vector.newBuilder[A]

    override def clear(): Unit = underlying.clear()

    override def result(): _root_.com.thesamet.pb.MyVector[A] = MyVector(underlying.result())

    override def addOne(elem: A): Builder.this.type = {
      underlying.addOne(elem); this
    }
  }
}
