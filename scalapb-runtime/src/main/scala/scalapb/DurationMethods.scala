package scalapb

import java.time.{Duration => jtDuration}

trait DurationMethods {
  def seconds: Long
  def nanos: Int

  final def asJavaDuration: jtDuration = jtDuration.ofSeconds(seconds).plusNanos(nanos.toLong)
}
