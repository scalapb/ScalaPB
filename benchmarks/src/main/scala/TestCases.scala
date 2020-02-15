package scalapb.perf

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.OutputTimeUnit
import scalapb.perf.{Protos => JavaProtos}
import scalapb.GeneratedMessage
import com.google.protobuf.ByteString
import com.google.protobuf.{GeneratedMessageV3 => JGeneratedMessage}

object TestCases {
  def makeMessageContainerScala =
    protos.MessageContainer(
      opt = Some(
        protos.SimpleMessage(
          i = 14,
          j = 9,
          k = ByteString.copyFromUtf8("foobar"),
          color = protos.Color.RED
        )
      ),
      rep = (for {
        i <- 1 to 20
        j <- 1 to 20
      } yield protos.SimpleMessage(
        i = i * 31 + j,
        j = j * 17 + i,
        k = ByteString.copyFromUtf8(s"foo_${i}_${j}"),
        color = protos.Color.fromValue((i + j) % 4)
      )).toList
    )

  def makeSimpleMessageScala = protos.SimpleMessage(
    i = 14,
    j = 9,
    k = ByteString.copyFromUtf8("foobar"),
    color = protos.Color.RED
  )

  def makeEnumScala = protos.Enum(protos.Color.RED)

  def makeEnumVectorScala =
    protos.EnumVector(colors = (0 to 1000).map(i => protos.Color.fromValue(i % 4)).toVector)

  def makeIntVectorScala = protos.IntVector(ints = (0 to 100).toVector)

  def makeStringMessageScala = protos.StringMessage(
    "foobar",
    "foobaz"
  )
}
