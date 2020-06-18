package scalapb

import protocbridge.JvmGenerator
import scalapb.GeneratorOption._

object gen {
  def apply(options: Set[GeneratorOption]): (JvmGenerator, Seq[String]) =
    (
      JvmGenerator("scala", ScalaPbCodeGenerator),
      options.map(_.toString).toSeq
    )

  def apply(options: GeneratorOption*): (JvmGenerator, Seq[String]) = apply(options.toSet)

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
    if (!lenses) {
      optionsBuilder += NoLenses
    }
    apply(optionsBuilder.result())
  }
}
