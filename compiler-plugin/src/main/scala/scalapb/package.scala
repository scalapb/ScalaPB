import protocbridge.JvmGenerator

package object scalapb {
  def gen(
    flatPackage: Boolean = false,
    javaConversions: Boolean = false,
    grpc: Boolean = false): (JvmGenerator, Seq[String]) =
    (JvmGenerator(
      "scala",
      ScalaPbCodeGenerator),
      Seq(
        "java_conversions" -> javaConversions,
        "flat_package" -> flatPackage,
        "grpc" -> grpc).collect { case (name, v) if v => name })
}
