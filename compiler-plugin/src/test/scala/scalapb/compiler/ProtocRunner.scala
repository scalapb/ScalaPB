package scalapb.compiler

import scala.io.Source
import sys.process._
import coursier._
import coursier.core.Extension
import protocbridge.SystemDetector

object ProtocRunner {
  // simple replacement for protoc-jar
  def runProtoc(version: String, args: Seq[String]): Int = {
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

    val maybeNixDynamicLinker: Option[String] =
      sys.env.get("NIX_CC").map { nixCC =>
        Source.fromFile(nixCC + "/nix-support/dynamic-linker").mkString.trim()
      }

    ((maybeNixDynamicLinker.toSeq :+ protoc.getAbsolutePath.toString) ++ args).!
  }
}
