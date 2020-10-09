package scalapb

import java.time.{Duration => jtDuration}

import com.google.protobuf.duration.Duration
import com.google.protobuf.timestamp.Timestamp.NANOS_PER_SECOND

trait DurationCompanionMethods {
  def apply(jtDuration: jtDuration): Duration = DurationConverters.fromJavaDuration(jtDuration)

  implicit final val ordering: Ordering[Duration] = new Ordering[Duration] {
    def compare(x: Duration, y: Duration): Int = {
      checkValid(x)
      checkValid(y)
      val o1 = java.lang.Long.compare(x.seconds, y.seconds)
      if (o1 != 0) o1
      else java.lang.Integer.compare(x.nanos, y.nanos)
    }
  }

  final def isValid(seconds: Long, nanos: Int): Boolean =
    (seconds >= DURATION_SECONDS_MIN) && (seconds <= DURATION_SECONDS_MAX) &&
      (nanos >= 0) && (nanos < NANOS_PER_SECOND)

  final def isValid(duration: Duration): Boolean =
    isValid(duration.seconds, duration.nanos)

  final def checkValid(duration: Duration): Duration = {
    require(isValid(duration), s"Duration ${duration} is not valid.")
    duration
  }

  final val DURATION_SECONDS_MIN: Long = -315576000000L
  final val DURATION_SECONDS_MAX: Long = 315576000000L
}
