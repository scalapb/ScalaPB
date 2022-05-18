package scalapb.options.compiler

import com.google.protobuf.ExtensionRegistry

object Scalapb {
  @deprecated("Use scalapb.options.Scalapb.registerAllExtensions", "0.10.9")
  def registerAllExtensions(registry: ExtensionRegistry) =
    scalapb.options.Scalapb.registerAllExtensions(registry)
}
