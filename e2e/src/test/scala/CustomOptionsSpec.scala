import scala.reflect.runtime.universe._
import com.trueaccord.scalapb.{GeneratedExtension, JavaProtoSupport}
import com.trueaccord.proto.e2e.custom_options.GoodOrBad._
import com.trueaccord.proto.e2e.custom_options_p3.GoodOrBadP3._
import com.trueaccord.proto.e2e.custom_options._
import com.trueaccord.proto.e2e.custom_options_p3._
import com.trueaccord.proto.e2e.custom_options_use._
import org.scalatest._
import com.google.protobuf.ByteString
import com.google.protobuf.descriptor.MessageOptions
import com.trueaccord.pb.{Base1, Base2, FullName}

class CustomOptionsSpec extends FlatSpec with MustMatchers with OptionValues {
  val barOptions = BarMessage.scalaDescriptor.getOptions
  val barP3Options = BarP3.scalaDescriptor.getOptions
  val fooOptions = FooMessage.scalaDescriptor.getOptions

  println(s"Have java conversions: ${MessageB.isInstanceOf[JavaProtoSupport[_, _]]}")

  def validateSetter[T](extension: GeneratedExtension[MessageOptions, T])(value: T) = {
    barOptions.withExtension(extension)(value).extension(extension) must be(value)
  }

  "CustomAnnotation" should "exist" in {
    val annotations = typeOf[FooMessage].typeSymbol.asClass.annotations
    annotations.count(_.toString == "com.trueaccord.pb.CustomAnnotation") must be (1)
  }

  "CustomAnnotation, CustomAnnotation1, CustomAnnotation2" should "exist" in {
    val annotations = typeOf[BarMessage].typeSymbol.asClass.annotations.map(_.toString)
    annotations must contain allOf (
      "com.trueaccord.pb.CustomAnnotation",
      "com.trueaccord.pb.CustomAnnotation1",
      "com.trueaccord.pb.CustomAnnotation2"
    )
  }
  "Options existing" should "return Some(option)" in {
    fooOptions.extension(CustomOptionsProto.messageB).value must be (MessageB(b = Some("BBB")))
    validateSetter(CustomOptionsProto.messageB)(Some(MessageB(b = Some("ABC"))))
  }

  "Options non-existing" should "return None" in {
    barOptions.extension(CustomOptionsProto.messageB) must be (None)
    validateSetter(CustomOptionsProto.messageB)(None)
  }

  "Repeated list of messages" should "work when non-empty" in {
    barOptions.extension(CustomOptionsProto.repMessageC) must be (
      Seq(MessageC(c = Some("C1")), MessageC(c = Some("C2"))))
    validateSetter(CustomOptionsProto.repMessageC)(
      Seq(MessageC(c = Some("XYZ")), MessageC(c = Some("ABC")))
    )
  }

  "parsing and serializing message with unknown fields" should "yield original message" in {
    MessageOptions.parseFrom(barOptions.toByteArray) must be(barOptions)
    MessageOptions.parseFrom(fooOptions.toByteArray) must be(fooOptions)
    MessageOptions.parseFrom(barP3Options.toByteArray) must be(barP3Options)
  }

  "Repeated list of messages" should "work when empty" in {
    fooOptions.extension(
      CustomOptionsProto.repMessageC) must be (Seq.empty)
    validateSetter(CustomOptionsProto.repMessageC)(Seq.empty)
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

  "setting repeated fields" should "work" in {
    validateSetter(CustomOptionsProto.repInt32)(Seq(1, 2, -16, -5))
    validateSetter(CustomOptionsProto.repInt64)(Seq(3, 4, -9, -11))
    validateSetter(CustomOptionsProto.repSint32)(Seq(5, -11))
    validateSetter(CustomOptionsProto.repSint64)(Seq(6, -1, -15))
    validateSetter(CustomOptionsProto.repFixed32)(Seq(7, 5))
    validateSetter(CustomOptionsProto.repFixed64)(Seq(8, 17))
    validateSetter(CustomOptionsProto.repFloat)(Seq(4.17f))
    validateSetter(CustomOptionsProto.repDouble)(Seq(5.35))
    validateSetter(CustomOptionsProto.repEnum)(Seq(GOOD, BAD, GOOD))
    validateSetter(CustomOptionsProto.repBool)(Seq(false, true, false))
    validateSetter(CustomOptionsProto.repString)(Seq("foo", "bar"))
    validateSetter(CustomOptionsProto.repBytes)(Seq(ByteString.copyFromUtf8("foo"), ByteString.copyFromUtf8("bar")))
    validateSetter(CustomOptionsProto.repUint32)(Seq(1, 2, -16, -5))
    validateSetter(CustomOptionsProto.repUint64)(Seq(3, 4, -9, -11))
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

  "setting optional primitives" should "work" in {
    validateSetter(CustomOptionsProto.optInt32)(Some(1))
    validateSetter(CustomOptionsProto.optInt64)(Some(3))
    validateSetter(CustomOptionsProto.optSint32)(Some(5))
    validateSetter(CustomOptionsProto.optSint64)(Some(6))
    validateSetter(CustomOptionsProto.optFixed32)(Some(7))
    validateSetter(CustomOptionsProto.optFixed64)(Some(8))
    validateSetter(CustomOptionsProto.optFloat)(Some(4.17f))
    validateSetter(CustomOptionsProto.optDouble)(Some(5.35))
    validateSetter(CustomOptionsProto.optEnum)(Some(GOOD))
    validateSetter(CustomOptionsProto.optBool)(Some(true))
    validateSetter(CustomOptionsProto.optString)(Some("foo"))
    validateSetter(CustomOptionsProto.optBytes)(Some(ByteString.copyFromUtf8("foo")))
    validateSetter(CustomOptionsProto.optInt32)(None)
    validateSetter(CustomOptionsProto.optInt64)(None)
    validateSetter(CustomOptionsProto.optSint32)(None)
    validateSetter(CustomOptionsProto.optSint64)(None)
    validateSetter(CustomOptionsProto.optFixed32)(None)
    validateSetter(CustomOptionsProto.optFixed64)(None)
    validateSetter(CustomOptionsProto.optFloat)(None)
    validateSetter(CustomOptionsProto.optDouble)(None)
    validateSetter(CustomOptionsProto.optEnum)(None)
    validateSetter(CustomOptionsProto.optBool)(None)
    validateSetter(CustomOptionsProto.optString)(None)
    validateSetter(CustomOptionsProto.optBytes)(None)
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

  "setting proto3 primitives" should "work" in {
    validateSetter(CustomOptionsP3Proto.p3OptInt32)(1)
    validateSetter(CustomOptionsP3Proto.p3OptInt64)(3)
    validateSetter(CustomOptionsP3Proto.p3OptSint32)(5)
    validateSetter(CustomOptionsP3Proto.p3OptSint64)(6)
    validateSetter(CustomOptionsP3Proto.p3OptFixed32)(7)
    validateSetter(CustomOptionsP3Proto.p3OptFixed64)(8)
    validateSetter(CustomOptionsP3Proto.p3OptFloat)(4.17f)
    validateSetter(CustomOptionsP3Proto.p3OptDouble)(5.35)
    validateSetter(CustomOptionsP3Proto.p3OptEnum)(GOOD_P3)
    validateSetter(CustomOptionsP3Proto.p3OptBool)(true)
    validateSetter(CustomOptionsP3Proto.p3OptString)("foo")
    validateSetter(CustomOptionsP3Proto.p3OptBytes)(ByteString.copyFromUtf8("foo"))
  }

  "Custom name on field descriptor" should "translate to custom type" in {
    val field1Opts = FooMessage.scalaDescriptor.findFieldByName("myField1").get.getOptions
    val field2Opts = FooMessage.scalaDescriptor.findFieldByName("myField2").get.getOptions
    CustomOptionsProto.optName.get(field1Opts) must be(Some(FullName("John", "")))
    CustomOptionsProto.optName.get(field2Opts) must be(None)
    CustomOptionsProto.repName.get(field1Opts) must be(Seq.empty)
    CustomOptionsProto.repName.get(field2Opts) must be(Seq(FullName("", "Doe"), FullName("Moe", "")))
  }

  "packed fields" should "parse correctly" in {
    val ps = PackedStuff().update(
      _.packedInt32 := Seq(1, 19, -6),
      _.packedInt64 := Seq(1L, 19L, -7),
      _.packedBool := Seq(false, true, false),
      _.packedSint32 := Seq(3, -15, 246),
      _.packedSint64 := Seq(-29, 35, 145),
      _.packedDouble := Seq(3.2, -55, 14.4, Double.NaN, Double.NegativeInfinity, Double.PositiveInfinity),
      _.packedFloat := Seq(3.2f, -55f, 14.4f, Float.NaN, Float.NegativeInfinity, Float.PositiveInfinity),
      _.packedFixed32 := Seq(1, 19, -6),
      _.packedFixed64 := Seq(1L, 19L, -6),
      _.packedEnum := Seq(GoodOrBad.BAD, GoodOrBad.GOOD, GoodOrBad.Unrecognized(39)),
      _.packedUint32 := Seq(3, -15, 246),
      _.packedUint64 := Seq(-29, 35, 145))
    val m = MessageOptions.parseFrom(ps.toByteArray)
    m.extension(CustomOptionsProto.packedInt32) must be (Seq(1, 19, -6))
    m.extension(CustomOptionsProto.packedInt64) must be (Seq(1L, 19L, -7))
    m.extension(CustomOptionsProto.packedBool) must be (Seq(false, true, false))
    m.extension(CustomOptionsProto.packedSint32) must be (Seq(3, -15, 246))
    m.extension(CustomOptionsProto.packedSint64) must be (Seq(-29, 35, 145))
    m.extension(CustomOptionsProto.packedDouble).take(3) must equal (Seq(3.2, -55, 14.4))
    m.extension(CustomOptionsProto.packedFloat).take(3) must equal (Seq(3.2f, -55f, 14.4f))
    m.extension(CustomOptionsProto.packedDouble)(3).isNaN must be(true)
    m.extension(CustomOptionsProto.packedDouble)(4).isNegInfinity must be(true)
    m.extension(CustomOptionsProto.packedDouble)(5).isPosInfinity must be(true)
    m.extension(CustomOptionsProto.packedFloat)(3).isNaN must be(true)
    m.extension(CustomOptionsProto.packedFloat)(4).isNegInfinity must be(true)
    m.extension(CustomOptionsProto.packedFloat)(5).isPosInfinity must be(true)
    m.extension(CustomOptionsProto.packedFixed32) must be (Seq(1, 19, -6))
    m.extension(CustomOptionsProto.packedFixed64) must be (Seq(1L, 19L, -6))
    m.extension(CustomOptionsProto.packedEnum) must be (Seq(GoodOrBad.BAD, GoodOrBad.GOOD, GoodOrBad.Unrecognized(39)))
    m.extension(CustomOptionsProto.packedUint32) must be (Seq(3, -15, 246))
    m.extension(CustomOptionsProto.packedUint64) must be (Seq(-29, 35, 145))
    MessageOptions.parseFrom(m.toByteArray) must be(m)
  }

  "setting packed field" should "work correctly" in {
    validateSetter(CustomOptionsProto.packedInt32)(Seq(1, 19, -6))
    validateSetter(CustomOptionsProto.packedInt64)(Seq(1L, 19L, -7))
    validateSetter(CustomOptionsProto.packedBool)(Seq(false, true, false))
    validateSetter(CustomOptionsProto.packedSint32)(Seq(3, -15, 246))
    validateSetter(CustomOptionsProto.packedSint64)(Seq(-29, 35, 145))
    validateSetter(CustomOptionsProto.packedDouble)(Seq(3.2, -55, 14.4))
    validateSetter(CustomOptionsProto.packedFloat)(Seq(3.2f, -55f, 14.4f))
    validateSetter(CustomOptionsProto.packedFixed32)(Seq(1, 19, -6))
    validateSetter(CustomOptionsProto.packedFixed64)(Seq(1L, 19L, -6))
    validateSetter(CustomOptionsProto.packedEnum)(Seq(GoodOrBad.BAD, GoodOrBad.GOOD, GoodOrBad.Unrecognized(39)))
    validateSetter(CustomOptionsProto.packedUint32)(Seq(3, -15, 246))
    validateSetter(CustomOptionsProto.packedUint64)(Seq(-29, 35, 145))
  }

  "my_one_of" should "extends Base1 and Base2" in {
    FooMessage.MyOneOf.Empty.isInstanceOf[Base1] must be(true)
    FooMessage.MyOneOf.Empty.isInstanceOf[Base2] must be(true)
    FooMessage.MyOneOf.X(3).isInstanceOf[Base1] must be(true)
    FooMessage.MyOneOf.X(3).isInstanceOf[Base2] must be(true)
  }

  "field annotations" should "be set correctly" in {
    typeOf[FieldAnnotations].member(TermName("z")).annotations.map(_.toString) must contain(
      "scala.deprecated(\"Will be removed\", \"0.1\")"
    )
  }

  "companion annotations" should "be set correctly" in {
    typeOf[FooMessage.type].typeSymbol.asClass.annotations.map(_.toString) must contain only(
      "com.trueaccord.pb.CustomAnnotation1",
      "com.trueaccord.pb.CustomAnnotation2"
    )
  }
}
