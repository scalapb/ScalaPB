package com.trueaccord.pb

import com.trueaccord.scalapb.TypeMapper
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Name

case class PersonId(untypedId: String)

case class Years(number: Int)

case class FullName(firstName: String, lastName: String)

object PersonId {
  implicit val mapper = TypeMapper(PersonId.apply)(_.untypedId)
}

object Years {
  implicit val mapper = TypeMapper(Years.apply)(_.number)
}

object FullName {
  implicit val mapper = TypeMapper[Name, FullName](n => FullName(n.getFirst, n.getLast))(fn =>
    Name(first = Some(fn.firstName), last = Some(fn.lastName)))
}
