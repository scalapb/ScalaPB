import com.thesamet.pb.EnumOptions.{EnumBase, EnumCompanionBase, ValueMixin}
import com.thesamet.proto.e2e.enum_options._
import org.scalatest._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class EnumOptionsSpec extends AnyFlatSpec with Matchers with OptionValues {
  "companion object" should "extend EnumCompanionBase" in {
    MyEnum mustBe a[EnumCompanionBase]
    MyEnum must not be a[EnumBase]
    MyEnum must not be a[ValueMixin]
  }

  "enum values" should "extend EnumBase" in {
    MyEnum.MyUnknown mustBe a[EnumBase]
    MyEnum.V1 mustBe a[EnumBase]
    MyEnum.V2 mustBe a[EnumBase]
    MyEnum.Unrecognized(-1) mustBe a[EnumBase]

    MyEnum.MyUnknown must not be a[ValueMixin]
    MyEnum.V1 mustBe a[ValueMixin]
    MyEnum.V2 must not be a[ValueMixin]
    MyEnum.Unrecognized(-1) must not be a[ValueMixin]
  }

  "enum values" should "use naming scheme correctly" in {
    MyEnum.MyThing.isMyThing must be(true)
    MyEnum.FuzzBUZZ.isFuzzBuzz must be(true)
  }

  "enum values" should "have the scala name provided in the descriptor" in {
    MyEnum.FuzzBUZZ.scalaValueDescriptor.scalaName must be ("FuzzBUZZ")
    MyEnum.FuzzBUZZ.name must be("ANOTHER_ONE")
    MyEnum.MyThing.scalaValueDescriptor.scalaName must be ("MyThing")
    MyEnum.MyThing.scalaValueDescriptor.name must be("MY_THING")
  }
}
