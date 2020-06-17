package scalapb

sealed trait GeneratorOption extends Product with Serializable

object GeneratorOption {
  case object FlatPackage extends GeneratorOption

  case object JavaConversions extends GeneratorOption

  case object Grpc extends GeneratorOption

  case object SingleLineToProtoString extends GeneratorOption

  case object AsciiFormatToString extends GeneratorOption

  case object Lenses extends GeneratorOption

  case object RetainSourceCodeInfo extends GeneratorOption
}
