import com.trueaccord.proto.e2e.custom_types._
import org.scalatest._
import com.trueaccord.pb.PersonId

class CustomTypesSpec extends FlatSpec with MustMatchers {

  "CustomMessage" should "serialize and parse" in {
    val message = CustomMessage(
      personId = Some(PersonId("abcd")),
      requiredPersonId = PersonId("required"),
      personIds = Seq(PersonId("p1"), PersonId("p2"))
    )
    message.getPersonId must be(PersonId("abcd"))
    message.requiredPersonId must be(PersonId("required"))
    message.personIds must be(Seq(PersonId("p1"), PersonId("p2")))
    CustomMessage.parseFrom(message.toByteArray) must be(message)
    CustomMessage.toJavaProto(message).getPersonId must be("abcd")
    CustomMessage.toJavaProto(message).getRequiredPersonId must be("required")
  }
}
