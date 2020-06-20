import com.thesamet.proto.e2e.defaults._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class DefaultsSpec extends AnyFlatSpec with Matchers {
  val d = DefaultsTest()

  "defaults" should "be set correctly" in {
    d.d1.isEmpty must be(true)
    d.getD1 must be(0.0)
    d.getD2.isPosInfinity must be(true)
    d.getD3.isNegInfinity must be(true)
    d.getD4.isNaN must be(true)
    d.getF1 must be(0.0f)
    d.getF2.isPosInfinity must be(true)
    d.getF3.isNegInfinity must be(true)
    d.getF4.isNaN must be(true)
  }
}
