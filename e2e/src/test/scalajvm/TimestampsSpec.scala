import java.time.{Instant, Duration => jtDuration}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import com.google.protobuf.timestamp.Timestamp
import com.google.protobuf.duration.Duration

class TimestampsSpec extends AnyFlatSpec with Matchers {
  "Timestamps and Duration" should "have implicit ordering" in {
    implicitly[Ordering[Timestamp]]
    implicitly[Ordering[Duration]]
  }

  "Timestamps and Duration" should "be convertible for their java.time counterparts via implicit conversions" in {
    import scalapb.TimestampConverters._
    import scalapb.DurationConverters._

    val t1: Instant = Instant.parse("2018-11-30T18:35:24.123Z")
    val t2: Instant = Instant.parse("2018-11-30T18:35:25.456Z")

    val timestamp: Timestamp = t1
    timestamp must be(Timestamp(1543602924, 123000000))
    (timestamp: Instant) must be(t1)

    val duration: Duration = jtDuration.between(t1, t2)
    duration must be(Duration(1, 333000000))
    (duration: jtDuration) must be(jtDuration.between(t1, t2))
  }

  "Timestamps and Duration" can "be convertible for their java.time counterparts via helpers" in {
    val t1: Instant = Instant.parse("2018-11-30T18:35:24.123Z")
    val t2: Instant = Instant.parse("2018-11-30T18:35:25.456Z")

    val timestamp: Timestamp = Timestamp(t1)
    timestamp must be(Timestamp(1543602924, 123000000))
    timestamp.asJavaInstant must be(t1)

    val duration: Duration = Duration(jtDuration.between(t1, t2))
    duration must be(Duration(1, 333000000))
    duration.asJavaDuration must be(jtDuration.between(t1, t2))
  }

  "Timestamps" should "be subtracted by another timestamp" in {
    import scalapb.TimestampConverters._
    import scalapb.implicits.TimestampOps

    val t1: Timestamp = Instant.parse("0001-01-01T00:00:00.000000000Z")
    val t2: Timestamp = Instant.parse("9999-12-31T23:59:59.999999999Z")
    t2 - t1 must be(Duration(315537897599L, 999999999))
    t1 - t2 must be(Duration(-315537897599L, -999999999))
  }

  "Timestamps" should "be added or subtracted by a duration" in {
    import scalapb.implicits.TimestampOps

    Timestamp(123, 456789000) + Duration(987, 654321000) must be(Timestamp(1111, 111110000))
    intercept[IllegalArgumentException](Timestamp(200000000000L, 1) + Duration(200000000000L, 2))

    Timestamp(987, 654321000) - Duration(1111, 111110000) must be(Timestamp(-124, 543211000))
    intercept[IllegalArgumentException](Timestamp(-200000000000L, 1) - Duration(200000000000L, 2))
  }

  "Durations" should "be valid when their seconds and nanos are in defined range and have same sign" in {
    Duration.isValid(Duration(315576000001L, 0)) must be(false)
    Duration.isValid(Duration(315576000000L, 999999999)) must be(true)
    Duration.isValid(Duration(315576000000L, 0)) must be(true)
    Duration.isValid(Duration(315576000000L, -999999999)) must be(false)
    Duration.isValid(Duration(0, 1000000000)) must be(false)
    Duration.isValid(Duration(0, 999999999)) must be(true)
    Duration.isValid(Duration(0, 0)) must be(true)
    Duration.isValid(Duration(0, -999999999)) must be(true)
    Duration.isValid(Duration(0, -1000000000)) must be(false)
    Duration.isValid(Duration(-315576000000L, 999999999)) must be(false)
    Duration.isValid(Duration(-315576000000L, 0)) must be(true)
    Duration.isValid(Duration(-315576000000L, -999999999)) must be(true)
    Duration.isValid(Duration(-315576000001L, 0)) must be(false)
  }

  "Durations" should "be added, subtracted, or divided by another duration" in {
    import scalapb.implicits.DurationOps

    Duration(13, 579000000) + Duration(24, 680000000) must be(Duration(38, 259000000))
    intercept[IllegalArgumentException](Duration(315576000000L, 999999999) + Duration(0, 1))

    Duration(-38, -259000000) - Duration(-13, -579000000) must be(Duration(-24, -680000000))
    intercept[IllegalArgumentException](Duration(315575999999L, 999999999) - Duration(-1, -1))

    Duration(1, 0) / Duration(2, 500000000) must be(0.4 +- 1e-6)
    intercept[IllegalArgumentException](Duration(1, 2) / Duration(0, 0))
  }

  "Durations" should "be multiplied or divided by a double precision floating point number" in {
    import scalapb.implicits.DurationOps

    Duration(111, 111000001) * 2.3 must be(Duration(255, 555300002))
    intercept[IllegalArgumentException](Duration(1000000, 10000) * -315576.0)

    Duration(-255, -555300002) / -2.3 must be(Duration(111, 111000001))
    intercept[IllegalArgumentException](Duration(-100000000000L, 0) / 0.3)
    intercept[IllegalArgumentException](Duration(1, 2) / 0.0)
  }
}
