package scalapb.e2e.scoped

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scalapb.changed.scoped.OrderTest

class OrderSpec extends AnyFlatSpec with Matchers {
  // The goal of this test is to validate the order aux_field_options are applied.
  // Having the code compile with the expected field name verifies that the expected
  // transformation occurred.
  val M = OrderTest(
    file_x1 = 1,
    file_x2_take2 = 2,
    local_x3 = 3
  )
}
