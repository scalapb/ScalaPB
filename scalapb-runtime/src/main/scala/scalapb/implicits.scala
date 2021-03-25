package scalapb

import com.google.protobuf.duration.Duration
import com.google.protobuf.timestamp.Timestamp

object implicits {
  implicit class DurationOps(val value: Duration) extends AnyVal {
    def +(that: Duration): Duration = Duration.add(value, that)
    def -(that: Duration): Duration = Duration.subtract(value, that)
    def /(that: Duration): Double   = Duration.divide(value, that)
    def *(that: Double): Duration   = Duration.multiply(value, that)
    def /(that: Double): Duration   = Duration.divide(value, that)
  }

  implicit class TimestampOps(val value: Timestamp) extends AnyVal {
    def -(that: Timestamp): Duration = Timestamp.between(that, value)
    def +(that: Duration): Timestamp = Timestamp.add(value, that)
    def -(that: Duration): Timestamp = Timestamp.subtract(value, that)
  }
}
