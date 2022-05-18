package scalapb.compiler

import coursier._
import coursier.core.Extension
import protocbridge.SystemDetector

object ProtocRunner {
  def forVersion(version: String): protocbridge.ProtocRunner[Int] = {
    val protocDep =
      Dependency(
        Module(Organization("com.google.protobuf"), ModuleName("protoc")),
        version = version
      ).withPublication(
        "protoc",
        Type("jar"),
        Extension("exe"),
        Classifier(SystemDetector.detectedClassifier())
      )

    val protoc = Fetch().addDependencies(protocDep).run().head
    protoc.setExecutable(true)
    protocbridge.ProtocRunner(protoc.getAbsolutePath())
  }
}
