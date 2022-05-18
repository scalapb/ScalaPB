import com.thesamet.proto.e2e.custom_types._
import com.thesamet.proto.e2e.custom_types.CustomMessage.Weather
import com.thesamet.pb._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CustomTypesSpec extends AnyFlatSpec with Matchers {
  import CustomTypesSpec.Message

  "CustomMessage" should "serialize and parse" in {
    Message.getPersonId must be(PersonId("abcd"))
    Message.requiredPersonId must be(PersonId("required"))
    Message.personIds must be(Seq(PersonId("p1"), PersonId("p2")))
    Message.getAge must be(Years(27))
    Message.requiredAge must be(Years(25))
    Message.ages must be(Seq(Years(3), Years(8), Years(35)))
    Message.getName must be(FullName("Foo", "Bar"))
  }

  "Custom message types" should "concatenate correctly" in {
    val m1 = CustomMessage(
      name = Some(FullName("Foo", "EMPTY")),
      requiredPersonId = PersonId("p1"),
      requiredName = FullName("first_req", "EMPTY"),
      age = Some(Years(4)),
      requiredAge = Years(1),
      requiredWeather = WrappedWeather(Weather.SUNNY),
      packedWeathers = Seq(
        WrappedWeather(Weather.SUNNY),
        WrappedWeather(Weather.RAIN)
      )
    )
    val m2 = CustomMessage(
      name = Some(FullName("EMPTY", "Bar")),
      requiredPersonId = PersonId("p2"),
      requiredName = FullName("EMPTY", "last_req"),
      age = Some(Years(5)),
      requiredAge = Years(2),
      requiredWeather = WrappedWeather(Weather.RAIN),
      packedWeathers = Seq(
        WrappedWeather(Weather.RAIN),
        WrappedWeather(Weather.SUNNY)
      )
    )

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
    ).update(
      _.name := FullName("Foo", "Bar"),
      _.age  := Years(5)
    )
    val concat = (m1.toByteArray ++ m2.toByteArray)
    CustomMessage.parseFrom(concat) must be(expected)
  }

  "Extended types" should "inherit from marker type" in {
    val t: DomainEvent = CustomerEvent(
      personId = Some(PersonId("123")),
      optionalNumber = Some(1),
      repeatedNumber = Seq(2, 3, 4),
      requiredNumber = 5
    )
    t mustBe a[DomainEvent]
    t.personId must be(Some(PersonId("123")))
    t.optionalNumber must be(Some(1))
    t.repeatedNumber must be(Seq(2, 3, 4))
    t.requiredNumber must be(5)
  }

  "Extended companion objects" should "inherit from marker type" in {
    CustomerEvent mustBe a[DomainEventCompanion]
    CustomerEvent.thisIs must be("The companion object")
  }

  "HasEmail" should "serialize and parse valid instances" in {
    val dm = HasEmail(
      requiredEmail = Email("foo", "bar")
    )
    HasEmail.parseFrom(dm.toByteArray) must be(dm)
  }

  "NoBoxEmail" should "serialize and parse valid instances" in {
    val dm = NoBoxEmail(
      noBoxEmail = Email("foo", "bar")
    )
    NoBoxEmail.parseFrom(dm.toByteArray) must be(dm)
  }

  "ContainsHasEmail" should "serialize and parse valid instances" in {
    val cem = ContainsHasEmail(
      requiredHasEmail = HasEmail(requiredEmail = Email("foo", "bar"))
    )
    ContainsHasEmail.parseFrom(cem.toByteArray) must be(cem)
  }
}

object CustomTypesSpec {
  val Message = CustomMessage(
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
      FullName(firstName = "V1", lastName = "Z2")
    ),
    weather = Some(WrappedWeather(Weather.RAIN)),
    requiredWeather = WrappedWeather(Weather.SUNNY),
    weathers = Seq(WrappedWeather(Weather.RAIN), WrappedWeather(Weather.SUNNY)),
    packedWeathers = Seq(WrappedWeather(Weather.RAIN), WrappedWeather(Weather.RAIN))
  )
}
