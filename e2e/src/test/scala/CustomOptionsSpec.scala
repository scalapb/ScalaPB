import com.trueaccord.scalapb.JavaProtoSupport
import com.trueaccord.proto.e2e.custom_options.GoodOrBad._
import com.trueaccord.proto.e2e.custom_options._
import com.trueaccord.proto.e2e.custom_options_use._
import org.scalatest._
import com.google.protobuf.ByteString
import com.trueaccord.pb.FullName

// NOTE: this file is symlinked from the nojava tests. The idea is that the same
// source should pass when the custom options were not compiled with Java
// support.

class CustomOptionsSpec extends FlatSpec with MustMatchers with OptionValues {
  import com.trueaccord.scalapb.Implicits._

  val barOptions = BarMessage.descriptor.getOptions
  val fooOptions = FooMessage.descriptor.getOptions

  println(s"Have java conversions: ${MessageB.isInstanceOf[JavaProtoSupport[_, _]]}")

  "Options existing" should "return Some(option)" in {
    fooOptions.extension(CustomOptions.messageB).value must be (MessageB(b = Some("BBB")))
  }

  "Options non-existing" should "return None" in {
    barOptions.extension(CustomOptions.messageB) must be (None)
  }

  "Repeated list of messages" should "work when non-empty" in {
    barOptions.extension(CustomOptions.repMessageC) must be (
      Seq(MessageC(c = Some("C1")), MessageC(c = Some("C2"))))
  }

  "Repeated list of messages" should "work when empty" in {
    fooOptions.extension(
      CustomOptions.repMessageC) must be (Seq.empty)
  }

  "Repeated list of primitives" should "work" in {
    barOptions.extension(CustomOptions.repInt32) must be (Seq(1, 2, -16, -5))
    barOptions.extension(CustomOptions.repInt64) must be (Seq(3, 4, -9, -11))
    barOptions.extension(CustomOptions.repSint32) must be (Seq(5, -11))
    barOptions.extension(CustomOptions.repSint64) must be (Seq(6, -1, -15))
    barOptions.extension(CustomOptions.repFixed32) must be (Seq(7, 5))
    barOptions.extension(CustomOptions.repFixed64) must be (Seq(8, 17))
    barOptions.extension(CustomOptions.repFloat) must be (Seq(4.17f))
    barOptions.extension(CustomOptions.repDouble) must be (Seq(5.35))
    barOptions.extension(CustomOptions.repEnum) must be (Seq(GOOD, BAD, GOOD))
    barOptions.extension(CustomOptions.repBool) must be (Seq(false, true, false))
    barOptions.extension(CustomOptions.repString) must be (Seq("foo", "bar"))
    barOptions.extension(CustomOptions.repBytes) must be (
      Seq(ByteString.copyFromUtf8("foo"), ByteString.copyFromUtf8("bar")))
    barOptions.extension(CustomOptions.repUint32) must be (Seq(1, 2, -16, -5))
    barOptions.extension(CustomOptions.repUint64) must be (Seq(3, 4, -9, -11))
  }

  "Optional primitives" should "work" in {
    barOptions.extension(CustomOptions.optInt32).value must be (1)
    barOptions.extension(CustomOptions.optInt64).value must be (3)
    barOptions.extension(CustomOptions.optSint32).value must be (5)
    barOptions.extension(CustomOptions.optSint64).value must be (6)
    barOptions.extension(CustomOptions.optFixed32).value must be (7)
    barOptions.extension(CustomOptions.optFixed64).value must be (8)
    barOptions.extension(CustomOptions.optFloat).value must be (4.17f)
    barOptions.extension(CustomOptions.optDouble).value must be (5.35)
    barOptions.extension(CustomOptions.optEnum).value must be (GOOD)
    barOptions.extension(CustomOptions.optBool).value must be (true)
    barOptions.extension(CustomOptions.optString).value must be ("foo")
    barOptions.extension(CustomOptions.optBytes).value must be (ByteString.copyFromUtf8("foo"))
  }

  "Custom name on field descriptor" should "translate to custom type" in {
    val field1Opts = FooMessage.descriptor.findFieldByName("myField1").getOptions
    val field2Opts = FooMessage.descriptor.findFieldByName("myField2").getOptions
    CustomOptions.optName.get(field1Opts) must be(Some(FullName("John", "")))
    CustomOptions.optName.get(field2Opts) must be(None)
    CustomOptions.repName.get(field1Opts) must be(Seq.empty)
    CustomOptions.repName.get(field2Opts) must be(Seq(FullName("", "Doe"), FullName("Moe", "")))
  }
}
