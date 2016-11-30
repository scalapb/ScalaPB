import protocbridge.JvmGenerator

package object scalapb {
  def gen(
    flatPackage: Boolean = false,
    javaConversions: Boolean = false,
    grpc: Boolean = true,
    grpcServiceIFace: Boolean = true,
    singleLineToString: Boolean = false): (JvmGenerator, Seq[String]) =
    (JvmGenerator(
      "scala",
      ScalaPbCodeGenerator),
      Seq(
        "flat_package" -> flatPackage,
        "java_conversions" -> javaConversions,
        "grpc" -> grpc,
        "grpc_service_interface" -> grpcServiceIFace,
        "single_line_to_string" -> singleLineToString
      ).collect { case (name, v) if v => name })
}
