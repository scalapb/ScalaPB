package scalapb.lenses
import scala.collection.SeqOps
import scala.language.implicitConversions

object CompatLensImplicits {

  /** Implicit that adds some syntactic sugar if our lens watches a Seq-like collection. */
  class SeqLikeLens[U, A, CC[A] <: SeqOps[A, CC, CC[A]]](val lens: Lens[U, CC[A]]) extends AnyVal {
    private def field(getter: CC[A] => A)(setter: (CC[A], A) => CC[A]): Lens[U, A] =
      lens.compose[A](Lens[CC[A], A](getter)(setter))

    def apply(i: Int): Lens[U, A] =
      field(_.apply(i))((c, v) => c.updated(i, v))

    def head: Lens[U, A] = apply(0)

    def last: Lens[U, A] =
      field(_.last)((c, v) => c.updated(c.size - 1, v))

    def :+=(item: A) = lens.modify(_ :+ item)

    def :++=(item: IterableOnce[A]) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A]): Mutation[U] =
      lens.modify(s =>
        s.map { (m: A) =>
          val field: Lens[A, A] = Lens.unit[A]
          val p: Mutation[A]    = f(field)
          p(m)
        }
      )
  }

  /** Implicit that adds some syntactic sugar if our lens watches a Set-like collection. */
  class SetLens[U, A, CC[A] <: collection.immutable.SetOps[A, CC, CC[A]]](val lens: Lens[U, CC[A]])
      extends AnyVal {
    def :+=(item: A) = lens.modify(_ + item)

    def :++=(item: scala.collection.IterableOnce[A]) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A]): Mutation[U] =
      lens.modify(s =>
        s.map { (m: A) =>
          val field: Lens[A, A] = Lens.unit[A]
          val p: Mutation[A]    = f(field)
          p(m)
        }
      )
  }
}

trait CompatLensImplicits {
  import CompatLensImplicits._

  implicit def seqLikeLens[U, A, CC[A] <: SeqOps[A, CC, CC[A]]](
      lens: Lens[U, CC[A]]
  ): SeqLikeLens[U, A, CC] =
    new SeqLikeLens(lens)

  implicit def setLens[U, A, CC[A] <: collection.immutable.SetOps[A, CC, CC[A]]](
      lens: Lens[U, CC[A]]
  ): SetLens[U, A, CC] =
    new SetLens(lens)
}
