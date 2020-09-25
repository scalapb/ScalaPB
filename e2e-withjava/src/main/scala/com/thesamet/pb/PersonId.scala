package com.thesamet.pb

import scalapb.TypeMapper
import com.thesamet.proto.e2e.custom_types.CustomMessage.Name
import com.thesamet.proto.e2e.custom_types.CustomMessage.Weather
import com.thesamet.proto.e2e.no_box.NameNoBox
import com.thesamet.proto.e2e.custom_types.CustomMessage

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
  implicit val mapper: TypeMapper[Name, FullName] = TypeMapper[Name, FullName](n => FullName(n.getFirst, n.getLast))(fn =>
    Name(
      first = if (fn.firstName != "EMPTY") Some(fn.firstName) else None, 
      last = if (fn.lastName != "EMPTY") Some(fn.lastName) else None,
    ))

  implicit val mapperNoBox: TypeMapper[NameNoBox, FullName] = TypeMapper[NameNoBox, FullName](n => FullName(n.first, n.last))(fn =>
    NameNoBox(first = fn.firstName, last = fn.lastName)
  )
}

// We import this into the generated code using a file-level option.
object MisplacedMapper {
  implicit val weatherMapper: TypeMapper[CustomMessage.Weather,WrappedWeather] = TypeMapper(WrappedWeather.apply)(_.weather)
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
