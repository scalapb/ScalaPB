import foo.barbaz.issue356._
import org.scalatest._

class NullValuesSpec extends FlatSpec with MustMatchers {
  val container = FooContainer()
    .withFoo(null)

  "null values" should "be None" in {
    container.foo must be(None)
  }
}
