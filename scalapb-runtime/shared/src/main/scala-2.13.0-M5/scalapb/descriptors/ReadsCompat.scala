package scalapb.descriptors

import collection.Factory
import scala.language.higherKinds

trait ReadsCompat {
  implicit def repeated[A, CC[_]](
      implicit reads: Reads[A],
      factory: Factory[A, CC[A]]
  ): Reads[CC[A]] = Reads[CC[A]] {
    case PRepeated(value) => value.iterator.map(reads.read).to(factory)
    case _                => throw new ReadsException("Expected PRepeated")
  }
}
