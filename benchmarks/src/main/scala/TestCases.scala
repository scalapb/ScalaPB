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
import scalapb.{GeneratedMessage, LazyField}
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

  def makeLazyFieldsStringMessageScala: protos.LazyFieldsStringMessage = {
    def randomString(len: Int): String = Random.alphanumeric.take(len).mkString

    def makeStrings10(level: Int): protos.LazyFieldsStringMessage = {
      val nested = if (level < 10) Some(makeStrings10(level + 1)) else None
      protos.LazyFieldsStringMessage(
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

  def makeLazyFieldsNoValidationScala: protos.LazyFieldsNoValidation = {
    def randomString(len: Int): String = Random.alphanumeric.take(len).mkString
    def lazyString: LazyField[String]  = LazyField.from(randomString(10 + Random.nextInt(91)))

    def makeStrings10(level: Int): protos.LazyFieldsNoValidation = {
      val nested = if (level < 10) Some(makeStrings10(level + 1)) else None
      protos.LazyFieldsNoValidation(
        s1 = Some(lazyString),
        s2 = Some(lazyString),
        s3 = Some(lazyString),
        s4 = Some(lazyString),
        s5 = Some(lazyString),
        s6 = Some(lazyString),
        s7 = Some(lazyString),
        s8 = Some(lazyString),
        s9 = Some(lazyString),
        s10 = Some(lazyString),
        nested = nested
      )
    }
    makeStrings10(1)
  }

  // Code points, not chars: astral symbols (emoji) are surrogate pairs in
  // UTF-16, and picking a lone half-surrogate from an Array[Char] would corrupt
  // the payload into ASCII '?' bytes during UTF-8 encoding.
  private val nonAsciiCodePoints: Array[Int] =
    ("абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
      "一二三四五六七八九十日本語中文" +
      "한국어" +
      "🙂🙃😉😊🥳🚀🌟").codePoints.toArray

  private def nonAsciiString(len: Int): String = {
    val sb = new java.lang.StringBuilder(len)
    var i  = 0
    while (i < len) {
      sb.appendCodePoint(nonAsciiCodePoints(Random.nextInt(nonAsciiCodePoints.length)))
      i += 1
    }
    sb.toString
  }

  def makeLazyFieldsNonAsciiScala: protos.LazyFieldsNonAscii = {
    def makeStrings10(level: Int): protos.LazyFieldsNonAscii = {
      val nested = if (level < 10) Some(makeStrings10(level + 1)) else None
      protos.LazyFieldsNonAscii(
        s1 = nonAsciiString(10 + Random.nextInt(91)),
        s2 = nonAsciiString(10 + Random.nextInt(91)),
        s3 = nonAsciiString(10 + Random.nextInt(91)),
        s4 = nonAsciiString(10 + Random.nextInt(91)),
        s5 = nonAsciiString(10 + Random.nextInt(91)),
        s6 = nonAsciiString(10 + Random.nextInt(91)),
        s7 = nonAsciiString(10 + Random.nextInt(91)),
        s8 = nonAsciiString(10 + Random.nextInt(91)),
        s9 = nonAsciiString(10 + Random.nextInt(91)),
        s10 = nonAsciiString(10 + Random.nextInt(91)),
        nested = nested
      )
    }
    makeStrings10(1)
  }

  def makeLargeNestedStringMessageNonAsciiScala: protos.LargeNestedStringMessageNonAscii = {
    def makeStrings10(level: Int): protos.LargeNestedStringMessageNonAscii = {
      val nested = if (level < 10) Some(makeStrings10(level + 1)) else None
      protos.LargeNestedStringMessageNonAscii(
        s1 = nonAsciiString(10 + Random.nextInt(91)),
        s2 = nonAsciiString(10 + Random.nextInt(91)),
        s3 = nonAsciiString(10 + Random.nextInt(91)),
        s4 = nonAsciiString(10 + Random.nextInt(91)),
        s5 = nonAsciiString(10 + Random.nextInt(91)),
        s6 = nonAsciiString(10 + Random.nextInt(91)),
        s7 = nonAsciiString(10 + Random.nextInt(91)),
        s8 = nonAsciiString(10 + Random.nextInt(91)),
        s9 = nonAsciiString(10 + Random.nextInt(91)),
        s10 = nonAsciiString(10 + Random.nextInt(91)),
        nested = nested
      )
    }
    makeStrings10(1)
  }

  def makeLargeByteMessageScala: protos.LargeByteMessage = {
    def randomBytes(len: Int): ByteString = {
      val arr = new Array[Byte](len)
      Random.nextBytes(arr)
      ByteString.copyFrom(arr)
    }

    def makeBytes10(): protos.Bytes10 = {
      protos.Bytes10(
        s1 = randomBytes(10 + Random.nextInt(91)),
        s2 = randomBytes(10 + Random.nextInt(91)),
        s3 = randomBytes(10 + Random.nextInt(91)),
        s4 = randomBytes(10 + Random.nextInt(91)),
        s5 = randomBytes(10 + Random.nextInt(91)),
        s6 = randomBytes(10 + Random.nextInt(91)),
        s7 = randomBytes(10 + Random.nextInt(91)),
        s8 = randomBytes(10 + Random.nextInt(91)),
        s9 = randomBytes(10 + Random.nextInt(91)),
        s10 = randomBytes(10 + Random.nextInt(91))
      )
    }

    protos.LargeByteMessage(
      level1 = Some(makeBytes10()),
      nested1 = Some(
        protos.LargeByteMessageNested1(
          level2 = Some(makeBytes10()),
          nested2 = Some(
            protos.LargeByteMessageNested2(
              level31 = Some(makeBytes10()),
              level32 = Some(makeBytes10()),
              level33 = Some(makeBytes10()),
              level34 = Some(makeBytes10()),
              level35 = Some(makeBytes10()),
              level36 = Some(makeBytes10()),
              level37 = Some(makeBytes10()),
              level38 = Some(makeBytes10())
            )
          )
        )
      )
    )
  }
}
