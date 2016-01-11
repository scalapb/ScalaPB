package object scalapb {
  def generator: protocbridge.Generator = generator()

  def generator(flatPackage: Boolean = false, 
                javaConversions: Boolean = false,
                grpc: Boolean = false): protocbridge.Generator = {
    scalapb.ScalaPbCodeGenerator.toGenerator(
      Seq(
        javaConversions -> "java_conversions",
        flatPackage -> "flat_package",
        grpc -> "grpc").filter(_._1).map(_._2))
  }
}
