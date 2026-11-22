package scalapb.perf

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import java.util.concurrent.TimeUnit

/** Round-trip benchmarks that materialize part of the strings between parse and serialize: fields
  * `s1` and `s2` at each of the 10 nesting levels (20 of the 100 strings in the payload). For lazy
  * messages, accessing `.value` on a `LazyField` forces the deferred UTF-8 decode; the remaining
  * 80% travel through as raw bytes. Run for the lazy ASCII / non-ASCII / no-validation payloads and
  * for the eager (LargeNestedStringMessage) payloads where the strings are already materialized
  * Strings; the Java variants are eager baselines with no deferred-decode component.
  *
  * Deliberately not a TestNames entry: the shared message.scala.tmpl cannot reference concrete
  * fields, and the payloads of the generated companions are reused as-is.
  */
class LazyFieldsAccessTest {

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessEagerScala: Int = {
    val msg   = protos.LargeNestedStringMessage.parseFrom(LargeNestedStringMessageTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.nested.isDefined) {
      total += cur.s1.length
      total += cur.s2.length
      cur = cur.nested.get
    }
    total += cur.s1.length
    total += cur.s2.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessEagerNonAsciiScala: Int = {
    val msg =
      protos.LargeNestedStringMessageNonAscii.parseFrom(LargeNestedStringMessageNonAsciiTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.nested.isDefined) {
      total += cur.s1.length
      total += cur.s2.length
      cur = cur.nested.get
    }
    total += cur.s1.length
    total += cur.s2.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessScala: Int = {
    val msg   = protos.LazyFieldsStringMessage.parseFrom(LazyFieldsStringMessageTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.nested.isDefined) {
      total += cur.s1.value.length
      total += cur.s2.value.length
      cur = cur.nested.get
    }
    total += cur.s1.value.length
    total += cur.s2.value.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessJava: Int = {
    val msg   = Protos.LazyFieldsStringMessage.parseFrom(LazyFieldsStringMessageTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.hasNested) {
      total += cur.getS1.length
      total += cur.getS2.length
      cur = cur.getNested
    }
    total += cur.getS1.length
    total += cur.getS2.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNonAsciiScala: Int = {
    val msg   = protos.LazyFieldsNonAscii.parseFrom(LazyFieldsNonAsciiTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.nested.isDefined) {
      total += cur.s1.value.length
      total += cur.s2.value.length
      cur = cur.nested.get
    }
    total += cur.s1.value.length
    total += cur.s2.value.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNoValidationScala: Int = {
    val msg   = protos.LazyFieldsNoValidation.parseFrom(LazyFieldsNoValidationTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.nested.isDefined) {
      total += cur.getS1.value.length
      total += cur.getS2.value.length
      cur = cur.nested.get
    }
    total += cur.getS1.value.length
    total += cur.getS2.value.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNoValidationJava: Int = {
    val msg = ProtosNoValidation.LazyFieldsNoValidation.parseFrom(
      LazyFieldsNoValidationTest.bytes
    )
    var total = 0
    var cur   = msg
    while (cur.hasNested) {
      total += cur.getS1.length
      total += cur.getS2.length
      cur = cur.getNested
    }
    total += cur.getS1.length
    total += cur.getS2.length
    total + msg.toByteArray.length
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNonAsciiJava: Int = {
    val msg   = Protos.LazyFieldsNonAscii.parseFrom(LazyFieldsNonAsciiTest.bytes)
    var total = 0
    var cur   = msg
    while (cur.hasNested) {
      total += cur.getS1.length
      total += cur.getS2.length
      cur = cur.getNested
    }
    total += cur.getS1.length
    total += cur.getS2.length
    total + msg.toByteArray.length
  }
}
