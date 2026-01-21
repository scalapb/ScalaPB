import com.thesamet.proto.e2e.no_box._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import com.google.protobuf.InvalidProtocolBufferException

class NoBoxSpec extends AnyFlatSpec with Matchers {
  val car = Car(tyre1 = Tyre(size = 10), tyre2 = Some(Tyre(size = 20)))

  "no_box" should "create correct methods" in {
    car.tyre1 must be(Tyre(size = 10))
    car.tyre2 must be(Some(Tyre(size = 20)))
  }

  "fields with no_box" should "parseFrom byte array correctly" in {
    val serialized = car.toByteArray
    Car.parseFrom(serialized) must be(car)
  }

  "Java representation of Scala message with a no_box field with default value" should "not have that field" in {
    val scalaCar = Car(tyre1 = Tyre.defaultInstance)
    scalaCar.tyre1 must be(Tyre.defaultInstance)
  }

  "Scala message with a no_box reference" should "generate correct types" in {
    val car = Car()
    car.dontBoxMeDef mustBe (DontBoxMe.defaultInstance)
    car.dontBoxMeOverrideTrue mustBe (DontBoxMe.defaultInstance)
    car.dontBoxMeOverrideFalse mustBe (None)
    car.nameNoBox mustBe (com.thesamet.pb.FullName("", ""))
  }

  "RequiredCar" should "have unboxed message field" in {
    RequiredCar(tyre1 = Tyre(size = 12))
  }

  "RequiredCar" should "fail validation if required field is missing" in {
    intercept[InvalidProtocolBufferException] {
      RequiredCar.parseFrom(Array.empty[Byte])
    }.getMessage must be("Message missing required fields: tyre1")
  }

  "RequiredCar" should "fail parsing from text if field is empty" in {
    RequiredCar.fromAscii("tyre1 { size: 12 }")
    intercept[NoSuchElementException] {
      RequiredCar.fromAscii("")
    }
  }

  // Issue 1198
  "Non-total type" should "serialize and parse correctly" in {
    val p = Person("", Money(BigDecimal("123.123")))
    Person.parseFrom(p.toByteArray) must be(p)
  }

  it should "throw an exception when missing data" in {
    intercept[NumberFormatException] {
      Person.parseFrom(Array.empty[Byte])
    }
  }
}
