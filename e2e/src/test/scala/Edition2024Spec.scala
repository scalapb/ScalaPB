import com.thesamet.proto.e2e.edition2024.{Edition2024Top, Edition2024Nested}
import com.thesamet.proto.e2e.edition2024_import.Edition2024Import
import com.google.protobuf.descriptor.FeatureSet._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class Edition2024Spec extends AnyFlatSpec with Matchers {
  "Edition 2024" should "resolve defaults and round trip imported types and DELIMITED fields" in {
    val nested: Edition2024Nested = Edition2024Nested(value = Some("edition 2024"))
    val value                     = Edition2024Top(
      explicitValue = Some(0),
      values = Seq(1, 2),
      nested = Some(nested),
      kind = Some(Edition2024Top.Kind.Unrecognized(123))
    )
    Edition2024Top.parseFrom(value.toByteArray) must be(value)
    val imported = Edition2024Import(top = Some(value), nested = Some(nested))
    Edition2024Import.parseFrom(imported.toByteArray) must be(imported)
    scalapb.TextFormat.fromAscii(Edition2024Top, scalapb.TextFormat.printToString(value)) must be(
      Right(value)
    )
    val descriptor = Edition2024Top.scalaDescriptor
    descriptor.file.resolvedFeatures.getEnforceNamingStyle must be(EnforceNamingStyle.STYLE2024)
    descriptor.file.resolvedFeatures.getDefaultSymbolVisibility must be(
      VisibilityFeature.DefaultSymbolVisibility.EXPORT_TOP_LEVEL
    )
    descriptor.findFieldByNumber(1).get.hasPresence must be(true)
    descriptor.findFieldByNumber(2).get.hasPresence must be(false)
    descriptor.findFieldByNumber(3).get.isPacked must be(true)
    descriptor.findFieldByNumber(4).get.messageEncoding must be(MessageEncoding.DELIMITED)
  }
}
