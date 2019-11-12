package com.thesamet.pb

import com.thesamet.proto.e2e.type_level.{XYMessage, DirEnum}
import scalapb.TypeMapper

case class Point2D(x: Int, y: Int) {
  def toXYMessage = XYMessage().update(_.x := x, _.y := y)
}

object Point2D {
  implicit val typeMapper =
    TypeMapper[XYMessage, Point2D](xy => Point2D(xy.getX, xy.getY))(p => p.toXYMessage)
}

case class Dir2D(i: Int)

object Dir2D {
  implicit val typeMapper =
    TypeMapper[DirEnum, Dir2D](de => Dir2D(de.value))(p => DirEnum.fromValue(p.i))
}
