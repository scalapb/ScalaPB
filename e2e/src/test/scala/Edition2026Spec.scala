import com.thesamet.proto.e2e.edition2026.{Edition2026Top, Edition2026Nested}
import com.thesamet.proto.e2e.edition2026_import.Edition2026Import
import com.google.protobuf.descriptor.FeatureSet._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2026Spec extends AnyFlatSpec with Matchers {
  "Edition 2026" should "resolve defaults and round trip imported types and DELIMITED fields" in {
    val nested: Edition2026Nested = Edition2026Nested(value = Some("edition 2026"))
    val value                     = Edition2026Top(
      explicitValue = Some(0),
      values = Seq(1, 2),
      nested = Some(nested),
      kind = Some(Edition2026Top.Kind.Unrecognized(123))
    )
    Edition2026Top.parseFrom(value.toByteArray) must be(value)
    val imported = Edition2026Import(top = Some(value), nested = Some(nested))
    Edition2026Import.parseFrom(imported.toByteArray) must be(imported)
    scalapb.TextFormat.fromAscii(Edition2026Top, scalapb.TextFormat.printToString(value)) must be(
      Right(value)
    )
    val descriptor = Edition2026Top.scalaDescriptor
    descriptor.file.resolvedFeatures.getEnforceNamingStyle must be(EnforceNamingStyle.STYLE2026)
    descriptor.file.resolvedFeatures.getDefaultSymbolVisibility must be(
      VisibilityFeature.DefaultSymbolVisibility.STRICT
    )
    descriptor.findFieldByNumber(1).get.hasPresence must be(true)
    descriptor.findFieldByNumber(2).get.hasPresence must be(false)
    descriptor.findFieldByNumber(3).get.isPacked must be(true)
    descriptor.findFieldByNumber(4).get.messageEncoding must be(MessageEncoding.DELIMITED)
  }
  it should "generate valid Scala APIs for explicit compatibility overrides" in {
    import com.thesamet.proto.e2e.edition2026_compat.Edition2026Compat
    val message = Edition2026Compat(value = Some("a"), hasValue = Some("b"))
    Edition2026Compat.parseFrom(message.toByteArray) must be(message)
  }
}
