import com.trueaccord.proto.e2e.custom_types._
import org.scalatest._
import com.trueaccord.pb._

class CustomTypesSpec extends FlatSpec with MustMatchers {

  "CustomMessage" should "serialize and parse" in {
    val message = CustomMessage(
      personId = Some(PersonId("abcd")),
      requiredPersonId = PersonId("required"),
      personIds = Seq(PersonId("p1"), PersonId("p2")),
      age = Some(Years(27)),
      requiredAge = Years(25),
      ages = Seq(Years(3), Years(8), Years(35))
    )
    message.getPersonId must be(PersonId("abcd"))
    message.requiredPersonId must be(PersonId("required"))
    message.personIds must be(Seq(PersonId("p1"), PersonId("p2")))
    message.getAge must be(Years(27))
    message.requiredAge must be(Years(25))
    message.ages must be(Seq(Years(3), Years(8), Years(35)))
    CustomMessage.parseFrom(message.toByteArray) must be(message)
    CustomMessage.toJavaProto(message).getPersonId must be("abcd")
    CustomMessage.toJavaProto(message).getRequiredPersonId must be("required")
    CustomMessage.toJavaProto(message).getAge must be(27)
    CustomMessage.toJavaProto(message).getRequiredAge must be(25)
  }
}
