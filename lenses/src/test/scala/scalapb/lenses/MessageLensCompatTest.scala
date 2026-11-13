package scalapb.lenses

import munit.FunSuite
import scala.annotation.nowarn

class MessageLensCompatTest extends FunSuite {
  case class Value(number: Int)

  @nowarn("cat=deprecation")
  class LegacyLens[U](lens: Lens[U, Value]) extends MessageLens[U, Value](lens) {
    def number: Lens[U, Int] = field(_.number)((value, number) => value.copy(number = number))
  }

  test("legacy generated lenses retain ObjectLens behavior") {
    val lens: ObjectLens[Value, Value] = new LegacyLens(Lens.unit[Value])
    val value                          = Value(1)
    assertEquals(lens.get(value), value)
    assertEquals(lens.set(Value(2))(value), Value(2))
    assertEquals(new LegacyLens(Lens.unit[Value]).number.set(3)(value), Value(3))
    assertEquals(lens.update(_.set(Value(4)))(value), Value(4))
  }
}
