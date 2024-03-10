package scalapb.perf

import com.google.protobuf.CodedOutputStream
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.util.NullOutputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Fork

import scalapb.GeneratedMessage
import scalapb.GeneratedMessageCompanion
import scalapb.JavaProtoSupport
import com.google.protobuf.Message
import com.google.protobuf.Parser

@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Fork(1)
abstract class ProtobufBenchmark[S <: GeneratedMessage, J <: Message](javaParser: Parser[J])(implicit
    cmp: GeneratedMessageCompanion[S] with JavaProtoSupport[S, J]
) {
  def sample: S

  var message: S             = _
  var javaMessage: J         = _
  var bytes: Array[Byte]     = _
  var out: CodedOutputStream = _

  @Setup(Level.Trial)
  def setup(): Unit = {
    message = sample
    javaMessage = cmp.toJavaProto(message)
    bytes = {
      val baos = new ByteArrayOutputStream
      val cos  = CodedOutputStream.newInstance(baos)
      message.writeTo(cos)
      cos.flush()
      baos.toByteArray()
    }
    out = CodedOutputStream.newInstance(new NullOutputStream)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def writeScala(): Unit = {
    message.writeTo(out)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def readScala(bh: Blackhole): Unit = {
    bh.consume(cmp.parseFrom(bytes))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def proxyScala(): Unit = {
    cmp.parseFrom(bytes).writeTo(out)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def writeJava(): Unit = {
    javaMessage.writeTo(out)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def readJava(bh: Blackhole): Unit = {
    bh.consume(javaParser.parseFrom(bytes))
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.SampleTime))
  final def proxyJava(): Unit = {
    javaParser.parseFrom(bytes).writeTo(out)
  }
}
