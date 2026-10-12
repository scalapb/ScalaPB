package scalapb

import com.google.protobuf.wrappers.Int32Value
import munit.FunSuite

/** Small wire regressions shared by JVM, JS and Native. Keep Java oracle tests in conformance. */
class PortableWireRegressionSpec extends FunSuite {
  test("duplicate singular scalars use the last value") {
    assertEquals(Int32Value.parseFrom(Array[Byte](8, 1, 8, 2)).value, 2)
  }

  test("unknown varint fields survive a parse/serialize cycle") {
    val message = Int32Value.parseFrom(Array[Byte](8, 1, 16, 3))
    assertEquals(Int32Value.parseFrom(message.toByteArray), message)
    assert(!message.unknownFields.asMap.isEmpty)
  }

  test("a truncated varint is rejected") {
    intercept[com.google.protobuf.InvalidProtocolBufferException] {
      Int32Value.parseFrom(Array[Byte](8, 0x80.toByte))
    }
  }
}
