package scalapb.docs

object CommonProtos {
  def row(libName: String, version: String): String = {
    s"""### $libName
       |ScalaPB 0.11.x:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.11" % "${version}" % "protobuf",
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.11" % "${version}"
       |)
       |```
       |ScalaPB 0.10.x:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.10" % "${version}" % "protobuf",
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.10" % "${version}"
       |)
       |```
       |ScalaPB 0.9.x:
       |```scala
       |libraryDependencies ++= Seq(
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.9" % "${version}" % "protobuf",
       |  "com.thesamet.scalapb.common-protos" %% "${libName}-scalapb_0.9" % "${version}"
       |)
       |```
       |""".stripMargin
  }

  def header: String = ""
  def footer: String = ""

  def table: String = {
    Seq(
      ("proto-google-common-protos", "2.9.6-0"),
      ("proto-google-cloud-pubsub-v1", "1.102.20-0"),
      ("pgv-proto", "0.6.13-0")
    ).map((row _).tupled).mkString(header, "", "footer")
  }

  def printTable(): Unit = {
    println(table)
  }

}
