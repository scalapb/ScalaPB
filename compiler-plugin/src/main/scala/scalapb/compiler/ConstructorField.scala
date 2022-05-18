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

  def isUnknownFields: Boolean = index == ConstructorField.UnknownFieldsIndex
}

object ConstructorField {
  private val UnknownFieldsIndex: Int = Int.MaxValue

  def unknownFields(annotations: Seq[String]): ConstructorField =
    ConstructorField(
      name = "unknownFields",
      typeName = C.UnknownFieldSet,
      default = Some(C.UnknownFieldSetEmpty),
      index = UnknownFieldsIndex,
      annotations = annotations
    )
}
