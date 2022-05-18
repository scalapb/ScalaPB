package scalapb

import java.time.Instant

trait TimestampMethods {
  def seconds: Long
  def nanos: Int

  final def asJavaInstant: Instant = Instant.ofEpochSecond(seconds).plusNanos(nanos.toLong)
}
