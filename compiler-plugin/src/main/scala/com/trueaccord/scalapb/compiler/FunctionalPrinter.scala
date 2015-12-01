package com.trueaccord.scalapb.compiler

import com.trueaccord.scalapb.compiler.FunctionalPrinter.PrinterEndo

trait FPrintable {
  def print(printer: FunctionalPrinter): FunctionalPrinter
}

object PrinterEndo {
  def apply(endo: PrinterEndo): PrinterEndo = endo
}

object FunctionalPrinter {
  type PrinterEndo = FunctionalPrinter => FunctionalPrinter
  val newline: PrinterEndo = _.newline
}

case class FunctionalPrinter(content: List[String] = Nil, indentLevel: Int = 0) {
  val INDENT_SIZE = 2

  def seq(s: Seq[String]): FunctionalPrinter = add(s: _*)

  def add(s: String*): FunctionalPrinter = {
    copy(content = s.map(l => " " * (indentLevel * INDENT_SIZE) + l).reverseIterator.toList ::: content)
  }

  /** add with indent */
  def addI(s: String*): FunctionalPrinter = {
    this.indent.seq(s).outdent
  }

  def newline: FunctionalPrinter = add("")

  def addM(s: String): FunctionalPrinter =
    add(s.stripMargin.split("\n", -1): _*)

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

  def indent = copy(indentLevel = indentLevel + 1)
  def outdent = copy(indentLevel = indentLevel - 1)

  def call(f: (FunctionalPrinter => FunctionalPrinter)*): FunctionalPrinter =
    f.foldLeft(this)((p, f) => f(p))

  def withIndent(f: (FunctionalPrinter => FunctionalPrinter)*): FunctionalPrinter =
    f.foldLeft(this.indent)((p, f) => f(p)).outdent

  def when(cond: => Boolean)(func: FunctionalPrinter => FunctionalPrinter) =
    if (cond) {
      func(this)
    } else {
      this
    }

  def print[M](objects: Traversable[M])(f: (M, FunctionalPrinter) => FunctionalPrinter): FunctionalPrinter =
    objects.foldLeft(this){ (printer, obj) => f(obj, printer) }

  def printAll(fs: Traversable[FPrintable]): FunctionalPrinter =
    print(fs)((p, printer) => p.print(printer))

  def result() =
    content.reverseIterator.mkString("\n")

  override def toString = s"FunctionalPrinter(lines=${content.length}, indentLevel=$indentLevel)"
}
