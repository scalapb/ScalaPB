package com.thesamet.proto.e2e

import scalapb.descriptors.EnumValueDescriptor

package object cats_types {
  implicit val floatOrdering  = Ordering.Float.TotalOrdering
  implicit val doubleOrdering = Ordering.Double.TotalOrdering

  implicit val enumOrdering: Ordering[com.thesamet.proto.e2e.collection_types.Enum] =
    Ordering.fromLessThan((x, y) => x.value < y.value)

  implicit val enumValueDescriptorOrdering: Ordering[EnumValueDescriptor] =
    Ordering.fromLessThan((x, y) => x.number < y.number)

  implicit val cgbEnumValueDescriptorOrdering
      : Ordering[com.google.protobuf.Descriptors.EnumValueDescriptor] =
    Ordering.fromLessThan((x, y) => x.getNumber < y.getNumber())
}
