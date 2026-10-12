import closed.enums.closed_enums.{ClosedEnums, RequiredClosedEnum}
import closed.editions.edition_enums.EditionEnums
import com.google.protobuf.{ByteString, CodedOutputStream, InvalidProtocolBufferException}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ClosedEnumsSpec extends AnyFlatSpec with Matchers {
  private def wire(f: CodedOutputStream => Unit): Array[Byte] = {
    val bytes = new java.io.ByteArrayOutputStream()
    val out   = CodedOutputStream.newInstance(bytes)
    f(out)
    out.flush()
    bytes.toByteArray
  }
  private def enums(field: Int, values: Int*): Array[Byte] =
    wire(out => values.foreach(out.writeEnum(field, _)))
  private def packed(field: Int, values: Int*): Array[Byte] =
    wire(_.writeByteArray(field, wire(out => values.foreach(out.writeEnumNoTag))))
  private def entry(key: Int, value: Int): Array[Byte] = wire { out =>
    out.writeInt32(1, key)
    out.writeEnum(2, value)
  }

  "Closed enums" should "leave unknown singular values unset and retain their signed numbers" in {
    val m = ClosedEnums.parseFrom(enums(1, 42, -2))
    m.singular must be(None)
    m.getSingular must be(ClosedEnums.Value.ONE)
    m.unknownFields.getField(1).get.varint must be(Seq(42L, -2L))
    ClosedEnums.parseFrom(m.toByteArray) must be(m)
    ClosedEnums.parseFrom(enums(1, 1, 42)).singular must be(Some(ClosedEnums.Value.ONE))
    ClosedEnums.parseFrom(enums(1, -1)).getSingular must be(ClosedEnums.Value.NEGATIVE)
  }

  it should "split recognized and unknown values in either repeated encoding" in {
    for (field <- Seq(2, 3); encode <- Seq(enums _, packed _)) {
      val m      = ClosedEnums.parseFrom(encode(field, Seq(0, 42, 1, -2, -1)))
      val values = if (field == 2) m.unpacked else m.packed
      values.map(_.value) must be(Seq(0, 1, -1))
      m.unknownFields.getField(field).get.varint must be(Seq(42L, -2L))
      ClosedEnums.parseFrom(m.toByteArray) must be(m)
    }
  }

  it should "avoid selecting or clearing a oneof for an unknown number" in {
    ClosedEnums.parseFrom(enums(4, 42)).choice must be(ClosedEnums.Choice.Empty)
    val m = ClosedEnums.parseFrom(enums(5, 123) ++ enums(4, 42))
    m.choice must be(ClosedEnums.Choice.Other(123))
    m.unknownFields.getField(4).get.varint must be(Seq(42L))
    ClosedEnums.parseFrom(enums(4, 1, 42)).choice must be(
      ClosedEnums.Choice.Selected(ClosedEnums.Value.ONE)
    )
  }

  it should "preserve the complete unknown map entry without replacing a known key" in {
    val rejected = entry(7, 42) ++ enums(10, 999)
    val bytes    = wire { out =>
      out.writeByteArray(6, entry(7, 1))
      out.writeByteArray(6, rejected)
      out.writeByteArray(6, entry(8, -2))
      out.writeByteArray(6, enums(1, 9)) // omitted value uses the first declared enum value
    }
    val m = ClosedEnums.parseFrom(bytes)
    m.entries must be(Map(7 -> ClosedEnums.Value.ONE, 9 -> ClosedEnums.Value.ZERO))
    m.unknownFields.getField(6).get.lengthDelimited must be(
      Seq(ByteString.copyFrom(rejected), ByteString.copyFrom(entry(8, -2)))
    )
    ClosedEnums.parseFrom(m.toByteArray) must be(m)
  }

  it should "use the final map value and reject malformed entries" in {
    val recognizedLast = entry(7, 42) ++ enums(2, 1)
    val unknownLast    = entry(8, 1) ++ enums(2, 42)
    val m              = ClosedEnums.parseFrom(wire { out =>
      out.writeByteArray(6, recognizedLast)
      out.writeByteArray(6, unknownLast)
    })
    m.entries must be(Map(7 -> ClosedEnums.Value.ONE))
    m.unknownFields.getField(6).get.lengthDelimited must be(Seq(ByteString.copyFrom(unknownLast)))
    for (bad <- Seq(Array[Byte](16, 42, 0), Array[Byte](16, 42, 12), Array[Byte](16, -128))) {
      intercept[InvalidProtocolBufferException](
        ClosedEnums.parseFrom(wire(_.writeByteArray(6, bad)))
      )
    }
  }

  it should "not satisfy required fields with unknown values" in {
    intercept[InvalidProtocolBufferException](RequiredClosedEnum.parseFrom(enums(1, 42)))
    RequiredClosedEnum.parseFrom(enums(1, 1, 42)).value must be(ClosedEnums.Value.ONE)
  }

  it should "keep imported proto3 enums open in a proto2 message" in {
    val m = ClosedEnums.parseFrom(enums(7, 42) ++ packed(8, 42, -2))
    m.getImported.value must be(42)
    m.importedRepeated.map(_.value) must be(Seq(42, -2))
    m.unknownFields must be(scalapb.UnknownFieldSet.empty)
  }

  it should "reject unknown values before invoking custom type mappers" in {
    val m = closed.enums.closed_enums.MappedClosedEnums.parseFrom(
      enums(1, 42) ++ packed(2, 0, 42, 1) ++ enums(3, 42) ++
        wire(_.writeByteArray(4, entry(7, 42)))
    )
    m.singular must be(None)
    m.repeated.map(_.value.value) must be(Seq(0, 1))
    m.choice.isEmpty must be(true)
    m.entries must be(Map.empty)
    m.unknownFields.asMap.keySet must be(Set(1, 2, 3, 4))
  }

  it should "discard rejected values when unknown field preservation is disabled" in {
    val m = closed.discard.discard_closed_enums.DiscardClosedEnums.parseFrom(
      enums(1, 42) ++ packed(2, 0, 42, 1) ++ enums(3, 42) ++
        wire(_.writeByteArray(4, entry(7, 42)))
    )
    m.singular must be(None)
    m.packed.map(_.value) must be(Seq(0, 1))
    m.choice.isEmpty must be(true)
    m.entries must be(Map.empty)
  }

  "Edition enums" should "honor inherited features and explicit overrides" in {
    val m = EditionEnums.parseFrom(
      enums(1, 42) ++ packed(2, 0, 42, 1) ++ enums(4, 9) ++
        enums(3, 42) ++ wire(_.writeByteArray(5, entry(7, 42))) ++ enums(6, 42)
    )
    m.singular must be(None)
    m.packed.map(_.value) must be(Seq(0, 1))
    m.choice must be(EditionEnums.Choice.Other(9))
    m.entries must be(Map.empty)
    m.getOpen.value must be(42)
    m.unknownFields.asMap.keySet must be(Set(1, 2, 3, 5))
    EditionEnums.parseFrom(m.toByteArray) must be(m)
    val nested = EditionEnums.Nested.parseFrom(enums(1, 42) ++ enums(2, 42))
    nested.getOpen.value must be(42)
    nested.closed must be(None)
    EditionEnums.Closed.scalaDescriptor.isClosed must be(true)
    EditionEnums.Open.scalaDescriptor.isClosed must be(false)
    EditionEnums.Nested.InheritedClosed.scalaDescriptor.isClosed must be(true)
    EditionEnums.Nested.OverrideOpen.scalaDescriptor.isClosed must be(false)
    EditionEnums.Nested.OverrideClosed.scalaDescriptor.isClosed must be(true)
    ClosedEnums.Value.scalaDescriptor.isClosed must be(true)
  }

  it should "keep unknown open values in repeated fields, oneofs, and maps" in {
    val m = EditionEnums.parseFrom(
      packed(7, 0, 42, -2) ++ enums(8, 42) ++
        wire(_.writeByteArray(9, entry(7, 42)))
    )
    m.openPacked.map(_.value) must be(Seq(0, 42, -2))
    m.openChoice.openSelected.get.value must be(42)
    m.openEntries(7).value must be(42)
    m.unknownFields must be(scalapb.UnknownFieldSet.empty)
    EditionEnums.parseFrom(m.toByteArray) must be(m)
  }

  it should "validate UTF-8 map keys even when their enum value is rejected" in {
    for (value <- Seq(0, 42)) {
      val invalidKey = Array[Byte](10, 1, -1, 16, value.toByte)
      intercept[InvalidProtocolBufferException](
        EditionEnums.parseFrom(wire(_.writeByteArray(10, invalidKey)))
      )
    }
  }

}
