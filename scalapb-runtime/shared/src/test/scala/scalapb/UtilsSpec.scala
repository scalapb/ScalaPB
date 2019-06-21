package scalapb
import scalapb.internal.compat
import utest._

import scala.collection.mutable

object UtilsSpec extends TestSuite {
  val tests = Tests {
    "convertTo" - {
      val x: Vector[Int] = compat.convertTo(Seq(1, 2, 3))
    }

    "convertTo" - {
      val x: mutable.Map[String, Int] = internal.compat.convertTo(Seq(("Foo", 1), ("Bar", 2)))
    }
  }
}
