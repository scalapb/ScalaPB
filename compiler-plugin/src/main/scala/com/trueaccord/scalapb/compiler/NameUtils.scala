package com.trueaccord.scalapb.compiler

object NameUtils {
  def snakeCaseToCamelCase(name: String, upperInitial: Boolean = false): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, index: Int, capNext: Boolean): Unit = if (name.nonEmpty) {
      val (r, capNext2) = name.head match {
        case c if c.isLower => (Some(if (capNext) c.toUpper else c), false)
        case c if c.isUpper =>
          // force first letter to lower unless forced to capitalize it.
          (Some(if (index == 0 && !capNext) c.toLower else c), false)
        case c if c.isDigit => (Some(c), true)
        case _ => (None, true)
      }
      r.foreach(b.append)
      inner(name.tail, index + 1, capNext2)
    }
    inner(name, 0, upperInitial)
    b.toString
  }

  def toAllCaps(name: String): String = {
    val b = new StringBuilder()
    @annotation.tailrec
    def inner(name: String, lastLower: Boolean): Unit = if (name.nonEmpty) {
      val nextLastLower = name.head match {
        case c if c.isLower => b.append(c.toUpper)
          true
        case c if c.isUpper =>
          if (lastLower) { b.append('_') }
          b.append(c)
          false
        case c =>
          b.append(c)
          false
      }
      inner(name.tail, nextLastLower)
    }
    inner(name, false)
    b.toString
  }


}
