import com.thesamet.proto.e2e.enum3.{Color3, EnumTest3}
import com.google.protobuf.ByteString
import com.google.protobuf.wrappers._
import com.thesamet.proto.e2e.no_box._
import com.thesamet.proto.e2e.sealed_oneof._
import com.thesamet.proto.e2e.proto3_presence.{PEnum, Presence}
import com.thesamet.proto.e2e.maps.{MapsTest, MessageValue}
import com.thesamet.proto.e2e.edition2023.{Edition2023Features, Edition2023Message}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.{GeneratedMessage, TextFormat}

class TextFormatDefaultsSpec extends AnyFlatSpec with Matchers {
  "TextFormat" should "omit implicit scalar defaults in every printing mode" in {
    val defaults: Seq[GeneratedMessage] = Seq(
      DoubleValue(),
      FloatValue(),
      Int64Value(),
      UInt64Value(),
      Int32Value(),
      UInt32Value(),
      BoolValue(),
      StringValue(),
      BytesValue(),
      Lit(),
      EnumTest3()
    )
    defaults.foreach { message =>
      TextFormat.printToString(message) must be("")
      TextFormat.printToUnicodeString(message) must be("")
      TextFormat.printToSingleLineUnicodeString(message) must be("")
      message.toProtoString must be("")
    }
    Int32Value(1).toProtoString must be("value: 1\n")
    BoolValue(true).toProtoString must be("value: true\n")
    StringValue("x").toProtoString must be("value: \"x\"\n")
    BytesValue(ByteString.copyFromUtf8("x")).toProtoString must be("value: \"x\"\n")
  }

  it should "preserve negative floating-point zero" in {
    FloatValue(-0.0f).toProtoString must be("value: -0.0\n")
    DoubleValue(-0.0).toProtoString must be("value: -0.0\n")
  }

  it should "preserve explicit scalar and message presence" in {
    Presence().toProtoString must be("")
    Presence(
      i32 = Some(0),
      penum = Some(PEnum.PENUM_UNKNOWN),
      presence = Some(Presence())
    ).toProtoString must be("i32: 0\npenum: PENUM_UNKNOWN\npresence {\n}\n")
    Edition2023Message(value = Some("")).toProtoString must be("value: \"\"\n")
    Edition2023Features(requiredValue = 0).toProtoString must be("required_value: 0\n")
  }

  it should "omit empty unboxed messages while preserving boxed and required messages" in {
    Car().toProtoString must be("")
    Car(tyre2 = Some(Tyre())).toProtoString must be("tyre2 {\n}\n")
    RequiredCar(Tyre()).toProtoString must be("tyre1 {\n}\n")
    Car(dontBoxMeOverrideFalse = Some(DontBoxMe())).toProtoString must be(
      "dont_box_me_override_false {\n}\n"
    )
    RequiredCarHolder().toProtoString must be("car {\n  tyre1 {\n  }\n}\n")
    Car(tyre1 = Tyre(1)).toProtoString must be("tyre1 {\n  size: 1\n}\n")
  }

  it should "print custom message types without constructing invalid defaults" in {
    Person("", Money(BigDecimal("123.123"))).toProtoString must be(
      "money {\n  amount: \"123.123\"\n}\n"
    )
  }

  it should "preserve an unboxed message containing only unknown fields" in {
    val tyre = Tyre.parseFrom(Array[Byte](16, 1))
    // Unknown fields are not printed, but still make this message present on the wire.
    Car(tyre1 = tyre).toProtoString must be("tyre1 {\n}\n")
  }

  it should "omit empty sealed-oneof fields but retain selected cases and repeated elements" in {
    Add().toProtoString must be("")
    Lit().asMessage.toProtoString must be("lit {\n}\n")
    Programs(programs = Seq(Expr.Empty)).toProtoString must be("programs {\n}\n")
    Programs(exprMap = Map("" -> Expr.Empty)).toProtoString must be(
      "expr_map {\n  key: \"\"\n  value {\n  }\n}\n"
    )
    com.thesamet.proto.e2e.optional.sealed_oneof_optional.Add().toProtoString must be("")
  }

  it should "retain map entries and repeated scalar defaults" in {
    MapsTest(stringToInt32 = Map("" -> 0), repeatedNotAMap = Seq(0)).toProtoString must be(
      "string_to_int32 {\n  key: \"\"\n  value: 0\n}\nrepeated_not_a_map: 0\n"
    )
    MapsTest(int32ToMessageField = Map(0 -> MessageValue())).toProtoString must be(
      "int32_to_message_field {\n  key: 0\n  value {\n  }\n}\n"
    )
  }

  it should "preserve selected scalar defaults and repeated enum defaults" in {
    protobuf_unittest.unittest.TestOneof2.fromAscii("foo_int: 0").toProtoString must be(
      "foo_int: 0\n"
    )
    EnumTest3(colorVector = Seq(Color3.C3_UNKNOWN)).toProtoString must be(
      "color_vector: C3_UNKNOWN\n"
    )
    EnumTest3(color = Color3.Unrecognized(123)).toProtoString must be("color: 123\n")
  }

  it should "leave reflection values unchanged" in {
    val field = Lit.scalaDescriptor.fields.head
    Lit().getField(field) must be(scalapb.descriptors.PInt(0))
    Lit().toPMessage.value(field) must be(scalapb.descriptors.PInt(0))
  }
}
