import com.trueaccord.scalapb.compiler.ProtobufGenerator
import protocbridge.JvmGenerator

package object scalapb {
  def gen(
    flatPackage: Boolean = false,
    javaConversions: Boolean = false,
    grpc: Boolean = true,
    singleLineToString: Boolean = false,
    collectionType: String = "scala.collection.Seq"): (JvmGenerator, Seq[String]) =
    (JvmGenerator(
      "scala",
      ScalaPbCodeGenerator),
      Seq(
        "flat_package" -> flatPackage,
        "java_conversions" -> javaConversions,
        "grpc" -> grpc,
        "single_line_to_string" -> singleLineToString
      ).collect { case (name, v) if v => name } :+ (ProtobufGenerator.collectionTypeKey + "=" + collectionType)
    )
}
