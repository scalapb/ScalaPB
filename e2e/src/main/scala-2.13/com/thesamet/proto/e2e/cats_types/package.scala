package com.thesamet.proto.e2e

package object cats_types {
  implicit val floatOrdering  = Ordering.Float.TotalOrdering
  implicit val doubleOrdering = Ordering.Double.TotalOrdering

  implicit val enumOrdering: Ordering[com.thesamet.proto.e2e.collection_types.Enum] =
    Ordering.fromLessThan((x, y) => x.value < y.value)
}
