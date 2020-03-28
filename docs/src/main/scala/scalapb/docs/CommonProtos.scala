package scalapb.docs

object CommonProtos {
  def row(libName: String, version: String): String = {
    s"""## $libName
       |ScalaPB 1.0.x:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_1.0" % "${version}" % "protobuf"
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_1.0" % "${version}"
       |)
       |```
       |ScalaPB 0.9.x:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.9" % "${version}" % "protobuf"
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.9" % "${version}"
       |)
       |```
       |""".stripMargin
  }

  def header: String = ""
  def footer: String = ""

  def table: String = {
      Seq(
          ("proto-google-common-protos", "1.17.0-0"),
          ("proto-google-cloud-pubsub-v1", "1.85.1-0")
      ).map((row _).tupled).mkString(header, "", "footer")
  }

  def printTable(): Unit = {
      println(table)
  }

}
