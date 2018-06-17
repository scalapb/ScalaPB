package scalapb.lenses

import scala.language.higherKinds
import scala.language.implicitConversions

trait Lens[Container, A] extends Any {
  self =>

  /** get knows how to extract some field of type `A` from a container */
  def get(c: Container): A

  /** Represents an assignment operator.
    *
    * Given a value of type A, sets knows how to transform a container such that `a` is
    * assigned to the field.
    *
    * We must have get(set(a)(c)) == a
    */
  def set(a: A): Mutation[Container]

  /** alias to set */
  def :=(a: A) = set(a)

  /** Represent an update operator (like x.y += 1 ) */
  def modify(f: A => A): Mutation[Container] = c => set(f(get(c)))(c)

  /** Composes two lenses, this enables nesting.
    *
    * If our field of type A has a sub-field of type B, then given a lens for it
    * (other: Lens[A, B]) we can create a single lens from Container to B.
    */
  def compose[B](other: Lens[A, B]): Lens[Container, B] = new Lens[Container, B] {
    def get(c: Container) = other.get(self.get(c))

    def set(b: B) = self.modify(other.set(b))
  }

  /** Given two lenses with the same origin, returns a new lens that can mutate both values
    * represented by both lenses through a tuple.
    */
  def zip[B](other: Lens[Container, B]): Lens[Container, (A, B)] = new Lens[Container, (A, B)] {
    def get(c: Container): (A, B)           = (self.get(c), other.get(c))
    def set(t: (A, B)): Mutation[Container] = self.set(t._1).andThen(other.set(t._2))
  }
}

object Lens {
  /* Create a Lens from getter and setter. */
  def apply[Container, A](
      getter: Container => A
  )(setter: (Container, A) => Container): Lens[Container, A] = new Lens[Container, A] {
    def get(c: Container) = getter(c)

    def set(a: A): Mutation[Container] = setter(_, a)
  }

  /** This is the unit lens, with respect to the compose operation defined above. That is,
    * len.compose(unit) == len == unit.compose(len)
    *
    * More practically, you can view it as a len that mutates the entire object, instead of
    * just a field of it: get() gives the original object, and set() returns the assigned value,
    * no matter what the original value was.
    */
  def unit[U]: Lens[U, U] = Lens(identity[U])((c, v) => v)

  /** Adds some syntactic sugar if our lens watches a Seq-like collection. */
  class SeqLikeLens[U, A, CollA <: collection.SeqLike[A, CollA]](val lens: Lens[U, CollA])
      extends AnyVal {
    type CBF = collection.generic.CanBuildFrom[CollA, A, CollA]

    private def field(getter: CollA => A)(setter: (CollA, A) => CollA): Lens[U, A] =
      lens.compose[A](Lens[CollA, A](getter)(setter))

    def apply(i: Int)(implicit cbf: CBF): Lens[U, A] = field(_.apply(i))((c, v) => c.updated(i, v))

    def head(implicit cbf: CBF): Lens[U, A] = apply(0)

    def last(implicit cbf: CBF): Lens[U, A] = field(_.last)((c, v) => c.updated(c.size - 1, v))

    def :+=(item: A)(implicit cbf: CBF) = lens.modify(_ :+ item)

    def :++=(item: scala.collection.GenTraversableOnce[A])(implicit cbf: CBF) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A])(implicit cbf: CBF): Mutation[U] =
      lens.modify(
        s =>
          s.map { (m: A) =>
            val field: Lens[A, A] = Lens.unit[A]
            val p: Mutation[A]    = f(field)
            p(m)
          }
      )
  }

  implicit def seqLikeLens[U, A, Coll[A] <: collection.SeqLike[A, Coll[A]]](
      x: Lens[U, Coll[A]]
  ): SeqLikeLens[U, A, Coll[A]] = new SeqLikeLens[U, A, Coll[A]](x)

  // Needed for Dotty, see https://github.com/lampepfl/dotty/issues/4647
  implicit def seqLikeLensDotty[U, A, CA <: collection.SeqLike[A, CA]](
    x: Lens[U, CA]
  ): SeqLikeLens[U, A, CA] = new SeqLikeLens[U, A, CA](x)

  /** Implicit that adds some syntactic sugar if our lens watches a Set-like collection. */
  implicit class SetLikeLens[U, A, Coll[A] <: collection.SetLike[A, Coll[A]] with Set[A]](
      val lens: Lens[U, Coll[A]]
  ) extends AnyVal {
    type CBF = collection.generic.CanBuildFrom[Coll[A], A, Coll[A]]

    private def field(getter: Coll[A] => A)(setter: (Coll[A], A) => Coll[A]): Lens[U, A] =
      lens.compose[A](Lens[Coll[A], A](getter)(setter))

    def :+=(item: A)(implicit cbf: CBF) = lens.modify(_ + item)

    def :++=(item: scala.collection.GenTraversableOnce[A])(implicit cbf: CBF) =
      lens.modify(_ ++ item)

    def foreach(f: Lens[A, A] => Mutation[A])(implicit cbf: CBF): Mutation[U] =
      lens.modify(
        s =>
          s.map { (m: A) =>
            val field: Lens[A, A] = Lens.unit[A]
            val p: Mutation[A]    = f(field)
            p(m)
          }
      )
  }

  /** Implicit that adds some syntactic sugar if our lens watches an Option[_]. */
  implicit class OptLens[U, A](val lens: Lens[U, Option[A]]) extends AnyVal {
    def inplaceMap(f: Lens[A, A] => Mutation[A]) =
      lens.modify(
        opt =>
          opt.map { (m: A) =>
            val field: Lens[A, A] = Lens.unit[A]
            val p: Mutation[A]    = f(field)
            p(m)
          }
      )
  }

  /** Implicit that adds some syntactic sugar if our lens watches a Map[_, _]. */
  implicit class MapLens[U, A, B](val lens: Lens[U, Map[A, B]]) extends AnyVal {
    def apply(key: A): Lens[U, B] =
      lens.compose(Lens[Map[A, B], B](_.apply(key))((map, value) => map.updated(key, value)))

    def :+=(pair: (A, B)) = lens.modify(_ + pair)

    def :++=(item: Iterable[(A, B)]) = lens.modify(_ ++ item)

    def foreach(f: Lens[(A, B), (A, B)] => Mutation[(A, B)]): Mutation[U] =
      lens.modify(
        s =>
          s.map { (pair: (A, B)) =>
            val field: Lens[(A, B), (A, B)] = Lens.unit[(A, B)]
            val p: Mutation[(A, B)]         = f(field)
            p(pair)
          }
      )

    def foreachValue(f: Lens[B, B] => Mutation[B]): Mutation[U] =
      lens.modify(
        s =>
          s.mapValues { (m: B) =>
            val field: Lens[B, B] = Lens.unit[B]
            val p: Mutation[B]    = f(field)
            p(m)
          }
      )

    def mapValues(f: B => B) = foreachValue(_.modify(f))
  }
}

/** Represents a lens that has sub-lenses. */
class ObjectLens[U, Container](self: Lens[U, Container]) extends Lens[U, Container] {

  /** Creates a sub-lens */
  def field[A](lens: Lens[Container, A]): Lens[U, A] = self.compose(lens)

  /** Creates a sub-lens */
  def field[A](getter: Container => A)(setter: (Container, A) => Container): Lens[U, A] =
    field(Lens(getter)(setter))

  override def get(u: U) = self.get(u)

  override def set(c: Container) = self.set(c)

  def update(ms: (Lens[Container, Container] => Mutation[Container])*): Mutation[U] =
    u => set(ms.foldLeft[Container](get(u))((p, m) => m(Lens.unit[Container])(p)))(u)
}

trait Updatable[A] extends Any {
  self: A =>
  def update(ms: (Lens[A, A] => Mutation[A])*): A =
    ms.foldLeft[A](self)((p, m) => m(Lens.unit[A])(p))
}
