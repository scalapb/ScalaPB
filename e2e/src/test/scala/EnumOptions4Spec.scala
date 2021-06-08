import com.thesamet.proto.e2e.enum_options4._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.OptionValues
import scala.reflect.runtime.universe._

class EnumOptions4Spec extends AnyFlatSpec with Matchers with OptionValues {
  "annotations for base" should "be added" in {
    typeOf[MyEnum4].typeSymbol.asClass.annotations.map(_.toString) must contain only (
      "com.thesamet.pb.CustomAnnotation"
    )
  }

  "annotations for recognized" should "be added" in {
    typeOf[MyEnum4.Recognized].typeSymbol.asClass.annotations.map(_.toString) must contain only (
      "com.thesamet.pb.CustomAnnotation1"
    )
  }

  "annotations for unrecognized" should "be added" in {
    typeOf[MyEnum4.Unrecognized].typeSymbol.asClass.annotations.map(_.toString) must contain only (
      "SerialVersionUID(value = 0L)",
      "com.thesamet.pb.CustomAnnotation3"
    )
  }

  "annotations for value" should "be added" in {
    //TODO
    //MyEnum4.E4_V1 should be annotated with "com.thesamet.pb.CustomAnnotation2"
    //MyEnum4.E4_V2 should not be annotated with "com.thesamet.pb.CustomAnnotation2"
  }
}
