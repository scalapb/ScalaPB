import com.thesamet.proto.e2e.edition_group_names.EditionGroupNames
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class EditionGroupNamesJavaSpec extends AnyFlatSpec with Matchers {
  "Edition 2023 Java conversions" should "preserve group-like capitalization only for matching DELIMITED fields" in {
    val value = EditionGroupNames(
      groupliketype = Some(EditionGroupNames.GroupLikeType(Some(1))),
      lengthprefixed = Some(EditionGroupNames.LengthPrefixed(Some(2))),
      repeatedgroup = Seq(EditionGroupNames.RepeatedGroup(Some(3))),
      ordinaryName = Some(EditionGroupNames.GroupLikeType(Some(5)))
    ).withChoicegroup(EditionGroupNames.ChoiceGroup(Some(4)))
    val javaValue = EditionGroupNames.toJavaProto(value)
    javaValue.getGroupLikeType.getValue must be(1)
    javaValue.getLengthprefixed.getValue must be(2)
    javaValue.getRepeatedGroup(0).getValue must be(3)
    javaValue.getChoiceGroup.getValue must be(4)
    javaValue.getOrdinaryName.getValue must be(5)
    EditionGroupNames.fromJavaProto(javaValue) must be(value)
    javaValue.toByteArray.toSeq must be(value.toByteArray.toSeq)
  }
}
