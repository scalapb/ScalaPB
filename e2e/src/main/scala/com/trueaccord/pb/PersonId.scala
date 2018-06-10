package com.trueaccord.pb

import com.trueaccord.scalapb.TypeMapper
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Name
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Weather

case class PersonId(untypedId: String)

case class Years(number: Int)

case class FullName(firstName: String, lastName: String)

case class WrappedWeather(weather: Weather)

object PersonId {
  implicit val mapper: TypeMapper[String, PersonId] = TypeMapper(PersonId.apply)(_.untypedId)
}

object Years {
  implicit val mapper: TypeMapper[Int, Years] = TypeMapper(Years.apply)(_.number)
}

object FullName {
  implicit val mapper: TypeMapper[Name, FullName] = TypeMapper((n: Name) => FullName(n.getFirst, n.getLast))(fn =>
    Name(first = Some(fn.firstName), last = Some(fn.lastName)))
}

// We import this into the generated code using a file-level option.
object MisplacedMapper {
  implicit val weatherMapper: TypeMapper[Weather, WrappedWeather] = TypeMapper(WrappedWeather.apply)(_.weather)
}

trait DomainEvent {
  def personId: Option[PersonId]
  def optionalNumber: Option[Int]
  def repeatedNumber: Seq[Int]
  def requiredNumber: Int
}

trait DomainEventCompanion {
  val thisIs = "The companion object"
}
