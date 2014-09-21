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
