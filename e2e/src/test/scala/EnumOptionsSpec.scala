import com.trueaccord.proto.e2e.enum_options._
import com.trueaccord.pb.EnumOptions._
import org.scalatest._

class EnumOptionsSpec extends FlatSpec with MustMatchers with OptionValues {
  "companion object" should "extend EnumCompanionBase" in {
    MyEnum mustBe a [EnumCompanionBase]
    MyEnum must not be a [EnumBase]
    MyEnum must not be a [ValueMixin]
  }

  "enum values" should "extend EnumBase" in {
    MyEnum.Unknown mustBe a [EnumBase]
    MyEnum.V1 mustBe a [EnumBase]
    MyEnum.V2 mustBe a [EnumBase]
    MyEnum.Unrecognized(-1) mustBe a [EnumBase]

    MyEnum.Unknown must not be a [ValueMixin]
    MyEnum.V1 mustBe a [ValueMixin]
    MyEnum.V2 must not be a [ValueMixin]
    MyEnum.Unrecognized(-1) must not be a [ValueMixin]
  }
}
