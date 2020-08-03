import scala.io.Source

package object S {
  // finds the number of leading spaces in the given string
  def indentLevel(s: String): Int =
    if (s.isEmpty()) Int.MaxValue
    else if (s.head == ' ') (1 + indentLevel(s.tail))
    else 0

  def example(name: String, marker: String) = {
    val content   =
      Source
        .fromFile("examples/basic/src/main/scala/" + name)
        .getLines()
        .dropWhile(!_.contains(s"start: $marker"))
        .drop(1)
        .takeWhile(!_.contains(s"end: $marker"))
        .toVector
    // indent left the block we read.
    val minIndent = content.map(indentLevel).min
    val trimmed   = content.map(_.drop(minIndent)).mkString("\n")
    println(s"```scala\n${trimmed}\n```")
  }
}
