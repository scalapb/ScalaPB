package com.trueaccord.pb

import com.trueaccord.proto.e2e.type_level.XYMessage
import com.trueaccord.scalapb.TypeMapper

case class Point2D(x: Int, y: Int) {
  def toXYMessage = XYMessage().update(_.x := x, _.y := y)
}

object Point2D {
  implicit val typeMapper = TypeMapper[XYMessage, Point2D](
    xy => Point2D(xy.getX, xy.getY))(p => p.toXYMessage)
}
