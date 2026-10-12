import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectInputStream, ObjectOutputStream}

import com.thesamet.proto.e2e.repeatables.RepeatablesTest
import com.thesamet.proto.e2e.repeatables.RepeatablesTest.Nested
import scalapb.GeneratedMessage
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class JavaSerializationSpec extends AnyFlatSpec with ScalaCheckDrivenPropertyChecks with Matchers {

  val nestedGen =
    Arbitrary.arbitrary[Option[Int]].map(s => Nested(nestedField = s))

  val oneEnum = Gen.choose(0, 1).map(i => RepeatablesTest.Enum.fromValue(i))

  val repGen = for {
    strings <- Gen.listOf(Arbitrary.arbitrary[String])
    ints    <- Gen.listOf(Arbitrary.arbitrary[Int])
    doubles <- Gen.listOf(Arbitrary.arbitrary[Double])
    nesteds <- Gen.listOf(nestedGen)
    longs   <- Gen.listOf(Arbitrary.arbitrary[Long])
    enums   <- Gen.listOf(oneEnum)
  } yield RepeatablesTest(
    strings = strings,
    ints = ints,
    doubles = doubles,
    nesteds = nesteds,
    packedLongs = longs,
    enums = enums
  )

  def checkJavaSerialization[T <: GeneratedMessage](a: T) = {
    val baos = new ByteArrayOutputStream()
    val oos  = new ObjectOutputStream(baos)

    val inputA = a.companion.parseFrom(a.toByteArray).asInstanceOf[T]

    oos.writeObject(inputA)
    oos.close()

    val ois    = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray))
    val b      = ois.readObject().asInstanceOf[T]
    val abytes = inputA.toByteArray
    val bbytes = b.toByteArray

    b mustBe inputA
    abytes mustBe bbytes
  }

  "Java serialization" should "round-trip a message" in {
    val rep = RepeatablesTest(
      strings = Seq("s1", "s2"),
      ints = Seq(14, 19),
      doubles = Seq(3.14, 2.17),
      nesteds = Seq(Nested())
    )
    checkJavaSerialization(rep)
  }

  it should "round-trip generated parsed messages" in {
    forAll(repGen) { rep =>
      checkJavaSerialization(rep)
    }
  }

  it should "preserve known and unknown packed closed enum values" in {
    val original = RepeatablesTest(enums =
      Seq(
        RepeatablesTest.Enum.ONE,
        RepeatablesTest.Enum.Unrecognized(0),
        RepeatablesTest.Enum.ONE,
        RepeatablesTest.Enum.Unrecognized(42)
      )
    )
    val parsed = RepeatablesTest.parseFrom(original.toByteArray)
    parsed.enums mustBe Seq(RepeatablesTest.Enum.ONE, RepeatablesTest.Enum.ONE)
    parsed.unknownFields.getField(7).get.varint mustBe Seq(0L, 42L)
    checkJavaSerialization(original)
  }

}
