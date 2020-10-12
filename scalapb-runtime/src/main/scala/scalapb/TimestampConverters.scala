package scalapb

import java.time.Instant

import scala.language.implicitConversions

import com.google.protobuf.timestamp.Timestamp

trait TimestampConverters {
  implicit def fromJavaInstant(instant: Instant): Timestamp =
    Timestamp(seconds = instant.getEpochSecond, nanos = instant.getNano)

  implicit def asJavaInstant(timestamp: Timestamp): Instant =
    Instant.ofEpochSecond(timestamp.seconds).plusNanos(timestamp.nanos.toLong)
}

object TimestampConverters extends TimestampConverters
