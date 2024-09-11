package com.thesamet.pb

import scalapb.TypeMapper
import com.thesamet.proto.e2e.custom_types.CustomMessage.Name
import com.thesamet.proto.e2e.no_box.NameNoBox

case class FullName(firstName: String, lastName: String)

object FullName {
  implicit val mapper: TypeMapper[Name, FullName] =
    TypeMapper[Name, FullName](n => FullName(n.getFirst, n.getLast))(fn =>
      Name(
        first = if (fn.firstName != "EMPTY") Some(fn.firstName) else None,
        last = if (fn.lastName != "EMPTY") Some(fn.lastName) else None
      )
    )

  implicit val mapperNoBox: TypeMapper[NameNoBox, FullName] =
    TypeMapper[NameNoBox, FullName](n => FullName(n.first, n.last))(fn =>
      NameNoBox(first = fn.firstName, last = fn.lastName)
    )
}

