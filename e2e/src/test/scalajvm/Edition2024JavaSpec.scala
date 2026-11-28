import com.thesamet.proto.e2e.edition2024.{Edition2024Top, Edition2024Nested}
import com.thesamet.proto.e2e.edition2024_import.Edition2024Import
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2024JavaSpec extends AnyFlatSpec with Matchers {
  "Edition 2024 Java interoperability" should "support mixed nesting, option imports and large enums" in {
    val nested: Edition2024Nested = Edition2024Nested(value = Some("value"))
    val value                     = Edition2024Top(
      explicitValue = Some(0),
      nested = Some(nested),
      kind = Some(Edition2024Top.Kind.KIND_ONE)
    )
    val javaValue: com.thesamet.proto.e2e.Edition2024Top = Edition2024Top.toJavaProto(value)
    val javaNested: com.thesamet.proto.e2e.Edition2024Proto.Edition2024Nested =
      Edition2024Nested.toJavaProto(nested)
    javaValue.getNested must be(javaNested)
    Edition2024Top.fromJavaProto(javaValue) must be(value)
    val unknown = value.withKind(Edition2024Top.Kind.Unrecognized(123))
    Edition2024Top.fromJavaProto(Edition2024Top.toJavaProto(unknown)) must be(unknown)
    javaValue.toByteArray.toSeq must be(value.toByteArray.toSeq)
    val imported = Edition2024Import(top = Some(value), nested = Some(nested))
    Edition2024Import.fromJavaProto(Edition2024Import.toJavaProto(imported)) must be(imported)
    Edition2024Top.javaDescriptor must be(javaValue.getDescriptorForType)
    Edition2024Nested.javaDescriptor must be(javaNested.getDescriptorForType)
    Edition2024Top.scalaDescriptor.fields.foreach { field =>
      field.hasPresence must be(
        javaValue.getDescriptorForType.findFieldByNumber(field.number).hasPresence
      )
    }
  }
}
