import com.thesamet.proto.e2e.cats_types._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import cats.data.NonEmptyList
import com.thesamet.pb.FullName
import com.thesamet.proto.e2e.collection_types.{Enum, SubMsg}

class CatsTypesSpec extends AnyFlatSpec with Matchers {
  "Non-empty types" should "throw exception in defaultInstance" in {
    intercept[RuntimeException](CollectionTypesNEL.defaultInstance)
    intercept[RuntimeException](CollectionTypesNELPacked.defaultInstance)
  }

  "Non-empty types" should "fail parsing when fields are missing" in {
    intercept[RuntimeException](CollectionTypesNEL.parseFrom(Array[Byte]()))
  }

  "Non-empty types" should "succeed when all types are set " in {
    val c1 = CollectionTypesNEL(
      repeatedInt32 = NonEmptyList.of(1),
      repeatedSint32 = NonEmptyList.of(2),
      repeatedSfixed32 = NonEmptyList.of(3),
      repeatedFixed32 = NonEmptyList.of(4),
      repeatedUint32 = NonEmptyList.of(5),
      repeatedInt64 = NonEmptyList.of(6),
      repeatedSint64 = NonEmptyList.of(7),
      repeatedSfixed64 = NonEmptyList.of(8),
      repeatedFixed64 = NonEmptyList.of(9),
      repeatedUint64 = NonEmptyList.of(10),
      repeatedFloat = NonEmptyList.of(11),
      repeatedDouble = NonEmptyList.of(12),
      repeatedString = NonEmptyList.of("boo"),
      repeatedBool = NonEmptyList.of(true),
      repeatedBytes = NonEmptyList.of(com.google.protobuf.ByteString.EMPTY),
      repeatedEnum = NonEmptyList.of(Enum.ONE),
      repeatedMsg = NonEmptyList.of(SubMsg()),
      repeatedFullname = NonEmptyList.of(FullName("X", "Y")),
    )
    CollectionTypesNEL.parseFrom(c1.toByteArray) must be(c1)
    CollectionTypesNEL.fromJavaProto(CollectionTypesNEL.toJavaProto(c1)) must be(c1)
  }
}
