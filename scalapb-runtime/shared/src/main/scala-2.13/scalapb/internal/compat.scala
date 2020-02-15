package scalapb.internal

import scala.collection.Factory

object compat {
  def convertTo[A, To](from: IterableOnce[A])(implicit f: Factory[A, To]): To = {
    from.iterator.to(f)
  }

  def toIterable[A](it: Iterator[A]): Iterable[A] = {
    it.to(Iterable)
  }

  val JavaConverters = scala.jdk.CollectionConverters
}
