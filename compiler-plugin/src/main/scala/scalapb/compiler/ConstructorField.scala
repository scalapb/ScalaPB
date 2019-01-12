package scalapb.compiler

case class ConstructorField(
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
