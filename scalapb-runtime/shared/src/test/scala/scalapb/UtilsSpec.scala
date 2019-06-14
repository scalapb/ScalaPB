package scalapb
import utest._

import scala.collection.mutable

object UtilsSpec extends TestSuite {
  val tests = Tests {
    'convertTo {
      val x: Vector[Int] = internal.convertTo(Seq(1, 2, 3))
    }

    'convertTo {
      val x: mutable.Map[String, Int] = internal.convertTo(Seq(("Foo", 1), ("Bar", 2)))
    }
  }
}
