package scalapb

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import com.trueaccord.scalapb.Scalapb
import com.trueaccord.scalapb.compiler.ProtobufGenerator
import protocbridge.{ProtocCodeGenerator, Artifact}


object ScalaPbCodeGenerator extends ProtocCodeGenerator {
  override def registerExtensions(registry: ExtensionRegistry): Unit = {
    Scalapb.registerAllExtensions(registry)
  }

  override def run(req: CodeGeneratorRequest): CodeGeneratorResponse = {
    ProtobufGenerator.handleCodeGeneratorRequest(req)
  }

  override def suggestedDependencies: Seq[Artifact] = Seq(
    Artifact("com.trueaccord.scalapb", "scalapb-runtime",
      com.trueaccord.scalapb.compiler.Version.scalapbVersion, crossVersion = true)
  )
}
