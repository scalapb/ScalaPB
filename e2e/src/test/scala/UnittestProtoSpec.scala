import org.scalatest._
import protobuf_unittest.unittest.TestExtremeDefaultValues

class UnittestProtoSpec extends FlatSpec with MustMatchers {
  "testDefaults" should "work with extreme default values" in {
    val message = TestExtremeDefaultValues()
    "\u1234" must be(message.getUtf8String)
    message.getInfDouble.isPosInfinity must be(true)
    message.getNegInfDouble.isNegInfinity must be(true)
    message.getNanDouble.isNaN must be(true)
    message.getInfFloat.isPosInfinity  must be(true)
    message.getNegInfFloat.isNegInfinity must be(true)
    message.getNanFloat.isNaN must be(true)
    "? ? ?? ?? ??? ??/ ??-" must be(message.getCppTrigraph)
  }
}

