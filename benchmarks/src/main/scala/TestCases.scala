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
import scala.util.Random

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

  def makeLargeStringMessageScala: protos.LargeStringMessage = {
    def randomString(len: Int): String = Random.alphanumeric.take(len).mkString

    def makeStrings10(): protos.Strings10 = {
      protos.Strings10(
        s1 = randomString(10 + Random.nextInt(91)),
        s2 = randomString(10 + Random.nextInt(91)),
        s3 = randomString(10 + Random.nextInt(91)),
        s4 = randomString(10 + Random.nextInt(91)),
        s5 = randomString(10 + Random.nextInt(91)),
        s6 = randomString(10 + Random.nextInt(91)),
        s7 = randomString(10 + Random.nextInt(91)),
        s8 = randomString(10 + Random.nextInt(91)),
        s9 = randomString(10 + Random.nextInt(91)),
        s10 = randomString(10 + Random.nextInt(91))
      )
    }

    protos.LargeStringMessage(
      level1 = Some(makeStrings10()),
      nested1 = Some(
        protos.LargeStringMessageNested1(
          level2 = Some(makeStrings10()),
          nested2 = Some(
            protos.LargeStringMessageNested2(
              level31 = Some(makeStrings10()),
              level32 = Some(makeStrings10()),
              level33 = Some(makeStrings10()),
              level34 = Some(makeStrings10()),
              level35 = Some(makeStrings10()),
              level36 = Some(makeStrings10()),
              level37 = Some(makeStrings10()),
              level38 = Some(makeStrings10())
            )
          )
        )
      )
    )
  }

  def makeLargeNestedStringMessageScala: protos.LargeNestedStringMessage = {
    def randomString(len: Int): String = Random.alphanumeric.take(len).mkString

    def makeStrings10(level: Int): protos.LargeNestedStringMessage = {
      val nested = if (level < 10) Some(makeStrings10(level + 1)) else None
      protos.LargeNestedStringMessage(
        s1 = randomString(10 + Random.nextInt(91)),
        s2 = randomString(10 + Random.nextInt(91)),
        s3 = randomString(10 + Random.nextInt(91)),
        s4 = randomString(10 + Random.nextInt(91)),
        s5 = randomString(10 + Random.nextInt(91)),
        s6 = randomString(10 + Random.nextInt(91)),
        s7 = randomString(10 + Random.nextInt(91)),
        s8 = randomString(10 + Random.nextInt(91)),
        s9 = randomString(10 + Random.nextInt(91)),
        s10 = randomString(10 + Random.nextInt(91)),
        nested = nested
      )
    }
    makeStrings10(1)
  }
}
