package com.trueaccord.scalapb.compiler

import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo

object PrinterEndo {
  def apply(endo: PrinterEndo): PrinterEndo = endo
}

object FunctionalPrinter {
  type PrinterEndo = FunctionalPrinter => FunctionalPrinter
}

case class FunctionalPrinter(content: Vector[String] = Vector.empty, indentLevel: Int = 0) {
  val INDENT_SIZE = 2

  // Increase indent level
  def indent = copy(indentLevel = indentLevel + 1)

  // Decreases indent level
  def outdent = {
    assert(indentLevel > 0)
    copy(indentLevel = indentLevel - 1)
  }

  /** Adds strings at the current indent level. */
  def add(s: String*): FunctionalPrinter = {
    copy(content = content ++ s.map(l => " " * (indentLevel * INDENT_SIZE) + l))
  }

  def seq(s: Seq[String]): FunctionalPrinter = add(s: _*)

  /** add with indent */
  def addIndented(s: String*): FunctionalPrinter = {
    this.indent.seq(s).outdent
  }

  def newline: FunctionalPrinter = add("")

  // Strips the margin, splits lines and adds.
  def addStringMargin(s: String): FunctionalPrinter =
    add(s.stripMargin.split("\n", -1): _*)

  // Adds the strings, while putting a delimiter between two lines.
  def addWithDelimiter(delimiter:String)(s: Seq[String]) = {
    add(s.zipWithIndex.map {
      case (line, index) => if (index == s.length - 1) line else (line + delimiter)
    }: _*)
  }

  def addGroupsWithDelimiter(delimiter:String)(groups: Seq[Seq[String]]) = {
    val lines = for {
      (group, index) <- groups.zipWithIndex
      (line, lineInGroup) <- group.zipWithIndex
    } yield if (index < groups.length - 1 && lineInGroup == group.length - 1)
        (line + delimiter) else line
    add(lines: _*)
  }

  def call(f: PrinterEndo*): FunctionalPrinter =
    f.foldLeft(this)((p, f) => f(p))

  def when(cond: => Boolean)(func: FunctionalPrinter => FunctionalPrinter) =
    if (cond) {
      func(this)
    } else {
      this
    }

  def print[M](objects: Traversable[M])(f: (FunctionalPrinter, M) => FunctionalPrinter): FunctionalPrinter = {
    objects.foldLeft(this)(f)
  }

  def result() =
    content.mkString("\n")

  override def toString = s"FunctionalPrinter(lines=${content.length}, indentLevel=$indentLevel)"
}
