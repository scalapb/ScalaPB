package scalapb.compiler

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File

final case class InsertionPoint(filename: String, insertionPoint: String) {
  def withContent(content: String): File = {
    File
      .newBuilder()
      .setName(filename)
      .setInsertionPoint(insertionPoint)
      .setContent(content)
      .build()
  }
}
