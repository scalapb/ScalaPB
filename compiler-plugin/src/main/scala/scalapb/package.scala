import protocbridge.JvmGenerator
import scalapb.GeneratorOption._

package object scalapb {

  sealed trait GeneratorOption extends Product with Serializable

  object GeneratorOption {
    case object FlatPackage extends GeneratorOption

    case object JavaConversions extends GeneratorOption

    case object Grpc extends GeneratorOption

    case object SingleLineToProtoString extends GeneratorOption

    case object AsciiFormatToString extends GeneratorOption

    case object Lenses extends GeneratorOption

    case object RetainSourceCodeInfo extends GeneratorOption

    case object OneofsAfterFieldsInConstructor extends GeneratorOption
  }

  def gen(options: Set[GeneratorOption]): (JvmGenerator, Seq[String]) =
    (
      JvmGenerator("scala", ScalaPbCodeGenerator),
      Seq(
        "flat_package"                       -> options(FlatPackage),
        "java_conversions"                   -> options(JavaConversions),
        "grpc"                               -> options(Grpc),
        "single_line_to_proto_string"        -> options(SingleLineToProtoString),
        "ascii_format_to_string"             -> options(AsciiFormatToString),
        "no_lenses"                          -> !options(Lenses),
        "retain_source_code_info"            -> options(RetainSourceCodeInfo),
        "oneofs_after_fields_in_constructor" -> options(OneofsAfterFieldsInConstructor)
      ).collect { case (name, v) if v => name }
    )

  def gen(
      flatPackage: Boolean = false,
      javaConversions: Boolean = false,
      grpc: Boolean = true,
      singleLineToProtoString: Boolean = false,
      asciiFormatToString: Boolean = false,
      lenses: Boolean = true,
      oneofsAfterFieldsInConstructor: Boolean = false
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
    if (oneofsAfterFieldsInConstructor) {
      optionsBuilder += OneofsAfterFieldsInConstructor
    }
    gen(optionsBuilder.result())
  }
}
