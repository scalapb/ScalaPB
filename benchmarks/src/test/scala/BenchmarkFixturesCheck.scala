package scalapb.perf

/** Run with Test/runMain scalapb.perf.BenchmarkFixturesCheck. */
object BenchmarkFixturesCheck {
  def main(args: Array[String]): Unit = {
    def sameBytes(expected: Array[Byte], actual: Array[Byte]): Unit =
      assert(java.util.Arrays.equals(expected, actual))

    val ascii = LargeNestedStringMessageTest.bytes
    sameBytes(ascii, LazyFieldsStringMessageTest.bytes)
    sameBytes(ascii, LazyFieldsNoValidationTest.bytes)
    sameBytes(ascii, LargeNestedStringMessageTest.java.toByteArray)
    sameBytes(ascii, LazyFieldsStringMessageTest.java.toByteArray)
    sameBytes(ascii, LazyFieldsNoValidationTest.java.toByteArray)

    val nonAscii = LargeNestedStringMessageNonAsciiTest.bytes
    sameBytes(nonAscii, LazyFieldsNonAsciiTest.bytes)
    sameBytes(nonAscii, LargeNestedStringMessageNonAsciiTest.java.toByteArray)
    sameBytes(nonAscii, LazyFieldsNonAsciiTest.java.toByteArray)

    // Unrelated fixture generation must not change the comparison payloads.
    TestCases.makeLargeStringMessageScala
    TestCases.makeLargeByteMessageScala
    sameBytes(ascii, TestCases.makeLargeNestedStringMessageScala.toByteArray)
    sameBytes(nonAscii, TestCases.makeLargeNestedStringMessageNonAsciiScala.toByteArray)
    println("Benchmark fixture byte equality checks passed")
  }
}
