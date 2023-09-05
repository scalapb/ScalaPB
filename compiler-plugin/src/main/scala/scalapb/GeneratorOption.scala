package scalapb

sealed trait GeneratorOption extends Product with Serializable

object GeneratorOption {
  case object FlatPackage extends GeneratorOption {
    override def toString = "flat_package"
  }

  case object JavaConversions extends GeneratorOption {
    override def toString = "java_conversions"
  }

  case object Grpc extends GeneratorOption {
    override def toString = "grpc"
  }

  case object SingleLineToProtoString extends GeneratorOption {
    override def toString = "single_line_to_proto_string"
  }

  case object AsciiFormatToString extends GeneratorOption {
    override def toString = "ascii_format_to_string"
  }

  case object NoLenses extends GeneratorOption {
    override def toString = "no_lenses"
  }

  case object RetainSourceCodeInfo extends GeneratorOption {
    override def toString = "retain_source_code_info"
  }

  case object Scala3Sources extends GeneratorOption {
    override def toString = "scala3_sources"
  }
}
