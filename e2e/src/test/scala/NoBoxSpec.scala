import org.scalatest._
import com.trueaccord.proto.e2e.no_box.Car
import com.trueaccord.proto.e2e.no_box.Tyre
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
}

