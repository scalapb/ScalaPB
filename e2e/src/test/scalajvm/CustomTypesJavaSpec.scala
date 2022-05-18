import com.thesamet.proto.e2e.custom_types._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CustomTypesJavaSpec extends AnyFlatSpec with Matchers {
  import CustomTypesSpec.Message

  "CustomMessage" should "serialize and parse" in {
    CustomMessage.parseFrom(Message.toByteArray) must be(Message)
    CustomMessage.toJavaProto(Message).getPersonId must be("abcd")
    CustomMessage.toJavaProto(Message).getRequiredPersonId must be("required")
    CustomMessage.toJavaProto(Message).getAge must be(27)
    CustomMessage.toJavaProto(Message).getRequiredAge must be(25)
    CustomMessage.toJavaProto(Message).getName.getFirst must be("Foo")
    CustomMessage.toJavaProto(Message).getName.getLast must be("Bar")
  }
}
