package com.trueaccord.scalapb.compiler

trait FPrintable {
  def print(printer: FunctionalPrinter): FunctionalPrinter
}

case class FunctionalPrinter(content: Vector[String] = Vector.empty, indentLevel: Int = 0) {
  val INDENT_SIZE = 2
  def add(s: String*): FunctionalPrinter = {
    copy(content = content ++ s.map(l => " " * (indentLevel * INDENT_SIZE) + l))
  }
  def indent = copy(indentLevel = indentLevel + 1)
  def outdent = copy(indentLevel = indentLevel - 1)

  def print[M](objects: Traversable[M])(f: (M, FunctionalPrinter) => FunctionalPrinter): FunctionalPrinter =
    objects.foldLeft(this){ (printer, obj) => f(obj, printer) }

  def printAll(fs: Traversable[FPrintable]): FunctionalPrinter =
    print(fs)((p, printer) => p.print(printer))

  override def toString = {
    content.mkString("\n")
  }
}
