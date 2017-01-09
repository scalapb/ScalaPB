import com.trueaccord.proto.e2e.one_of._
import org.scalatest._
import org.scalatest.prop._
import org.scalacheck.Gen
import collection.JavaConversions._
import Matchers._

class OneofSpec extends FlatSpec with GeneratorDrivenPropertyChecks with MustMatchers {
  val unspecified = OneofTest()
  val tempField = OneofTest(myOneOf = OneofTest.MyOneOf.TempField(9))
  val otherField = OneofTest(myOneOf = OneofTest.MyOneOf.OtherField("boo"))
  val sub = OneofTest(myOneOf = OneofTest.MyOneOf.Sub(OneofTest.SubMessage(subField = Some(18))))

  "oneofs" should "serialize and parse" in {
    OneofTest.parseFrom(unspecified.toByteArray) must be(unspecified)
    OneofTest.parseFrom(tempField.toByteArray) must be(tempField)
    OneofTest.parseFrom(otherField.toByteArray) must be(otherField)
    OneofTest.parseFrom(sub.toByteArray) must be(sub)
  }

  "oneof.isX function" should "return correct value" in {
    unspecified.myOneOf shouldBe 'isEmpty
    unspecified.myOneOf should not be 'isDefined
    unspecified.myOneOf should not be 'isTempField
    unspecified.myOneOf should not be 'isOtherField
    unspecified.myOneOf should not be 'isSub
    tempField.myOneOf shouldBe 'isTempField
    tempField.myOneOf shouldBe 'isDefined
    tempField.myOneOf should not be 'isEmpty
    tempField.myOneOf should not be 'isOtherField
    tempField.myOneOf should not be 'isSub
  }

  "oneof.number function" should "return correct value" in {
    unspecified.myOneOf.number shouldBe 0
    tempField.myOneOf.number shouldBe 2
    otherField.myOneOf.number shouldBe 3
    sub.myOneOf.number shouldBe 4
  }

  "oneOf matching" should "work" in {
    (sub.myOneOf match {
        case OneofTest.MyOneOf.Sub(subm) => subm.getSubField
        case _ => 4
    }) must be(18)

    (tempField.myOneOf match {
        case OneofTest.MyOneOf.TempField(17) => "foo"
        case OneofTest.MyOneOf.TempField(9) => "bar"
        case OneofTest.MyOneOf.TempField(_) => "baz"
        case _ => "bang"
    }) must be("bar")

    (unspecified.myOneOf match {
      case OneofTest.MyOneOf.Empty => "unset"
      case _ => "boo"
    }) must be("unset")
  }

  "clearMyOneOf" should "unset the oneof" in {
    tempField.clearMyOneOf should be(unspecified)
    unspecified.clearMyOneOf should be(unspecified)
    otherField.clearMyOneOf should be(unspecified)
    sub.clearMyOneOf should be(unspecified)
  }

  "withField" should "set the one off" in {
    otherField.withTempField(9) should be(tempField)
    tempField.withOtherField("boo") should be(otherField)
    otherField.withOtherField("boo") should be(otherField)
    otherField.withOtherField("zoo") should not be(otherField)
    otherField.withOtherField("zoo").myOneOf.otherField should be(Some("zoo"))
  }

  "withOneOf" should "set the one off" in {
    tempField.withMyOneOf(otherField.myOneOf) should be(otherField)
    otherField.withMyOneOf(tempField.myOneOf) should be(tempField)
  }

  "oneOf option getters" should "work" in {
    tempField.myOneOf.tempField must be(Some(9))
    tempField.myOneOf.otherField must be(None)
    tempField.myOneOf.sub must be(None)
    sub.myOneOf.sub must be(Some(OneofTest.SubMessage(subField = Some(18))))
    sub.myOneOf.tempField must be(None)
    sub.myOneOf.otherField must be(None)
  }

  "oneOf update" should "allow updating the one of" in {
    val obj1 = tempField.update(
        _.myOneOf := otherField.myOneOf)
    obj1 must be(otherField)

    val obj2 = tempField.update(
        _.myOneOf := otherField.myOneOf,
        _.otherField.modify(_ + "zoo"))
    obj2.myOneOf.otherField must be(Some("boozoo"))
  }

  "oneOf update" should "update fields inside one of" in {
    val obj = tempField.update(
        _.sub.name := "Hi",
        _.sub.subField := 4)
    obj.myOneOf.tempField must be(None)
    obj.myOneOf.isTempField must be(false)
    obj.myOneOf.sub must be(Some(OneofTest.SubMessage(
        name = Some("Hi"), subField = Some(4))))
  }

  "oneOf update" should "make use of defaults" in {
    val obj = unspecified.update(
        _.otherField.modify(_ + " Yo!"))
    obj.myOneOf.otherField must be(Some("Other value Yo!"))
  }

  "oneof parser" should "pick last oneof value" in {
    forAll(Gen.listOf(
      Gen.oneOf(unspecified, tempField, otherField))) { l =>
      val concat = l.map(_.toByteArray.toSeq).foldLeft(Seq[Byte]())(_ ++ _).toArray
      val parsed = OneofTest.parseFrom(concat)
      val expectedOneOf = l.reverse.collectFirst {
        case e if e != unspecified => e.myOneOf
      } getOrElse(unspecified.myOneOf)
      parsed.myOneOf must be(expectedOneOf)
    }
  }

  "oneof field descriptors" should "give the right containing name" in {
    for (fieldDescriptor <- OneofTest.javaDescriptor.getFields) {
      if (fieldDescriptor.getNumber > 1) {
        fieldDescriptor.getContainingOneof.getName must be("my_one_of")
      }
    }
  }
}
