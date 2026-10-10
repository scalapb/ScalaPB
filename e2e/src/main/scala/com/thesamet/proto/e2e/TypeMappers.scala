package com.thesamet.proto.e2e

import scalapb.{GeneratedEnumCompanion, TypeMapper}

object TypeMappers {
  implicit def enumMapper[A <: scalapb.GeneratedEnum](implicit
      ec: GeneratedEnumCompanion[A]
  ): TypeMapper[A, String] = TypeMapper[A, String](_.name)(ec.fromName(_).get)
}
