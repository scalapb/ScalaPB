import com.thesamet.pb.EnumOptions.{EnumBase, EnumCompanionBase, ValueMixin}
import com.thesamet.proto.e2e.enum_options._
import org.scalatest._

class EnumOptionsSpec extends FlatSpec with MustMatchers with OptionValues {
  "companion object" should "extend EnumCompanionBase" in {
    MyEnum mustBe a [EnumCompanionBase]
    MyEnum must not be a [EnumBase]
    MyEnum must not be a [ValueMixin]
  }

  "enum values" should "extend EnumBase" in {
    MyEnum.MyUnknown mustBe a [EnumBase]
    MyEnum.V1 mustBe a [EnumBase]
    MyEnum.V2 mustBe a [EnumBase]
    MyEnum.Unrecognized(-1) mustBe a [EnumBase]

    MyEnum.MyUnknown must not be a [ValueMixin]
    MyEnum.V1 mustBe a [ValueMixin]
    MyEnum.V2 must not be a [ValueMixin]
    MyEnum.Unrecognized(-1) must not be a [ValueMixin]
  }

  "enum values" should "use naming scheme correctly" in {
    MyEnum.MyThing.isMyThing must be(true)
    MyEnum.FuzzBUZZ.isFuzzBuzz must be(true)
  }
}
