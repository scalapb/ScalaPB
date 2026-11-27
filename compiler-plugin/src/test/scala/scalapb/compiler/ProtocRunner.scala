package scalapb.compiler

import coursier._
import coursier.core.Extension
import protocbridge.SystemDetector
import scala.annotation.nowarn

object ProtocRunner {
  def forVersion(version: String): protocbridge.ProtocRunner[Int] = {
    // Scala 2.12 still uses Coursier 2.1.24, which requires the String version API.
    @nowarn("msg=Use the override accepting a VersionConstraint")
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
