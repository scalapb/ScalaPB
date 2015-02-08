package com.trueaccord.pb

import com.trueaccord.scalapb.TypeMapper

case class PersonId(untypedId: String)

case class Years(number: Int)

case class Name(firstName: String, lastName: String)

object PersonId {
  implicit val mapper = TypeMapper(PersonId.apply)(_.untypedId)
}

object Years {
  implicit val mapper = TypeMapper(Years.apply)(_.number)
}

