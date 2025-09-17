import org.scalatest._
import org.scalatestplus.scalacheck._
import org.scalacheck.Gen
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import com.thesamet.proto.e2e.one_of._
import com.thesamet.proto.e2e.oneof_named_field._

class OneofSpec
    extends AnyFlatSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with OptionValues {
  val unspecified = OneofTest()
  val tempField   = OneofTest(myOneOf = OneofTest.MyOneOf.TempField(9))
  val otherField  = OneofTest(myOneOf = OneofTest.MyOneOf.OtherField("boo"))
  val subMessage  = OneofTest.SubMessage(subField = Some(18))
  val sub         = OneofTest(myOneOf = OneofTest.MyOneOf.Sub(subMessage))

  "oneofs" should "serialize and parse" in {
    OneofTest.parseFrom(unspecified.toByteArray) must be(unspecified)
    OneofTest.parseFrom(tempField.toByteArray) must be(tempField)
    OneofTest.parseFrom(otherField.toByteArray) must be(otherField)
    OneofTest.parseFrom(sub.toByteArray) must be(sub)
  }

  "oneofs.scala_name" should "set the scala field name" in {
    NamedOneOf().myField must be(NamedOneOf.MyOneof.Empty)
  }

  "oneof.isX function" should "return correct value" in {
    unspecified.myOneOf mustBe empty
    unspecified.myOneOf must not be defined
    unspecified.myOneOf.isTempField must be(false)
    unspecified.myOneOf.isOtherField must be(false)
    unspecified.myOneOf.isSub must be(false)
    tempField.myOneOf.isTempField mustBe (true)
    tempField.myOneOf mustBe defined
    tempField.myOneOf must not be empty
    tempField.myOneOf.isOtherField mustBe (false)
    tempField.myOneOf.isSub must be(false)
  }

  "oneof.number function" should "return correct value" in {
    unspecified.myOneOf.number mustBe 0
    tempField.myOneOf.number mustBe 2
    otherField.myOneOf.number mustBe 3
    sub.myOneOf.number mustBe 4
  }

  "oneof.valueOption function" should "return correct value" in {
    unspecified.myOneOf.valueOption mustBe None
    tempField.myOneOf.valueOption mustBe Some(9)
    otherField.myOneOf.valueOption mustBe Some("boo")
    sub.myOneOf.valueOption mustBe Some(subMessage)
  }

  "oneof.value function" should "return correct value" in {
    assertThrows[java.util.NoSuchElementException] { unspecified.myOneOf.value }
    tempField.myOneOf.value mustBe (9)
    otherField.myOneOf.value mustBe ("boo")
    sub.myOneOf.value mustBe (subMessage)
  }

  "oneOf matching" should "work" in {
    (sub.myOneOf match {
      case OneofTest.MyOneOf.Sub(subm) => subm.getSubField
      case _                           => 4
    }) must be(18)

    (tempField.myOneOf match {
      case OneofTest.MyOneOf.TempField(17) => "foo"
      case OneofTest.MyOneOf.TempField(9)  => "bar"
      case OneofTest.MyOneOf.TempField(_)  => "baz"
      case _                               => "bang"
    }) must be("bar")

    (unspecified.myOneOf match {
      case OneofTest.MyOneOf.Empty => "unset"
      case _                       => "boo"
    }) must be("unset")
  }

  "clearMyOneOf" should "unset the oneof" in {
    tempField.clearMyOneOf must be(unspecified)
    unspecified.clearMyOneOf must be(unspecified)
    otherField.clearMyOneOf must be(unspecified)
    sub.clearMyOneOf must be(unspecified)
  }

  "withField" should "set the one off" in {
    otherField.withTempField(9) must be(tempField)
    tempField.withOtherField("boo") must be(otherField)
    otherField.withOtherField("boo") must be(otherField)
    otherField.withOtherField("zoo") must not be (otherField)
    otherField.withOtherField("zoo").myOneOf.otherField must be(Some("zoo"))
  }

  "withOneOf" should "set the one off" in {
    tempField.withMyOneOf(otherField.myOneOf) must be(otherField)
    otherField.withMyOneOf(tempField.myOneOf) must be(tempField)
  }

  "oneOf option getters" should "work" in {
    tempField.myOneOf.tempField must be(Some(9))
    tempField.myOneOf.otherField must be(None)
    tempField.myOneOf.sub must be(None)
    sub.myOneOf.sub must be(Some(subMessage))
    sub.myOneOf.tempField must be(None)
    sub.myOneOf.otherField must be(None)
  }

  "oneOf update" should "allow updating the one of" in {
    val obj1 = tempField.update(_.myOneOf := otherField.myOneOf)
    obj1 must be(otherField)

    val obj2 = tempField.update(_.myOneOf := otherField.myOneOf, _.otherField.modify(_ + "zoo"))
    obj2.myOneOf.otherField must be(Some("boozoo"))
  }

  "oneOf update" should "update fields inside one of" in {
    val obj = tempField.update(_.sub.name := "Hi", _.sub.subField := 4)
    obj.myOneOf.tempField must be(None)
    obj.myOneOf.isTempField must be(false)
    obj.myOneOf.sub must be(Some(OneofTest.SubMessage(name = Some("Hi"), subField = Some(4))))
  }

  "oneOf update" should "make use of defaults" in {
    val obj = unspecified.update(_.otherField.modify(_ + " Yo!"))
    obj.myOneOf.otherField must be(Some("Other value Yo!"))
  }

  "oneof parser" should "pick last oneof value" in {
    forAll(Gen.listOf(Gen.oneOf(unspecified, tempField, otherField))) { l =>
      val concat        = l.map(_.toByteArray.toSeq).foldLeft(Seq[Byte]())(_ ++ _).toArray
      val parsed        = OneofTest.parseFrom(concat)
      val expectedOneOf = l.reverse.collectFirst {
        case e if e != unspecified => e.myOneOf
      } getOrElse (unspecified.myOneOf)
      parsed.myOneOf must be(expectedOneOf)
    }
  }

  "oneof field descriptors" should "give the right containing name" in {
    for (fieldDescriptor <- OneofTest.scalaDescriptor.fields) {
      if (fieldDescriptor.number >= 2 && fieldDescriptor.number <= 4) {
        fieldDescriptor.containingOneof.value.name must be("my_one_of")
      }
    }
  }
}
