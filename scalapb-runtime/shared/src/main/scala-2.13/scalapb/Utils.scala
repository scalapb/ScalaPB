package scalapb
import scala.collection.Factory

object Utils {
  def convertTo[A, To](from: IterableOnce[A])(implicit f: Factory[A, To]): To = {
    from.iterator.to(f)
  }
}
