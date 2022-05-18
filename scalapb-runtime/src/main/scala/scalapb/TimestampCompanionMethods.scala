package scalapb

import java.time.Instant

import com.google.protobuf.timestamp.Timestamp

trait TimestampCompanionMethods {
  def apply(instant: Instant): Timestamp = TimestampConverters.fromJavaInstant(instant)

  implicit final val ordering: Ordering[Timestamp] = new Ordering[Timestamp] {
    def compare(x: Timestamp, y: Timestamp): Int = {
      checkValid(x)
      checkValid(y)
      val o1 = java.lang.Long.compare(x.seconds, y.seconds)
      if (o1 != 0) o1
      else java.lang.Integer.compare(x.nanos, y.nanos)
    }
  }

  final def isValid(seconds: Long, nanos: Int): Boolean =
    (seconds >= TIMESTAMP_SECONDS_MIN) && (seconds <= TIMESTAMP_SECONDS_MAX) &&
      (nanos >= 0) && (nanos < NANOS_PER_SECOND)

  final def isValid(timestamp: Timestamp): Boolean =
    isValid(timestamp.seconds, timestamp.nanos)

  final def checkValid(timestamp: Timestamp): Timestamp = {
    require(isValid(timestamp), s"Timestamp ${timestamp} is not valid.")
    timestamp
  }

  final val TIMESTAMP_SECONDS_MIN: Long = -62135596800L

  // Timestamp for "9999-12-31T23:59:59Z"
  final val TIMESTAMP_SECONDS_MAX: Long = 253402300799L

  final val NANOS_PER_SECOND: Long      = 1000000000
  final val NANOS_PER_MILLISECOND: Long = 1000000
  final val NANOS_PER_MICROSECOND: Long = 1000
  final val MILLIS_PER_SECOND: Long     = 1000
  final val MICROS_PER_SECOND: Long     = 1000000
}
