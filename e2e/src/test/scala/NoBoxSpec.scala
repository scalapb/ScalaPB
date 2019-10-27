import com.thesamet.proto.e2e.NoBox
import com.thesamet.proto.e2e.no_box.Car
import com.thesamet.proto.e2e.no_box.DontBoxMe
import com.thesamet.proto.e2e.no_box.Tyre
import org.scalatest._

class NoBoxSpec extends FlatSpec with MustMatchers {
  val car = Car(tyre1 = Tyre(size = 10), tyre2 = Some(Tyre(size = 20)))

  "no_box" should "create correct methods" in {
    car.tyre1 must be(Tyre(size = 10))
    car.tyre2 must be(Some(Tyre(size = 20)))
  }

  "fields with no_box" should "parseFrom byte array correctly" in {
    val serialized = car.toByteArray
    Car.parseFrom(serialized) must be(car)
  }

  "Scala representation of Java message with no_box field with default value" should "have that field with default value" in {
    val javaCar = NoBox.Car.getDefaultInstance()
    javaCar.hasTyre1 must be(false)

    val scalaCar = Car.parseFrom(javaCar.toByteArray)
    scalaCar.tyre1 must be(Tyre.defaultInstance)
  }

  "Java representation of Scala message with a no_box field with default value" should "not have that field" in {
    val scalaCar = Car(tyre1 = Tyre.defaultInstance)
    scalaCar.tyre1 must be(Tyre.defaultInstance)

    val javaCar = NoBox.Car.parseFrom(scalaCar.toByteArray)
    javaCar.hasTyre1 must be(false)
  }

  "Scala message with a no_box field with null value" should "throw exception when being serialized" in {
    val car = Car(tyre1 = null)
    a[NullPointerException] shouldBe thrownBy(car.toByteArray)
  }

  "Scala message with a no_box reference" should "generate correct types" in {
    val car = Car()
    car.dontBoxMeDef mustBe (DontBoxMe.defaultInstance)
    car.dontBoxMeOverrideTrue mustBe (DontBoxMe.defaultInstance)
    car.dontBoxMeOverrideFalse mustBe (None)
    car.nameNoBox mustBe (com.thesamet.pb.FullName("", ""))
  }
}
