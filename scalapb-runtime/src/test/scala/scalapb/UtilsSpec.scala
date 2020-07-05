package scalapb
import scalapb.internal.compat
import munit.FunSuite

import scala.collection.mutable

class UtilsSpec extends FunSuite {
  test("convertTo") {
    val x: Vector[Int] = compat.convertTo(Seq(1, 2, 3))
    x
  }

  test("mutable convertTo") {
    val x: mutable.Map[String, Int] = internal.compat.convertTo(Seq(("Foo", 1), ("Bar", 2)))
    x
  }
}
