import com.google.protobuf.CodedInputStream
import com.thesamet.proto.e2e.unknown_fields._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.UnknownFieldSet

class UnknownFieldSetSpec extends AnyFlatSpec with Matchers {
  val baseX = BaseCase(x = "x", y = 12)
  val baseY = BaseCase(x = "y", y = 31)
  val baseZ = BaseCase(x = "z", y = 47)
  val ext   = Extended(
    extInt = 4,
    extRepStr = Seq("foo", "bar"),
    extMsg = Some(baseX),
    extRepMsg = Seq(baseY, baseZ)
  )

  "Unknown fields" should "be accessible through GeneratedMessage for every message kind" in {
    val preserved: scalapb.GeneratedMessage = BaseCase.parseFrom(ext.toByteArray)
    preserved.unknownFields must not be UnknownFieldSet.empty

    val discarded: scalapb.GeneratedMessage =
      com.thesamet.proto.e2e.issue1664.NestedExampleEvent.defaultInstance
    val valueClass: scalapb.GeneratedMessage =
      com.thesamet.proto.e2e.sealed_oneof_extends.universal.BasketBallPlayer(1)
    discarded.unknownFields must be(UnknownFieldSet.empty)
    valueClass.unknownFields must be(UnknownFieldSet.empty)
    scalapb.TextFormat.printToString(discarded, true) must be(
      scalapb.TextFormat.printToString(discarded)
    )
    scalapb.TextFormat.printToString(valueClass, true) must be(
      scalapb.TextFormat.printToString(valueClass)
    )
  }

  "Unknown fields" should "be preserved when merging" in {
    val mergedBase = BaseCase.merge(baseX, CodedInputStream.newInstance(ext.toByteArray))

    Extended.parseFrom(mergedBase.toByteArray).withUnknownFields(UnknownFieldSet.empty) must be(ext)
  }

  "Unknown fields" should "work in concatenated byte arrays" in {
    val mergedBase = BaseCase.parseFrom(baseX.toByteArray ++ ext.toByteArray)

    Extended.parseFrom(mergedBase.toByteArray).withUnknownFields(UnknownFieldSet.empty) must be(ext)
  }

  "Unknown fields" should "be combined" in {
    val mergedBase     = BaseCase.parseFrom(baseX.toByteArray ++ ext.toByteArray)
    val mergedOnceMore =
      BaseCase.merge(mergedBase, CodedInputStream.newInstance(Extended(extInt = 35).toByteArray))

    Extended.parseFrom(mergedOnceMore.toByteArray).withUnknownFields(UnknownFieldSet.empty) must be(
      ext.withExtInt(35)
    )
  }

  "Unknown fields" should "stay untouched when merging with known fields" in {
    val mergedBase     = BaseCase.parseFrom(baseX.toByteArray ++ ext.toByteArray)
    val mergedOnceMore = BaseCase.merge(mergedBase, CodedInputStream.newInstance(baseY.toByteArray))

    Extended.parseFrom(mergedOnceMore.toByteArray).withUnknownFields(UnknownFieldSet.empty) must be(
      ext
    )
  }
}
