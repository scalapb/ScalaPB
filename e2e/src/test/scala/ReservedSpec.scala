import com.trueaccord.proto.e2e.reserved._
import com.trueaccord.scalapb.GeneratedEnumCompanion
import org.scalatest._

class ReservedSpec extends FlatSpec with MustMatchers with OptionValues {
  val nonReserved = ReservedWords.SomeNonReservedWord
  val notRecognized = ReservedWords.Unrecognized(42)
  val unrecognizedValue = ReservedWords.Unrecognized

  "Enum isUnrecognized1 and isUnrecognized" should "return correct values" in {
    nonReserved.isUnrecognized1 must be(false)
    nonReserved.isUnrecognized must be(false)

    notRecognized.isUnrecognized1 must be(false)
    notRecognized.isUnrecognized must be(true)

    unrecognizedValue.isUnrecognized1 must be(true)
    unrecognizedValue.isUnrecognized must be(false)
  }

}
