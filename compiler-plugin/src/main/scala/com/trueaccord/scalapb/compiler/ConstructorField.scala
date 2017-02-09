package com.trueaccord.scalapb.compiler

final case class ConstructorField(name: String, fieldType: String, default: Option[String]) {
  def asString: String = s"""${name}: ${fieldType}${default.fold("")(" = " + _)}"""
}
