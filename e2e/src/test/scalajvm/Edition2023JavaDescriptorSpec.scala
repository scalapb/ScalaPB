import com.thesamet.proto.e2e.edition2023.Edition2023Features
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2023JavaDescriptorSpec extends AnyFlatSpec with Matchers {
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
  }
}
