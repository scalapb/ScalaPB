package com.thesamet.proto.e2e

package object cats_types {
  implicit val enumOrdering: Ordering[com.thesamet.proto.e2e.collection_types.Enum] =
    Ordering.fromLessThan((x, y) => x.value < y.value)
}
