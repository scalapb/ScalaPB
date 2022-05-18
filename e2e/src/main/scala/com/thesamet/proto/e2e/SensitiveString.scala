package com.thesamet.proto.e2e

import scalapb.TypeMapper

final case class SensitiveString(s: String)

final case class SensitiveString7(s: String)

object SensitiveString {
  implicit val tm: TypeMapper[String, SensitiveString] =
    TypeMapper[String, SensitiveString](SensitiveString(_))(_.s)
}

object SensitiveString7 {
  implicit val tm7: TypeMapper[String, SensitiveString7] =
    TypeMapper[String, SensitiveString7](SensitiveString7(_))(_.s)
}
