import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

scalaVersion := "2.11.7"

PB.protobufSettings

PB.runProtoc in PB.protobufConfig := { args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
}

libraryDependencies ++= Seq(
  // For finding google/protobuf/descriptor.proto
  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.5.29" % PB.protobufConfig
)

