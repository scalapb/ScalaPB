import foo.barbaz.issue356._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class NullValuesSpec extends AnyFlatSpec with Matchers {
  val container = FooContainer()
    .withFoo(null)

  "null values" should "be None" in {
    container.foo must be(None)
  }
}
