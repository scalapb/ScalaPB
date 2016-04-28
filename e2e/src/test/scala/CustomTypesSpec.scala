import com.trueaccord.proto.e2e.custom_types._
import com.trueaccord.proto.e2e.custom_types.CustomMessage.Weather
import com.trueaccord.proto.e2e.CustomTypes.{CustomMessage => CustomMessageJava}
import com.trueaccord.proto.e2e.CustomTypes.CustomMessage.{Weather => WeatherJava}
import org.scalatest._
import com.trueaccord.pb._

class CustomTypesSpec extends FlatSpec with MustMatchers {

  "CustomMessage" should "serialize and parse" in {
    val message = CustomMessage(
      personId = Some(PersonId("abcd")),
      requiredPersonId = PersonId("required"),
      personIds = Seq(PersonId("p1"), PersonId("p2")),
      age = Some(Years(27)),
      requiredAge = Years(25),
      ages = Seq(Years(3), Years(8), Years(35)),
      name = Some(FullName(firstName = "Foo", lastName = "Bar")),
      requiredName = FullName(firstName = "Owen", lastName = "Money"),
      names = Seq(
        FullName(firstName = "Foo", lastName = "Bar"),
        FullName(firstName = "V1", lastName = "Z2")),
      weather = Some(WrappedWeather(Weather.RAIN)),
      requiredWeather = WrappedWeather(Weather.SUNNY),
      weathers = Seq(WrappedWeather(Weather.RAIN), WrappedWeather(Weather.SUNNY)),
      packedWeathers = Seq(WrappedWeather(Weather.RAIN), WrappedWeather(Weather.RAIN))
    )
    message.getPersonId must be(PersonId("abcd"))
    message.requiredPersonId must be(PersonId("required"))
    message.personIds must be(Seq(PersonId("p1"), PersonId("p2")))
    message.getAge must be(Years(27))
    message.requiredAge must be(Years(25))
    message.ages must be(Seq(Years(3), Years(8), Years(35)))
    message.getName must be(FullName("Foo", "Bar"))
    CustomMessage.parseFrom(message.toByteArray) must be(message)
    CustomMessage.toJavaProto(message).getPersonId must be("abcd")
    CustomMessage.toJavaProto(message).getRequiredPersonId must be("required")
    CustomMessage.toJavaProto(message).getAge must be(27)
    CustomMessage.toJavaProto(message).getRequiredAge must be(25)
    CustomMessage.toJavaProto(message).getName.getFirst must be("Foo")
    CustomMessage.toJavaProto(message).getName.getLast must be("Bar")
  }

  "Custom message types" should "concatenate correctly" in {
    val m1 = {
      val b = CustomMessageJava.newBuilder
      b.getNameBuilder.setFirst("Foo")
      b.setRequiredPersonId("p1")
      b.getRequiredNameBuilder.setFirst("first_req")
      b.setAge(4)
      b.setRequiredAge(1)
      b.setRequiredWeather(WeatherJava.SUNNY)
      b.addPackedWeathers(WeatherJava.SUNNY)
      b.addPackedWeathers(WeatherJava.RAIN)
      b.build
    }
    val m2 = {
      val b = CustomMessageJava.newBuilder
      b.getNameBuilder.setLast("Bar")
      b.setRequiredPersonId("p2")
      b.getRequiredNameBuilder.setLast("last_req")
      b.setAge(5)
      b.setRequiredAge(2)
      b.setRequiredWeather(WeatherJava.RAIN)
      b.addPackedWeathers(WeatherJava.RAIN)
      b.addPackedWeathers(WeatherJava.SUNNY)
      b.build
    }
    val expected = CustomMessage(
      requiredPersonId = PersonId("p2"),
      requiredAge = Years(2),
      requiredName = FullName("first_req", "last_req"),
      requiredWeather = WrappedWeather(Weather.RAIN),
      packedWeathers = Seq(
        WrappedWeather(Weather.SUNNY),
        WrappedWeather(Weather.RAIN),
        WrappedWeather(Weather.RAIN),
        WrappedWeather(Weather.SUNNY)
      )
    )
      .update(
        _.name := FullName("Foo", "Bar"),
        _.age := Years(5)
      )
    val concat = (m1.toByteArray ++ m2.toByteArray)
    CustomMessage.parseFrom(concat) must be(expected)
  }

  "Extended types" should "inherit from marker type" in {
    val t: DomainEvent = CustomerEvent(
      personId = Some(PersonId("123")),
      optionalNumber = Some(1),
      repeatedNumber = Seq(2,3,4),
      requiredNumber = 5)
    t mustBe a [DomainEvent]
    t.personId must be(Some(PersonId("123")))
    t.optionalNumber must be(Some(1))
    t.repeatedNumber must be(Seq(2,3,4))
    t.requiredNumber must be(5)
  }

  "Extended companion objects" should "inherit from marker type" in {
    CustomerEvent mustBe a [DomainEventCompanion]
    CustomerEvent.thisIs must be("The companion object")
  }
}
