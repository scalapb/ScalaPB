package scalapb.internal

import scala.collection.Factory

object compat {
  def convertTo[A, To](from: IterableOnce[A])(implicit f: Factory[A, To]): To = {
    from.iterator.to(f)
  }

  val JavaConverters = scala.jdk.CollectionConverters
}
