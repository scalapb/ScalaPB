package scalapb.compiler

case class GeneratorParams(
    javaConversions: Boolean = false,
    flatPackage: Boolean = false,
    grpc: Boolean = false,
    singleLineToProtoString: Boolean = false,
    asciiFormatToString: Boolean = false,
    lenses: Boolean = true,
    retainSourceCodeInfo: Boolean = false,
    scala3Sources: Boolean = false
)

object GeneratorParams {
  def fromString(params: String): Either[String, GeneratorParams] =
    fromStringCollectUnrecognized(params) match {
      case Left(l)              => Left(l)
      case Right((params, Nil)) => Right(params)
      case Right((_, unrec))    => Left(s"Unrecognized parameters: ${unrec.mkString(", ")}")
    }

  /** Parses generator parameters from the comma-separated string, collects the unrecognized ones.
    *
    * This allows subsequent generators to try and process those parameters.
    *
    * @return
    *   Returns Left(error) if a parsing error occurred. On success, it returns `Right((params,
    *   remainder))` where `params` are recognized parameters and `remainder` are all unrecognized
    *   params.
    */
  def fromStringCollectUnrecognized(
      params: String
  ): Either[String, (GeneratorParams, Seq[String])] = {
    params
      .split(",")
      .map(_.trim)
      .filter(_.nonEmpty)
      .foldLeft[Either[String, (GeneratorParams, Seq[String])]](
        Right((GeneratorParams(), Seq.empty))
      ) {
        case (Right((params, unrecognized)), str) =>
          str match {
            case "java_conversions" => Right((params.copy(javaConversions = true), unrecognized))
            case "flat_package"     => Right((params.copy(flatPackage = true), unrecognized))
            case "grpc"             => Right((params.copy(grpc = true), unrecognized))
            case "single_line_to_proto_string" =>
              Right((params.copy(singleLineToProtoString = true), unrecognized))
            case "ascii_format_to_string" =>
              Right((params.copy(asciiFormatToString = true), unrecognized))
            case "no_lenses"               => Right((params.copy(lenses = false), unrecognized))
            case "retain_source_code_info" =>
              Right((params.copy(retainSourceCodeInfo = true), unrecognized))
            case "scala3_sources" =>
              Right((params.copy(scala3Sources = true), unrecognized))
            case p => Right((params, unrecognized :+ p))
          }
        case (l, _) => l
      }
  }
}
