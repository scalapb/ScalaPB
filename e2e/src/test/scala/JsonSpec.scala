
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest.Nested
import org.scalatest._
import org.scalatest.prop._
import org.scalacheck.{Arbitrary, Gen}
import com.trueaccord.scalapb.json.JsonFormat

class JsonSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {

  val nestedGen =
    Arbitrary.arbitrary[Option[Int]].map(s => Nested(nestedField = s))

  val repGen = for {
    strings <- Gen.listOf(Arbitrary.arbitrary[String])
    ints <- Gen.listOf(Arbitrary.arbitrary[Int])
    doubles <- Gen.listOf(Arbitrary.arbitrary[Double])
    nesteds <- Gen.listOf(nestedGen)
  } yield RepeatablesTest(strings = strings, ints = ints, doubles = doubles, nesteds = nesteds)

  "fromJson" should "invert toJson (single)" in {
    val rep = RepeatablesTest(strings=Seq("s1", "s2"), ints=Seq(14, 19), doubles=Seq(3.14, 2.17), nesteds=Seq(Nested()))
    val j = JsonFormat.toJson(rep)
    JsonFormat.fromJson[RepeatablesTest](j) must be (rep)
  }

  "fromJson" should "invert toJson" in {
    forAll(repGen) {
      rep =>
        val j = JsonFormat.toJson(rep)
        JsonFormat.fromJson[RepeatablesTest](j) must be (rep)
    }
  }
}
