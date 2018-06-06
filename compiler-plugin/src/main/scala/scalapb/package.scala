import protocbridge.JvmGenerator

package object scalapb {
  def gen(
      flatPackage: Boolean = false,
      javaConversions: Boolean = false,
      grpc: Boolean = true,
      singleLineToProtoString: Boolean = false,
      asciiFormatToString: Boolean = false
  ): (JvmGenerator, Seq[String]) =
    (
      JvmGenerator("scala", ScalaPbCodeGenerator),
      Seq(
        "flat_package"                -> flatPackage,
        "java_conversions"            -> javaConversions,
        "grpc"                        -> grpc,
        "single_line_to_proto_string" -> singleLineToProtoString,
        "ascii_format_to_string"      -> asciiFormatToString
      ).collect { case (name, v) if v => name }
    )
}
