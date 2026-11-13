import com.thesamet.proto.e2e.enum3.{Color3, EnumTest3}
import com.google.protobuf.{DynamicMessage, TextFormat => JavaTextFormat}
import com.thesamet.proto.e2e.no_box._
import com.thesamet.proto.e2e.sealed_oneof._
import com.thesamet.proto.e2e.proto3_presence.{PEnum, Presence}
import com.thesamet.proto.e2e.maps.{MapsTest, MessageValue}
import com.thesamet.proto.e2e.edition2023.{Edition2023Features, Edition2023Message}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.{GeneratedMessage, TextFormat}

class TextFormatDefaultsJavaSpec extends AnyFlatSpec with Matchers {
  "TextFormat default omission" should "match Java for the same binary message" in {
    val messages: Seq[GeneratedMessage] = Seq(
      EnumTest3(),
      EnumTest3(colorVector = Seq(Color3.C3_UNKNOWN)),
      EnumTest3(color = Color3.Unrecognized(123)),
      protobuf_unittest.unittest.TestOneof2.fromAscii("foo_int: 0"),
      Lit(),
      Add(),
      Car(),
      Car(tyre1 = Tyre(1)),
      Car(tyre2 = Some(Tyre())),
      Car(dontBoxMeOverrideFalse = Some(DontBoxMe())),
      RequiredCar(Tyre()),
      RequiredCarHolder(),
      Person("", Money(BigDecimal("123.123"))),
      Lit().asMessage,
      Programs(programs = Seq(Expr.Empty)),
      Programs(exprMap = Map("" -> Expr.Empty)),
      Presence(),
      Presence(i32 = Some(0), penum = Some(PEnum.PENUM_UNKNOWN), presence = Some(Presence())),
      Edition2023Message(value = Some("")),
      Edition2023Features(requiredValue = 0),
      MapsTest(stringToInt32 = Map("" -> 0), repeatedNotAMap = Seq(0)),
      MapsTest(int32ToMessageField = Map(0 -> MessageValue())),
      protobuf_unittest.unittest.TestAllTypes(optionalInt32 = Some(0))
    )
    messages.foreach { message =>
      val javaMessage =
        DynamicMessage.parseFrom(message.companion.javaDescriptor, message.toByteArray)
      withClue(message.companion.scalaDescriptor.fullName + ": ") {
        TextFormat.printToString(message) must be(
          JavaTextFormat.printer().printToString(javaMessage)
        )
      }
    }
  }
}
