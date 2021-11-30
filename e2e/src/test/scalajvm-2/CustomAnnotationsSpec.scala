import scala.reflect.runtime.universe._
import com.thesamet.proto.e2e.custom_options_use._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CustomAnnotationsSpec extends AnyFlatSpec with Matchers {
  "CustomAnnotation" should "exist" in {
    val annotations = typeOf[FooMessage].typeSymbol.asClass.annotations
    annotations.count(_.toString == "com.thesamet.pb.CustomAnnotation") must be(1)
  }

  "CustomAnnotation, CustomAnnotation1, CustomAnnotation2" should "exist" in {
    val annotations = typeOf[BarMessage].typeSymbol.asClass.annotations.map(_.toString)
    annotations must contain allOf (
      "com.thesamet.pb.CustomAnnotation",
      "com.thesamet.pb.CustomAnnotation1",
      "com.thesamet.pb.CustomAnnotation2"
    )
  }

  "field annotations" should "be set correctly" in {
    val message =
      typeOf[FieldAnnotations]
        .member(TermName("z"))
        .annotations
        .map(_.toString)
        .filter(_.contains("deprecated"))
        .head
    // Formatting is different between Scala 2.13 and Scala 2.12, so testing
    // the relevant text exists, but without specific formatting.
    message must include("deprecated")
    message must include("Will be removed")
    message must include("0.1")
  }

  "companion annotations" should "be set correctly" in {
    typeOf[FooMessage.type].typeSymbol.asClass.annotations.map(_.toString) must contain only (
      "com.thesamet.pb.CustomAnnotation1",
      "com.thesamet.pb.CustomAnnotation2"
    )
  }

  "unknownFields annotations" should "be set correctly" in {
    typeOf[FieldAnnotations]
      .member(TermName("unknownFields"))
      .annotations
      .map(_.toString) must contain allOf (
      "com.thesamet.pb.CustomFieldAnnotation1",
      "com.thesamet.pb.CustomFieldAnnotation2"
    )
  }
}
