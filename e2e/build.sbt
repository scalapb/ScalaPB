import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

PB.scalapbVersion in PB.protobufConfig := com.trueaccord.scalapb.Version.scalapbVersion

PB.javaConversions in PB.protobufConfig := true

// PB.runProtoc in PB.protobufConfig := {
//   s: Seq[String] =>
//     println("Should go through here: " + s)
//     (Seq("protoc") ++ s)!
// }

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test",
  "org.scalacheck" %% "scalacheck" % "1.11.6" % "test",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.Version.scalapbVersion % PB.protobufConfig
)

