import closed.enums.closed_enums.ClosedEnums
import closed.enums.ClosedEnumsProto
import closed.editions.edition_enums.EditionEnums
import com.google.protobuf.{ByteString, UnknownFieldSet}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ClosedEnumsJavaSpec extends AnyFlatSpec with Matchers {
  "Opted-in required enum Java conversions" should "reject an unknown-only value" in {
    import closed.enums.closed_enums.RequiredClosedEnum
    val message = RequiredClosedEnum(value = ClosedEnums.Value.Unrecognized(42))
    assertThrows[com.google.protobuf.InvalidProtocolBufferException] {
      RequiredClosedEnum.toJavaProto(message)
    }
  }

  "Closed enum Java conversions" should "retain unknown numbers and complete map entries" in {
    val unknown = UnknownFieldSet
      .newBuilder()
      .mergeVarintField(1, 42)
      .mergeVarintField(2, 42)
      .mergeVarintField(3, 42)
      .mergeVarintField(4, 42)
      .addField(
        6,
        UnknownFieldSet.Field
          .newBuilder()
          .addLengthDelimited(ByteString.copyFrom(Array[Byte](8, 7, 16, 42)))
          .build()
      )
      .build()
    val java = ClosedEnumsProto.ClosedEnums
      .newBuilder()
      .setOther(123)
      .setUnknownFields(unknown)
      .build()
    val scala = ClosedEnums.fromJavaProto(java)
    scala.choice must be(ClosedEnums.Choice.Other(123))
    scala.entries must be(Map.empty)
    scala.unknownFields.asMap.keySet must be(Set(1, 2, 3, 4, 6))
    ClosedEnums.toJavaProto(scala) must be(java)
    ClosedEnums.parseFrom(java.toByteArray) must be(scala)
  }

  it should "bridge Java's legacy closed treatment of imported open enums without losing numbers" in {
    val scala = ClosedEnums.parseFrom(Array[Byte](56, 42, 64, 42))
    scala.getImported.value must be(42)
    val java = ClosedEnums.toJavaProto(scala)
    // Java deliberately differs from the protobuf specification for this proto2 import.
    java.hasImported must be(false)
    java.getImportedRepeatedCount must be(0)
    java.getUnknownFields.getField(7).getVarintList.get(0).longValue must be(42L)
    ClosedEnums.fromJavaProto(java) must be(scala)
  }

  it should "retain closed Edition values through Java" in {
    val scala = EditionEnums.parseFrom(Array[Byte](8, 42, 16, 42, 24, 42, 48, 42))
    EditionEnums.fromJavaProto(EditionEnums.toJavaProto(scala)) must be(scala)
  }
}
