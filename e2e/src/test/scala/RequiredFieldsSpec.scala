import com.google.protobuf.InvalidProtocolBufferException
import com.thesamet.proto.e2e.reqs.RequiredFields
import org.scalatest._

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class RequiredFieldsSpec extends AnyFlatSpec with Matchers {
  "RequiredMessage" should "throw InvalidProtocolBufferException for empty byte array" in {
    intercept[InvalidProtocolBufferException](RequiredFields.parseFrom(Array[Byte]()))
  }
}
