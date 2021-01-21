package com.thesamet.pb

import scalapb.TypeMapper

case class Email(user: String, domain: String) {
  override def toString = s"$user@$domain"
}

object Email {
  def fromString(s: String) = s.split("@", 2).toSeq match {
    case Seq(user, domain) => Email(user, domain)
    case _                   => throw new IllegalArgumentException(s"Expected @ in email. Got: $s")
  }

  implicit val emailTypeMapper: TypeMapper[String, Email] = TypeMapper[String, Email](fromString)(_.toString)
}