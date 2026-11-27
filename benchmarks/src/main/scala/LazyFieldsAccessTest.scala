package scalapb.perf

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.infra.Blackhole

/** Round-trip benchmarks that materialize part of the strings between parse and serialize: fields
  * `s1` and `s2` at each of the 10 nesting levels (20 of the 100 strings in the payload). For lazy
  * messages, accessing `.value` on a `LazyField` forces the deferred UTF-8 decode; the remaining
  * 80% travel through as raw bytes. Run for the lazy ASCII / non-ASCII / no-validation payloads and
  * for the eager (LargeNestedStringMessage) payloads where the strings are already materialized
  * Strings; the Java variants exercise the same accesses through their generated getters.
  *
  * Deliberately not a TestNames entry: the shared message.scala.tmpl cannot reference concrete
  * fields, and the payloads of the generated companions are reused as-is.
  */
class LazyFieldsAccessTest {

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessEagerScala(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessEagerNonAsciiScala(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessScala(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessJava(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNonAsciiScala(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNoValidationScala(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNoValidationJava(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.NANOSECONDS)
  def roundTripAccessNonAsciiJava(blackhole: Blackhole): Array[Byte] = {
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
    blackhole.consume(total)
    msg.toByteArray
  }
}
