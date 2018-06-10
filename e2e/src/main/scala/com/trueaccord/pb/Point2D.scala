package com.trueaccord.pb

import com.trueaccord.proto.e2e.type_level.{XYMessage, DirEnum}
import com.trueaccord.scalapb.TypeMapper

case class Point2D(x: Int, y: Int) {
  def toXYMessage = XYMessage().update(_.x := x, _.y := y)
}

object Point2D {
  implicit val typeMapper: TypeMapper[XYMessage, Point2D] = TypeMapper(
    (xy: XYMessage) => Point2D(xy.getX, xy.getY))(p => p.toXYMessage)
}

case class Dir2D(i: Int)

object Dir2D {
  implicit val typeMapper: TypeMapper[DirEnum, Dir2D] = TypeMapper(
    (de: DirEnum) => Dir2D(de.value))(p => DirEnum.fromValue(p.i))
}
