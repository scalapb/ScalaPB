package scalapb
import scalapb.internal.compat
import utest._

import scala.collection.mutable
import scala.annotation.nowarn

object UtilsSpec extends TestSuite {
  @nowarn
  val tests = Tests {
    "convertTo" - {
      val x: Vector[Int] = compat.convertTo(Seq(1, 2, 3))
    }

    "convertTo" - {
      val x: mutable.Map[String, Int] = internal.compat.convertTo(Seq(("Foo", 1), ("Bar", 2)))
    }
  }
}
