/**
 * Created by nadavsr on 9/20/14.
 */

trait Printable {
  def print(printer: CodePrinter): Unit
}

class CodePrinter {
  val INDENT_SIZE = 2
  var lines = collection.mutable.ArrayBuffer[String]()
  var indentLevel: Int = 0;
  def add(s: String*): CodePrinter = {
    lines ++= s.map(l => " " * (indentLevel * INDENT_SIZE) + l)
    this
  }
  def print(printables: Printable*): CodePrinter = {
    printables.foreach(_.print(this))
    this
  }
  def indent: CodePrinter = {
    indentLevel += 1
    this
  }
  def outdent: CodePrinter = {
    indentLevel -= 1
    this
  }

  override def toString = lines.mkString("\n")
}

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
