package scalapb

import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.compiler.PluginProtos.{CodeGeneratorRequest, CodeGeneratorResponse}
import com.trueaccord.scalapb.Scalapb
import com.trueaccord.scalapb.compiler.ProtobufGenerator
import protocbridge.{ProtocCodeGenerator, Artifact}


object ScalaPbCodeGenerator extends ProtocCodeGenerator {
  override def run(req: Array[Byte]): Array[Byte] = {
    val registry = ExtensionRegistry.newInstance()
    Scalapb.registerAllExtensions(registry)
    val request = CodeGeneratorRequest.parseFrom(req, registry)
    ProtobufGenerator.handleCodeGeneratorRequest(request).toByteArray
  }

  override def suggestedDependencies: Seq[Artifact] = Seq(
    Artifact("com.trueaccord.scalapb", "scalapb-runtime",
      com.trueaccord.scalapb.compiler.Version.scalapbVersion, crossVersion = true)
  )
}
