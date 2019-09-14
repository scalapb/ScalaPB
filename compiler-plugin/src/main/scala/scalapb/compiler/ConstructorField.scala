package scalapb.compiler

case class ConstructorField(
    index: Option[Int],
    name: String,
    typeName: String,
    default: Option[String],
    annotations: Seq[String] = Nil
) {
  def fullString: String =
    Seq(
      s"${if (annotations.isEmpty) "" else annotations.mkString("", " ", " ")}",
      s"${name}: ${typeName}",
      default.fold("")(" = " + _)
    ).mkString

  def nameAndType: String = s"${name}: ${typeName}"
}

object ConstructorField {
  implicit val orderByDefinedIndexFirst = new Ordering[ConstructorField] {
    override def compare(x: ConstructorField, y: ConstructorField): Int = (x.index, y.index) match {
      case (None, None)       => 0
      case (None, _)          => 1
      case (_, None)          => -1
      case (Some(x), Some(y)) => java.lang.Integer.compare(x, y)
    }
  }
}
