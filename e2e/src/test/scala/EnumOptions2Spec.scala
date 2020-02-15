import com.thesamet.proto.e2e.enum_options2._
import com.thesamet.proto.e2e.enum_options3._
import org.scalatest._

class EnumOptions2Spec extends FlatSpec with MustMatchers with OptionValues {
  "Prefixes" should "be stripped from MyEnum2" in {
    MyEnum2.V1.isV1 must be(true)
    MyEnum2.`2`.is2 must be(true)
  }

  "Prefixes" should "be stripped from MyEnum3" in {
    MyEnum3.V1.isV1 must be(true)
  }
}
