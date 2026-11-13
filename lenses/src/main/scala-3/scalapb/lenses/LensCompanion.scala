package scalapb.lenses

/** Supplies the conversion to a generated message's lens class.
  *
  * Keeping the polymorphic given in the runtime lets generated sources support both Scala 3.3 and
  * newer compilers' future source mode without repeating forwarding methods for every field.
  */
trait LensCompanion[M, L[_]] {
  protected def makeLens[U](lens: Lens[U, M]): L[U]

  given lensConversion[U]: Conversion[Lens[U, M], L[U]] with {
    def apply(lens: Lens[U, M]): L[U] = makeLens(lens)
  }
}
