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
  "Edition 2023 descriptors" should "resolve core feature semantics" in {
    val scalaDesc = Edition2023Features.scalaDescriptor
    scalaDesc.fields(0).hasPresence must be(false)
    scalaDesc.fields(1).isRequired must be(true)
    scalaDesc.fields(1).hasPresence must be(true)
    scalaDesc.fields(2).isPackable must be(true)
    scalaDesc.fields(2).isPacked must be(false)
    scalaDesc.fields(3).needsUtf8Check must be(false)
    scalaDesc.enums.head.isClosed must be(true)
    scalaDesc.fields(1).isOptional must be(false)
    scalaDesc.fields(5).isMapField must be(true)
    val value = Edition2023Features(requiredValue = 42, expanded = Seq(1, 2))
    Edition2023Features.parseFrom(value.toByteArray) must be(value)
  }
  "Edition 2023 text enums" should "preserve unknown open values and reject unknown closed values" in {
    import com.thesamet.proto.e2e.edition2023.Edition2023OpenEnums
    val parsed = scalapb.TextFormat.fromAscii(Edition2023OpenEnums, "value: 123 values: [0, -7]")
    parsed must be(
      Right(
        Edition2023OpenEnums(
          value = Some(Edition2023OpenEnums.Open.Unrecognized(123)),
          values = Seq(Edition2023OpenEnums.Open.ZERO, Edition2023OpenEnums.Open.Unrecognized(-7))
        )
      )
    )
    scalapb.TextFormat
      .fromAscii(Edition2023Features, "required_value: 1 closed: 123")
      .isLeft must be(true)
    scalapb.TextFormat.fromAscii(Edition2023OpenEnums, "value: 2147483648").isLeft must be(true)
  }
}
