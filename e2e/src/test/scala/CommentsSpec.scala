import com.trueaccord.proto.e2e.comments._
import org.scalatest._

class CommentsSpec extends FlatSpec with MustMatchers {
  "comments proto" should "have comments enabled" in {

    val fooLocation = Foo.scalaDescriptor.location.get
    val field1Location = Foo.scalaDescriptor.findFieldByName("field1").get.location.get
    val barLocation = Foo.Bar.scalaDescriptor.location.get
    val enumLocation = Foo.Bar.scalaDescriptor.enums(0).location.get
    val enumXyzLocation = Foo.Bar.MyBarEnum.XYZ.scalaValueDescriptor.location.get
    val enumDefLocation = Foo.Bar.MyBarEnum.DEF.scalaValueDescriptor.location.get

    fooLocation.getLeadingComments must be(" This is the foo comment\n")

    fooLocation.getTrailingComments must be(" more here\n")

    field1Location.getLeadingComments must be(" field comment A\n")

    field1Location.getTrailingComments must be(" more field comment A\n")

    barLocation.getLeadingComments must be(" Some inner comment\n")

    enumLocation.getLeadingComments must be(" MyBarEnum comment\n")

    enumXyzLocation.getLeadingComments must be(" My enum value\n")

    enumDefLocation.getLeadingComments must be(" Def comment\n")
  }
}
