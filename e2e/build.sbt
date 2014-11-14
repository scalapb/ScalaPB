import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)
