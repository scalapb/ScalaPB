
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.trueaccord.proto.e2e.repeatables.RepeatablesTest
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest.Nested
import com.trueaccord.scalapb.GeneratedMessage
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest._
import org.scalatest.prop._

class JavaSerializationSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {

  val nestedGen =
    Arbitrary.arbitrary[Option[Int]].map(s => Nested(nestedField = s))

  val oneEnum = Gen.choose(0, 1).map(i => RepeatablesTest.Enum.fromValue(i))

  val repGen = for {
    strings <- Gen.listOf(Arbitrary.arbitrary[String])
    ints <- Gen.listOf(Arbitrary.arbitrary[Int])
    doubles <- Gen.listOf(Arbitrary.arbitrary[Double])
    nesteds <- Gen.listOf(nestedGen)
    longs <- Gen.listOf(Arbitrary.arbitrary[Long])
    enums <- Gen.listOf(oneEnum)
  } yield RepeatablesTest(
    strings = strings, ints = ints, doubles = doubles,
    nesteds = nesteds, packedLongs = longs, enums = enums
  )

  def checkJavaSerialization[T <: GeneratedMessage](a: T) = {
    val baos = new ByteArrayOutputStream()
    val oos = new ObjectOutputStream(baos)

    val inputA = a.companion.parseFrom(a.toByteArray)

    oos.writeObject(inputA)
    oos.close()

    val ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    val b = ois.readObject().asInstanceOf[T]
    val abytes = a.toByteArray
    val bbytes = b.toByteArray

    abytes mustBe bbytes
  }

  "fromJson" should "invert toJson (single)" in {
    val rep = RepeatablesTest(strings=Seq("s1", "s2"), ints=Seq(14, 19), doubles=Seq(3.14, 2.17), nesteds=Seq(Nested()))
    checkJavaSerialization(rep)
  }

  "fromJson" should "invert toJson" in {
    forAll(repGen) {
      rep => checkJavaSerialization(rep)
    }
  }
}
