package scalapb.compiler

case class ConstructorField(
    name: String,
    typeName: String,
    default: Option[String],
    index: Int,
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
  val UnknownFields = ConstructorField(
    name = "unknownFields",
    typeName = C.UnknownFieldSet,
    default = Some(C.UnknownFieldSetEmpty),
    index = Int.MaxValue
  )
  
  def unknownFields(annotations: Seq[String]): ConstructorField = UnknownFields.copy(annotations = annotations)
}
