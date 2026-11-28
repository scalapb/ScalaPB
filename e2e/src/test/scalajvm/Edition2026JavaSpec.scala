import com.thesamet.proto.e2e.edition2026.{Edition2026Top, Edition2026Nested}
import com.thesamet.proto.e2e.edition2026_import.Edition2026Import
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2026JavaSpec extends AnyFlatSpec with Matchers {
  "Edition 2026 Java interoperability" should "support mixed nesting, option imports and large enums" in {
    val nested: Edition2026Nested = Edition2026Nested(value = Some("value"))
    val value                     = Edition2026Top(
      explicitValue = Some(0),
      nested = Some(nested),
      kind = Some(Edition2026Top.Kind.KIND_ONE)
    )
    val javaValue: com.thesamet.proto.e2e.Edition2026Top = Edition2026Top.toJavaProto(value)
    val javaNested: com.thesamet.proto.e2e.Edition2026Proto.Edition2026Nested =
      Edition2026Nested.toJavaProto(nested)
    javaValue.getNested must be(javaNested)
    Edition2026Top.fromJavaProto(javaValue) must be(value)
    val unknown = value.withKind(Edition2026Top.Kind.Unrecognized(123))
    Edition2026Top.fromJavaProto(Edition2026Top.toJavaProto(unknown)) must be(unknown)
    javaValue.toByteArray.toSeq must be(value.toByteArray.toSeq)
    val imported = Edition2026Import(top = Some(value), nested = Some(nested))
    Edition2026Import.fromJavaProto(Edition2026Import.toJavaProto(imported)) must be(imported)
    Edition2026Top.javaDescriptor must be(javaValue.getDescriptorForType)
    Edition2026Nested.javaDescriptor must be(javaNested.getDescriptorForType)
    Edition2026Top.scalaDescriptor.fields.foreach { field =>
      field.hasPresence must be(
        javaValue.getDescriptorForType.findFieldByNumber(field.number).hasPresence
      )
    }
  }
}
