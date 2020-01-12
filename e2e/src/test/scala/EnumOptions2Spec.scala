import com.thesamet.proto.e2e.enum_options2._
import com.thesamet.proto.e2e.enum_options3._
import org.scalatest._

class EnumOptions2Spec extends FlatSpec with MustMatchers with OptionValues {
  "Prefixes" should "be stripped from MyEnum2" in {
    MyEnum2.Unknown.isUnknown must be(true)
    MyEnum2.V1.isV1 must be(true)
    MyEnum2.V2.isV2 must be(true)
    MyEnum2.Thing.isThing must be(true)
    MyEnum2.`4`.is4 must be(true)
  }

  "Prefixes" should "be stripped from MyEnum3" in {
    MyEnum3.UNKNOWN.isUnknown must be(true)
    MyEnum3.V1.isV1 must be(true)
    MyEnum3.V2.isV2 must be(true)
    MyEnum3.THING.isThing must be(true)
  }
}
