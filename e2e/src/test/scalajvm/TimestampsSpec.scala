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
}
