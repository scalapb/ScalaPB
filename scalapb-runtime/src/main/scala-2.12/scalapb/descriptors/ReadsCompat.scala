package scalapb.descriptors

import scala.language.higherKinds
import scala.collection.generic.CanBuildFrom

trait ReadsCompat {
  implicit def repeated[A, CC[_]](implicit
      reads: Reads[A],
      cbf: CanBuildFrom[Nothing, A, CC[A]]
  ): Reads[CC[A]] = Reads[CC[A]] {
    case PRepeated(value) => value.map(reads.read)(scala.collection.breakOut)
    case _                => throw new ReadsException("Expected PRepeated")
  }
}
