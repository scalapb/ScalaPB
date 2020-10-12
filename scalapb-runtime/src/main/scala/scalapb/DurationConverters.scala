package scalapb

import java.time.{Duration => jtDuration}

import scala.language.implicitConversions

import com.google.protobuf.duration.Duration

trait DurationConverters {
  implicit def fromJavaDuration(duration: jtDuration): Duration =
    new Duration(duration.getSeconds, duration.getNano)

  implicit def asJavaDuration(duration: Duration): jtDuration =
    jtDuration.ofSeconds(duration.seconds).plusNanos(duration.nanos.toLong)
}

object DurationConverters extends DurationConverters
