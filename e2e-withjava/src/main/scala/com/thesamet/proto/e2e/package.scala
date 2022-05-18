package com.thesamet.proto.e2e

import scalapb.TypeMapper

package object cats_types {
  implicit val enumOrdering: Ordering[com.thesamet.proto.e2e.collection_types.Enum] =
    Ordering.fromLessThan((x, y) => x.value < y.value)
}

package object no_box {
  implicit val stringToBigDecimalMapper: TypeMapper[String, BigDecimal] =
    TypeMapper[String, BigDecimal](BigDecimal.apply)(_.bigDecimal.toPlainString)
}
