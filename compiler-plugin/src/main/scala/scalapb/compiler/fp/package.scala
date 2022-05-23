package scalapb.compiler

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse
import protocgen.CodeGenResponse
import FunctionalPrinter._

package object fp {
  def file(path: String)(content: PrinterEndo): CodeGenResponse = {
    try {
      val c = content(FunctionalPrinter())
        .content
        .mkString(System.lineSeparator())
      CodeGenResponse.succeed(Seq(CodeGeneratorResponse.File.newBuilder().setName(path).setContent(c).build()), Set(CodeGeneratorResponse.Feature.FEATURE_PROTO3_OPTIONAL))
    } catch {
      case e: GeneratorException => CodeGenResponse.fail(e.message)
    }
  }
}
