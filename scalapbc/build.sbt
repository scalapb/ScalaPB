enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.6.0",
  "org.typelevel" %% "cats-effect" % "1.0.0"
)
