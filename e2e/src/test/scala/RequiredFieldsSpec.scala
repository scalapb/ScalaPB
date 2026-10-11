import com.google.protobuf.InvalidProtocolBufferException
import com.thesamet.proto.e2e.reqs.RequiredFields
import protobuf_unittest.unittest.TestEmptyMessage
import scalapb.UnknownFieldSet

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class RequiredFieldsSpec extends AnyFlatSpec with Matchers {

  private val descriptor = RequiredFields.javaDescriptor

  private def partialMessage(fields: Map[Int, Int]): Array[Byte] = {
    val fieldSet = fields.foldLeft(UnknownFieldSet.empty){ case (fieldSet, (field, value)) =>
      fieldSet
       .withField(field, UnknownFieldSet.Field(varint = Seq(value)))
    }

    TestEmptyMessage(fieldSet).toByteArray
  }

  private val allFieldsSet: Map[Int, Int] = (100 to 164).map(i => (i, i)).toMap

  "RequiredMessage" should "throw InvalidProtocolBufferException for empty byte array" in {
    val exception = intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(Array[Byte]()))

    exception.getMessage() must startWith("Message missing required fields")
  }

  it should "throw no exception when all fields are set correctly" in {
    val parsed = RequiredFields.parseFrom(partialMessage(allFieldsSet))
    parsed must be(a[RequiredFields])
    parsed.f0 must be(100)
    parsed.f64 must be(164)
  }

  it should "throw an exception if a field is missing and name the missing field" in {
    val fields = allFieldsSet.removed(123)
    val exception = intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(partialMessage(fields)))

    exception.getMessage() must be("Message missing required fields: f23")
  }

  it should "throw an exception if a multiple fields are missing and name those missing fields" in {
    val fields = allFieldsSet.removed(123).removed(164).removed(130)
    val exception = intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(partialMessage(fields)))

    exception.getMessage() must be("Message missing required fields: f23, f30, f64")
  }

  it should "sort the missing fields by field number" in {
    val fields = Map.empty[Int, Int]
    val exception = intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(partialMessage(fields)))
    val missingFields =exception.getMessage().stripPrefix("Message missing required fields: ").split(", ")

    missingFields.sortBy[Int](field => descriptor.findFieldByName(field).getNumber()) must be(missingFields)

    missingFields.toSeq mustBe Seq.tabulate(65)(i => s"f$i")
  }
}
