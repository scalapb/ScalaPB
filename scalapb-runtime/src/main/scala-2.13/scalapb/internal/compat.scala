package scalapb.internal

import scala.collection.Factory
import scala.collection.mutable.Builder

object compat {
  def convertTo[A, To](from: IterableOnce[A])(implicit f: Factory[A, To]): To = {
    from.iterator.to(f)
  }

  def toIterable[A](it: Iterator[A]): Iterable[A] = {
    it.to(Iterable)
  }

  def newBuilder[A, Coll](implicit f: Factory[A, Coll]): Builder[A, Coll] = f.newBuilder

  val JavaConverters = scala.jdk.CollectionConverters
}
