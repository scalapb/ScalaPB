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
      (nanos > -NANOS_PER_SECOND) && (nanos < NANOS_PER_SECOND) &&
      (math.signum(seconds) * math.signum(nanos) != -1)

  final def isValid(duration: Duration): Boolean =
    isValid(duration.seconds, duration.nanos)

  final def checkValid(duration: Duration): Duration = {
    require(isValid(duration), s"Duration ${duration} is not valid.")
    duration
  }

  final def normalizedDuration(seconds: Long, nanos: Int): Duration = {
    var s = seconds
    var n = nanos
    if (n <= -NANOS_PER_SECOND || n >= NANOS_PER_SECOND) {
      s = Math.addExact(s, n / NANOS_PER_SECOND)
      n = (n % NANOS_PER_SECOND).toInt
    }
    if (s > 0 && n < 0) {
      s = s - 1                        // no overflow since positive seconds is decremented
      n = (n + NANOS_PER_SECOND).toInt // no overflow since negative nanos is added
    } else if (s < 0 && n > 0) {
      s = s + 1                        // no overflow since positive seconds is incremented
      n = (n - NANOS_PER_SECOND).toInt // no overflow since positive nanos is subtracted
    }
    checkValid(Duration(s, n))
  }

  final def fromSecondsDouble(seconds: Double): Duration =
    normalizedDuration(seconds.toLong, math.round((seconds % 1) * NANOS_PER_SECOND).toInt)

  final def toSecondsDouble(duration: Duration): Double =
    duration.seconds + duration.nanos.toDouble / NANOS_PER_SECOND

  final def add(d1: Duration, d2: Duration): Duration = {
    checkValid(d1)
    checkValid(d2)
    normalizedDuration(
      Math.addExact(d1.seconds, d2.seconds),
      Math.addExact(d1.nanos, d2.nanos)
    )
  }

  final def subtract(d1: Duration, d2: Duration): Duration = {
    checkValid(d1)
    checkValid(d2)
    normalizedDuration(
      Math.subtractExact(d1.seconds, d2.seconds),
      Math.subtractExact(d1.nanos, d2.nanos)
    )
  }

  final def divide(d1: Duration, d2: Duration): Double = {
    checkValid(d1)
    checkValid(d2)
    require(d2.seconds != 0 || d2.nanos != 0, "/ by zero duration")
    toSecondsDouble(d1) / toSecondsDouble(d2)
  }

  final def multiply(duration: Duration, multiplier: Double): Duration = {
    checkValid(duration)
    fromSecondsDouble(toSecondsDouble(duration) * multiplier)
  }

  final def divide(duration: Duration, divider: Double): Duration = {
    checkValid(duration)
    require(divider != 0.0, "/ by zero")
    fromSecondsDouble(toSecondsDouble(duration) / divider)
  }

  final val DURATION_SECONDS_MIN: Long = -315576000000L
  final val DURATION_SECONDS_MAX: Long = 315576000000L
}
