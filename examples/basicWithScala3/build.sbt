scalaVersion := "3.3.0"

Compile / PB.targets := Seq(
  scalapb.gen(scala3Sources = true) -> (Compile / sourceManaged).value / "scalapb"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-source", "future",
  "-feature"
)