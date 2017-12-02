package com.trueaccord

package object lenses {
  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type Updatable[A] = scalapb.lenses.Updatable[A]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type Lens[Container, A] = scalapb.lenses.Lens[Container, A]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type ObjectLens[U, Container] = scalapb.lenses.ObjectLens[U, Container]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  val Lens = scalapb.lenses.Lens
}