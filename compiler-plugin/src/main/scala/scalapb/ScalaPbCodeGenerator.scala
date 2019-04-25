package scalapb

import com.google.protobuf.{CodedInputStream, ExtensionRegistry}
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import scalapb.compiler.ProtobufGenerator
import scalapb.options.compiler.Scalapb
import protocbridge.{Artifact, ProtocCodeGenerator}

object ScalaPbCodeGenerator extends ProtocCodeGenerator {
  override def run(req: Array[Byte]): Array[Byte] = run(CodedInputStream.newInstance(req))

  def run(input: CodedInputStream): Array[Byte] = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)
    try {
      val request = CodeGeneratorRequest.parseFrom(input, registry)
      ProtobufGenerator.handleCodeGeneratorRequest(request).toByteArray
    } catch {
      case t: Throwable =>
      CodeGeneratorResponse.newBuilder().setError(t.toString).build().toByteArray
    }
  }

  override def suggestedDependencies: Seq[Artifact] = Seq(
    Artifact(
      "com.thesamet.scalapb",
      "scalapb-runtime",
      scalapb.compiler.Version.scalapbVersion,
      crossVersion = true
    )
  )
}
