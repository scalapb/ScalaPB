package com.thesamet.pb

import closed.enums.closed_enums.ClosedEnums
import scalapb.TypeMapper

case class KnownClosedEnum(value: ClosedEnums.Value)
object KnownClosedEnum {
  implicit val mapper: TypeMapper[ClosedEnums.Value, KnownClosedEnum] = TypeMapper {
    value: ClosedEnums.Value =>
      require(!value.isUnrecognized, "Unknown values must not reach the type mapper")
      KnownClosedEnum(value)
  }(_.value)
}
