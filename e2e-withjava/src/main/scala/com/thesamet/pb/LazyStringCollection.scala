package com.thesamet.pb

// Deliberately has no iterator method: generated code must use the adapter.
case class LazyStringCollection[T](values: Vector[T])

class LazyStringCollectionAdapter[T] extends scalapb.CollectionAdapter[T, LazyStringCollection[T]] {
  def foreach(c: LazyStringCollection[T])(f: T => Unit): Unit = c.values.foreach(f)
  def empty: LazyStringCollection[T] = LazyStringCollection(Vector.empty[T])
  def newBuilder: Builder = Vector.newBuilder[T].mapResult(v => Right(LazyStringCollection(v)))
  def concat(c: LazyStringCollection[T], xs: Iterable[T]): LazyStringCollection[T] =
    LazyStringCollection(c.values ++ xs)
  def toIterator(c: LazyStringCollection[T]): Iterator[T] = c.values.iterator
  def size(c: LazyStringCollection[T]): Int               = c.values.size
}
object LazyStringCollectionAdapter {
  def apply[T](): LazyStringCollectionAdapter[T] = new LazyStringCollectionAdapter[T]
}
