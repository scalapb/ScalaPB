package scalapb.lenses

object CompatLensImplicits {

  /** Implicit that adds some syntactic sugar if our lens watches a Seq-like collection. */
  class SeqLikeLens[U, A, Coll[A] <: collection.SeqLike[A, Coll[A]]](
      val lens: Lens[U, Coll[A]]
  ) extends AnyVal {
    type CBF = collection.generic.CanBuildFrom[Coll[A], A, Coll[A]]

    private def field(getter: Coll[A] => A)(setter: (Coll[A], A) => Coll[A]): Lens[U, A] =
      lens.compose[A](Lens[Coll[A], A](getter)(setter))

    def apply(i: Int)(implicit cbf: CBF): Lens[U, A] = field(_.apply(i))((c, v) => c.updated(i, v))

    def head(implicit cbf: CBF): Lens[U, A] = apply(0)

    def last(implicit cbf: CBF): Lens[U, A] = field(_.last)((c, v) => c.updated(c.size - 1, v))

    def :+=(item: A)(implicit cbf: CBF) = lens.modify(_ :+ item)

    def :++=(item: scala.collection.GenTraversableOnce[A])(implicit cbf: CBF) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A])(implicit cbf: CBF): Mutation[U] =
      lens.modify(s =>
        s.map { (m: A) =>
          val field: Lens[A, A] = Lens.unit[A]
          val p: Mutation[A]    = f(field)
          p(m)
        }
      )
  }

  /** Implicit that adds some syntactic sugar if our lens watches a Set-like collection. */
  class SetLikeLens[U, A, Coll[A] <: collection.SetLike[A, Coll[A]] with Set[A]](
      val lens: Lens[U, Coll[A]]
  ) extends AnyVal {
    type CBF = collection.generic.CanBuildFrom[Coll[A], A, Coll[A]]

    private def field(getter: Coll[A] => A)(setter: (Coll[A], A) => Coll[A]): Lens[U, A] =
      lens.compose[A](Lens[Coll[A], A](getter)(setter))

    def :+=(item: A)(implicit cbf: CBF) = lens.modify(_ + item)

    def :++=(item: scala.collection.GenTraversableOnce[A])(implicit cbf: CBF) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A])(implicit cbf: CBF): Mutation[U] =
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
  implicit def seqLikeLens[U, A, Coll[A] <: collection.SeqLike[A, Coll[A]]](
      lens: Lens[U, Coll[A]]
  ) =
    new SeqLikeLens[U, A, Coll](lens)

  implicit def SetLikeLens[U, A, Coll[A] <: collection.SetLike[A, Coll[A]] with Set[A]](
      lens: Lens[U, Coll[A]]
  ) = new SetLikeLens[U, A, Coll](lens)
}
