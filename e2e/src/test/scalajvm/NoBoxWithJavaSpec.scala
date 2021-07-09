import com.thesamet.proto.e2e.NoBox
import com.thesamet.proto.e2e.no_box._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class NoBoxWithJavaSpec extends AnyFlatSpec with Matchers {
  val car = Car(tyre1 = Tyre(size = 10), tyre2 = Some(Tyre(size = 20)))

  "Scala representation of Java message with no_box field with default value" should "have that field with default value" in {
    val javaCar = NoBox.Car.getDefaultInstance()
    javaCar.hasTyre1 must be(false)
  }

  "Java representation of Scala message with a no_box field with default value" should "not have that field" in {
    val scalaCar = Car(tyre1 = Tyre.defaultInstance)
    val javaCar = NoBox.Car.parseFrom(scalaCar.toByteArray)
    javaCar.hasTyre1 must be(false)
  }

  "Non-total type" should "Convert to and from Java" in {
    val p = Person("", Money(BigDecimal("123.123")))
    Person.fromJavaProto(Person.toJavaProto(p)) must be(p)
  }
}
