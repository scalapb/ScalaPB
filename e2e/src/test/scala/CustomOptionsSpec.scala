import com.trueaccord.scalapb.JavaProtoSupport
import com.trueaccord.proto.e2e.custom_options.GoodOrBad._
import com.trueaccord.proto.e2e.custom_options_p3.GoodOrBadP3._
import com.trueaccord.proto.e2e.custom_options._
import com.trueaccord.proto.e2e.custom_options_p3._
import com.trueaccord.proto.e2e.custom_options_use._
import org.scalatest._
import com.google.protobuf.ByteString
import com.trueaccord.pb.FullName

// NOTE: this file is symlinked from the nojava tests. The idea is that the same
// source should pass when the custom options were not compiled with Java
// support.

class CustomOptionsSpec extends FlatSpec with MustMatchers with OptionValues {
  import com.trueaccord.scalapb.Implicits._

  val barOptions = BarMessage.javaDescriptor.getOptions
  val barP3Options = BarP3.javaDescriptor.getOptions
  val fooOptions = FooMessage.javaDescriptor.getOptions

  println(s"Have java conversions: ${MessageB.isInstanceOf[JavaProtoSupport[_, _]]}")

  "Options existing" should "return Some(option)" in {
    fooOptions.extension(CustomOptionsProto.messageB).value must be (MessageB(b = Some("BBB")))
  }

  "Options non-existing" should "return None" in {
    barOptions.extension(CustomOptionsProto.messageB) must be (None)
  }

  "Repeated list of messages" should "work when non-empty" in {
    barOptions.extension(CustomOptionsProto.repMessageC) must be (
      Seq(MessageC(c = Some("C1")), MessageC(c = Some("C2"))))
  }

  "Repeated list of messages" should "work when empty" in {
    fooOptions.extension(
      CustomOptionsProto.repMessageC) must be (Seq.empty)
  }

  "Repeated list of primitives" should "work" in {
    barOptions.extension(CustomOptionsProto.repInt32) must be (Seq(1, 2, -16, -5))
    barOptions.extension(CustomOptionsProto.repInt64) must be (Seq(3, 4, -9, -11))
    barOptions.extension(CustomOptionsProto.repSint32) must be (Seq(5, -11))
    barOptions.extension(CustomOptionsProto.repSint64) must be (Seq(6, -1, -15))
    barOptions.extension(CustomOptionsProto.repFixed32) must be (Seq(7, 5))
    barOptions.extension(CustomOptionsProto.repFixed64) must be (Seq(8, 17))
    barOptions.extension(CustomOptionsProto.repFloat) must be (Seq(4.17f))
    barOptions.extension(CustomOptionsProto.repDouble) must be (Seq(5.35))
    barOptions.extension(CustomOptionsProto.repEnum) must be (Seq(GOOD, BAD, GOOD))
    barOptions.extension(CustomOptionsProto.repBool) must be (Seq(false, true, false))
    barOptions.extension(CustomOptionsProto.repString) must be (Seq("foo", "bar"))
    barOptions.extension(CustomOptionsProto.repBytes) must be (
      Seq(ByteString.copyFromUtf8("foo"), ByteString.copyFromUtf8("bar")))
    barOptions.extension(CustomOptionsProto.repUint32) must be (Seq(1, 2, -16, -5))
    barOptions.extension(CustomOptionsProto.repUint64) must be (Seq(3, 4, -9, -11))
  }

  "Optional primitives" should "work" in {
    barOptions.extension(CustomOptionsProto.optInt32).value must be (1)
    barOptions.extension(CustomOptionsProto.optInt64).value must be (3)
    barOptions.extension(CustomOptionsProto.optSint32).value must be (5)
    barOptions.extension(CustomOptionsProto.optSint64).value must be (6)
    barOptions.extension(CustomOptionsProto.optFixed32).value must be (7)
    barOptions.extension(CustomOptionsProto.optFixed64).value must be (8)
    barOptions.extension(CustomOptionsProto.optFloat).value must be (4.17f)
    barOptions.extension(CustomOptionsProto.optDouble).value must be (5.35)
    barOptions.extension(CustomOptionsProto.optEnum).value must be (GOOD)
    barOptions.extension(CustomOptionsProto.optBool).value must be (true)
    barOptions.extension(CustomOptionsProto.optString).value must be ("foo")
    barOptions.extension(CustomOptionsProto.optBytes).value must be (ByteString.copyFromUtf8("foo"))
  }

  "proto3 primitives" should "work" in {
    barP3Options.extension(CustomOptionsP3Proto.p3OptInt32) must be (1)
    barP3Options.extension(CustomOptionsP3Proto.p3OptInt64) must be (3)
    barP3Options.extension(CustomOptionsP3Proto.p3OptSint32) must be (5)
    barP3Options.extension(CustomOptionsP3Proto.p3OptSint64) must be (6)
    barP3Options.extension(CustomOptionsP3Proto.p3OptFixed32) must be (7)
    barP3Options.extension(CustomOptionsP3Proto.p3OptFixed64) must be (8)
    barP3Options.extension(CustomOptionsP3Proto.p3OptFloat) must be (4.17f)
    barP3Options.extension(CustomOptionsP3Proto.p3OptDouble) must be (5.35)
    barP3Options.extension(CustomOptionsP3Proto.p3OptEnum) must be (GOOD_P3)
    barP3Options.extension(CustomOptionsP3Proto.p3OptBool) must be (true)
    barP3Options.extension(CustomOptionsP3Proto.p3OptString) must be ("foo")
    barP3Options.extension(CustomOptionsP3Proto.p3OptBytes) must be (ByteString.copyFromUtf8("foo"))
  }

  "Custom name on field descriptor" should "translate to custom type" in {
    val field1Opts = FooMessage.javaDescriptor.findFieldByName("myField1").getOptions
    val field2Opts = FooMessage.javaDescriptor.findFieldByName("myField2").getOptions
    CustomOptionsProto.optName.get(field1Opts) must be(Some(FullName("John", "")))
    CustomOptionsProto.optName.get(field2Opts) must be(None)
    CustomOptionsProto.repName.get(field1Opts) must be(Seq.empty)
    CustomOptionsProto.repName.get(field2Opts) must be(Seq(FullName("", "Doe"), FullName("Moe", "")))
  }
}
