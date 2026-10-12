import com.thesamet.proto.e2e.edition2023.{Edition2023Message, Edition2023Features}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2023Spec extends AnyFlatSpec with Matchers {
  "Edition 2023 messages" should "preserve explicit presence and repeated fields" in {
    val absent  = Edition2023Message()
    val present = Edition2023Message(value = Some(""), numbers = Seq(1, 2, 3))

    Edition2023Message.parseFrom(absent.toByteArray).value must be(None)
    Edition2023Message.parseFrom(present.toByteArray) must be(present)
    present.toByteArray.toSeq must be(Seq[Byte](10, 0, 18, 3, 1, 2, 3))
  }
  "Edition 2023 descriptors" should "agree with Java on supported core feature semantics" in {
    val scalaDesc = Edition2023Features.scalaDescriptor
    val javaDesc  = Edition2023Features.javaDescriptor
    scalaDesc.fields.foreach { field =>
      val javaField = javaDesc.findFieldByNumber(field.number)
      field.hasPresence must be(javaField.hasPresence)
      field.isRequired must be(javaField.isRequired)
      field.isPacked must be(javaField.isPacked)
      field.isPackable must be(javaField.isPackable)
      withClue(s"${field.name}: ${javaField.toProto} / ${field.asProto}") {
        field.needsUtf8Check must be(javaField.needsUtf8Check)
      }
    }
    scalaDesc.enums.head.isClosed must be(javaDesc.getEnumTypes.get(0).isClosed)
    scalaDesc.fields(1).isOptional must be(false)
    scalaDesc.fields(5).isMapField must be(true)
    val value = Edition2023Features(requiredValue = 42, expanded = Seq(1, 2))
    Edition2023Features.parseFrom(value.toByteArray) must be(value)
  }
}
