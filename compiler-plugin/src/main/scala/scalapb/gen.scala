package scalapb

import protocbridge.{Artifact, SandboxedJvmGenerator}
import scalapb.GeneratorOption._

object gen {
  val SandboxedGenerator = SandboxedJvmGenerator.forModule(
    "scala",
    Artifact(
      "com.thesamet.scalapb",
      "compilerplugin_2.12",
      scalapb.compiler.Version.scalapbVersion
    ),
    "scalapb.ScalaPbCodeGenerator$",
    scalapb.ScalaPbCodeGenerator.suggestedDependencies
  )

  def apply(options: Set[GeneratorOption]): (SandboxedJvmGenerator, Seq[String]) =
    (
      SandboxedGenerator,
      options.map(_.toString).toSeq
    )

  def apply(options: GeneratorOption*): (SandboxedJvmGenerator, Seq[String]) =
    apply(options.toSet)

  def apply(): (SandboxedJvmGenerator, Seq[String]) =
    apply(grpc = true)

  def apply(
      flatPackage: Boolean = false,
      javaConversions: Boolean = false,
      grpc: Boolean = true,
      singleLineToProtoString: Boolean = false,
      asciiFormatToString: Boolean = false,
      lenses: Boolean = true,
      scala3Sources: Boolean = false
  ): (SandboxedJvmGenerator, Seq[String]) = {
    val optionsBuilder = Set.newBuilder[GeneratorOption]
    if (flatPackage) {
      optionsBuilder += FlatPackage
    }
    if (javaConversions) {
      optionsBuilder += JavaConversions
    }
    if (grpc) {
      optionsBuilder += Grpc
    }
    if (singleLineToProtoString) {
      optionsBuilder += SingleLineToProtoString
    }
    if (asciiFormatToString) {
      optionsBuilder += AsciiFormatToString
    }
    if (!lenses) {
      optionsBuilder += NoLenses
    }
    if (scala3Sources) {
      optionsBuilder += Scala3Sources
    }
    apply(optionsBuilder.result())
  }
}
