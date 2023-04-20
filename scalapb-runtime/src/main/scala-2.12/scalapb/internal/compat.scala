package scalapb.internal

import scala.collection.generic.CanBuildFrom
import scala.collection.compat.Factory
import scala.collection.mutable.Builder

object compat {
  def convertTo[A, To](from: TraversableOnce[A])(implicit cbf: CanBuildFrom[Nothing, A, To]): To = {
    val builder = cbf()
    builder ++= from
    builder.result()
  }

  def toIterable[A](it: Iterator[A]): Iterable[A] = {
    it.toIterable
  }

  def newBuilder[A, Coll](implicit f: Factory[A, Coll]): Builder[A, Coll] = f()

  val JavaConverters = scala.collection.JavaConverters
}
