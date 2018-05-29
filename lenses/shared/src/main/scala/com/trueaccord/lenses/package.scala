package com.trueaccord

package object lenses {
  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type Updatable[A] = _root_.scalapb.lenses.Updatable[A]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type Lens[Container, A] = _root_.scalapb.lenses.Lens[Container, A]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  type ObjectLens[U, Container] = _root_.scalapb.lenses.ObjectLens[U, Container]

  @deprecated("Use scalapb.lenses package instead of com.trueaccord.lenses", "0.7.0")
  val Lens = _root_.scalapb.lenses.Lens
}
