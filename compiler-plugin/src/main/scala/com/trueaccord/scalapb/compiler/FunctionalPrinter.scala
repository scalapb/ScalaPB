package com.trueaccord.scalapb.compiler

trait FPrintable {
  def print(printer: FunctionalPrinter): FunctionalPrinter
}

case class FunctionalPrinter(content: List[String] = Nil, indentLevel: Int = 0) {
  val INDENT_SIZE = 2
  def add(s: String*): FunctionalPrinter = {
    copy(content = s.map(l => " " * (indentLevel * INDENT_SIZE) + l).reverseIterator.toList ::: content)
  }
  def addWithDelimiter(delimiter:String)(s: Seq[String]) = {
    add(s.zipWithIndex.map {
      case (str, i) => str + (if (s.length - 1 == i) "" else delimiter)
    }: _*)
  }

  def indent = copy(indentLevel = indentLevel + 1)
  def outdent = copy(indentLevel = indentLevel - 1)

  def call(f: FunctionalPrinter => FunctionalPrinter) = f(this)

  def print[M](objects: Traversable[M])(f: (M, FunctionalPrinter) => FunctionalPrinter): FunctionalPrinter =
    objects.foldLeft(this){ (printer, obj) => f(obj, printer) }

  def printAll(fs: Traversable[FPrintable]): FunctionalPrinter =
    print(fs)((p, printer) => p.print(printer))

  override def toString = {
    content.reverseIterator.mkString("\n")
  }
}
