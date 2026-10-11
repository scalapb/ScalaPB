package scalapb.docs

object CommonProtos {
  def dependencies(libName: String, version: String, line: String, label: String): String = {
    s"""$label:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_$line" % "$version" % "protobuf",
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_$line" % "$version"
       |)
       |```
       |""".stripMargin
  }

  def row(libName: String, version: String): String = {
    s"### $libName\n" +
      dependencies(libName, version, "0.11", "ScalaPB 0.11.x") +
      dependencies(libName, version, "1.0", "ScalaPB 1.0 prereleases (experimental)")
  }

  def table: String = {
    Seq(
      ("proto-google-common-protos", "2.78.0-1"),
      ("proto-google-cloud-pubsub-v1", "1.157.0-0"),
      ("proto-google-cloud-dataproc-v1", "4.96.0-0"),
      ("pgv-proto", "1.3.3-1")
    ).map((row _).tupled).mkString("\n")
  }

  def printTable(): Unit = {
    println(table)
  }
}
