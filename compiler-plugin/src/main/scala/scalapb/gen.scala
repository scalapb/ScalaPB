package scalapb

import protocbridge.JvmGenerator
import scalapb.GeneratorOption._

object gen {
  def apply(options: Set[GeneratorOption]): (JvmGenerator, Seq[String]) =
    (
      JvmGenerator("scala", ScalaPbCodeGenerator),
      Seq(
        "flat_package"                -> options(FlatPackage),
        "java_conversions"            -> options(JavaConversions),
        "grpc"                        -> options(Grpc),
        "single_line_to_proto_string" -> options(SingleLineToProtoString),
        "ascii_format_to_string"      -> options(AsciiFormatToString),
        "no_lenses"                   -> !options(Lenses),
        "retain_source_code_info"     -> options(RetainSourceCodeInfo)
      ).collect { case (name, v) if v => name }
    )

  def apply(
      flatPackage: Boolean = false,
      javaConversions: Boolean = false,
      grpc: Boolean = true,
      singleLineToProtoString: Boolean = false,
      asciiFormatToString: Boolean = false,
      lenses: Boolean = true
  ): (JvmGenerator, Seq[String]) = {
    val optionsBuilder = Set.newBuilder[GeneratorOption]
    if (flatPackage) {
      optionsBuilder += FlatPackage
    }
    if (javaConversions) {
      optionsBuilder += JavaConversions
    }
    if (grpc) {
      optionsBuilder += Grpc
    }
    if (singleLineToProtoString) {
      optionsBuilder += SingleLineToProtoString
    }
    if (asciiFormatToString) {
      optionsBuilder += AsciiFormatToString
    }
    if (lenses) {
      optionsBuilder += Lenses
    }
    gen(optionsBuilder.result())
  }
}