package scalapb

import scala.collection.generic.CanBuildFrom

object internal {
  def convertTo[A, To](from: TraversableOnce[A])(implicit cbf: CanBuildFrom[Nothing, A, To]): To = {
    val builder = cbf()
    builder ++= from
    builder.result()
  }

  val JavaConverters = scala.collection.JavaConverters
}