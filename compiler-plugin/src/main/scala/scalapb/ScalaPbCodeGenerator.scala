package scalapb

import scalapb.compiler.ProtobufGenerator
import scalapb.options.Scalapb
import com.google.protobuf.ExtensionRegistry
import protocbridge.Artifact
import protocgen.CodeGenApp
import protocgen.CodeGenRequest
import protocgen.CodeGenResponse

object ScalaPbCodeGenerator extends CodeGenApp {
  override def registerExtensions(registry: ExtensionRegistry): Unit =
    Scalapb.registerAllExtensions(registry)

  def process(request: CodeGenRequest): CodeGenResponse =
    ProtobufGenerator.handleCodeGeneratorRequest(request)

  override def suggestedDependencies: Seq[Artifact] = Seq(
    Artifact(
      "com.thesamet.scalapb",
      "scalapb-runtime",
      scalapb.compiler.Version.scalapbVersion,
      crossVersion = true
    )
  )
}
