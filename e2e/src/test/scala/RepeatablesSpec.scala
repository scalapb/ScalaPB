import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import com.google.protobuf.CodedInputStream
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest
import com.trueaccord.proto.e2e.repeatables.RepeatablesTest.Nested
import org.scalatest._
import org.scalatest.prop._
import org.scalacheck.{Arbitrary, Gen}

class RepeatablesSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {

  val nestedGen =
    Arbitrary.arbitrary[Option[Int]].map(s => Nested(nestedField = s))

  "toString" should "give empty string" in {
    RepeatablesTest().toString must be("")
  }

  def mergeRepeatables(rep1: RepeatablesTest, rep2: RepeatablesTest) =
    RepeatablesTest(
      strings = rep1.strings ++ rep2.strings,
      ints = rep1.ints ++ rep2.ints,
      doubles = rep1.doubles ++ rep2.doubles,
      nesteds = rep1.nesteds ++ rep2.nesteds)

  val repGen = for {
    strings <- Gen.listOf(Arbitrary.arbitrary[String])
    ints <- Gen.listOf(Arbitrary.arbitrary[Int])
    doubles <- Gen.listOf(Arbitrary.arbitrary[Double])
    nesteds <- Gen.listOf(nestedGen)
  } yield RepeatablesTest(strings = strings, ints = ints, doubles = doubles, nesteds = nesteds)

  "clear" should "empty the list" in {
    forAll(repGen) {
      rep =>
        rep.clearStrings must be(rep.copy(strings = Nil))
        rep.clearInts must be(rep.copy(ints = Nil))
        rep.clearDoubles must be(rep.copy(doubles = Nil))
        rep.clearNesteds must be(rep.copy(nesteds = Nil))
    }
  }

  "with" should "set the entire list" in {
    forAll(repGen, repGen) {
      (rep, other) =>
        rep
          .withStrings(other.strings)
          .withInts(other.ints)
          .withDoubles(other.doubles)
          .withNesteds(other.nesteds) must be(other)
    }
  }

  "addAll" should "add the entire list" in {
    forAll(repGen, repGen) {
      (rep, other) =>
        rep
          .addAllStrings(other.strings)
          .addAllInts(other.ints)
          .addAllDoubles(other.doubles)
          .addAllNesteds(other.nesteds) must be(
          mergeRepeatables(rep, other))
    }
  }

  "updates" should "allow adding an entire lists" in {
    forAll(repGen, repGen) {
      (rep, other) =>
        rep
          .update(
            _.strings :++= other.strings,
            _.ints :++= other.ints,
            _.doubles :++= other.doubles,
            _.nesteds :++= other.nesteds)
        mergeRepeatables(rep, other)
    }
}

  "add" should "add one or two elements" in {
    forAll(repGen, Arbitrary.arbitrary[Int], Arbitrary.arbitrary[Int]) {
      (rep, int1, int2) =>
        rep.addInts(int1) must be (rep.copy(ints = rep.ints :+ int1))
        rep.addInts(int1, int2) must be (rep.copy(ints = rep.ints ++ Seq(int1, int2)))
    }
  }

  "update" should "add one element" in {
    forAll(repGen, Arbitrary.arbitrary[Int]) {
      (rep, int1) =>
        rep.update(_.ints :+= int1) must be (rep.copy(ints = rep.ints :+ int1))
    }
  }

  "update" should "update one element in place" in {
    forAll(repGen.suchThat(_.ints.size >= 2), Arbitrary.arbitrary[Int]) {
      (rep, int1) =>
        rep.update(_.ints(1) := int1) must be (rep.copy(ints = rep.ints.updated(1, int1)))
    }
    forAll(repGen.suchThat(_.nesteds.size >= 2), Arbitrary.arbitrary[Int]) {
      (rep, int1) =>
        rep.update(_.nesteds(1).nestedField := int1) must be (
          rep.copy(nesteds = rep.nesteds.updated(1, Nested(Some(int1)))))
    }
  }

  "parse" should "be the inverse of toByteArray" in {
    forAll(repGen) {
      rep =>
        RepeatablesTest.parseFrom(rep.toByteArray) must be(rep)
    }
  }

  "concatenate message" should "result in merged messages" in {
    forAll(repGen, repGen) {
      (rep1, rep2) =>
        RepeatablesTest.parseFrom(rep1.toByteArray ++ rep2.toByteArray) must be(
          mergeRepeatables(rep1, rep2))
    }
  }

  "parsing delimited stream" should "be the inverse of writing delimited stream" in {
    forAll(Gen.listOf(repGen)) {
      list =>
        val os = new ByteArrayOutputStream()
        list.foreach(_.writeDelimitedTo(os))
        val bytes = os.toByteArray
        val parsedStream = RepeatablesTest.streamFromDelimitedInput(new ByteArrayInputStream(bytes)).toList
        val parsedInputStream = {
          val is = new ByteArrayInputStream(bytes)
          Stream.continually(RepeatablesTest.parseDelimitedFrom(is)).takeWhile(_.isDefined).map(_.get).toList
        }
        val parsedCoded = {
          val cis = CodedInputStream.newInstance(bytes)
          Stream.continually(RepeatablesTest.parseDelimitedFrom(cis)).takeWhile(_.isDefined).map(_.get).toList
        }
        parsedStream must be(list)
        parsedInputStream must be(list)
        parsedCoded must be(list)
    }
  }
}
