import sbt._
import sbt.internal.inc.ScalaInstance
import Keys._
import Dependencies.versions
import sbtprotoc.ProtocPlugin.autoImport.PB
import sbtassembly.AssemblyPlugin.autoImport._

object BuildHelper {
  val commonScalacOptions = Seq(
    "-deprecation",
    "-release",
    "8",
    "-feature"
  )

  val scalac2Options = Seq(
    "-explaintypes",
    "-Xfatal-warnings",
    "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
    "-Xlint:constant",     // Evaluation of a constant arithmetic expression results in an error.
    "-Xlint:delayedinit-select",     // Selecting member of DelayedInit.
    "-Xlint:doc-detached",           // A Scaladoc comment appears to be detached from its element.
    "-Xlint:inaccessible",           // Warn about inaccessible types in method signatures.
    "-Xlint:infer-any",              // Warn when a type argument is inferred to be `Any`.
    "-Xlint:missing-interpolator",   // A string literal appears to be missing an interpolator id.
    "-Xlint:nullary-unit",           // Warn when nullary methods return Unit.
    "-Xlint:option-implicit",        // Option.apply used implicit view.
    "-Xlint:package-object-classes", // Class or object defined in package object.
    "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
    "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
    "-Xlint:stars-align",    // Pattern sequence wildcard must align with sequence component.
    "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
    "-Ywarn-dead-code",             // Warn when dead code is identified.
    "-Ywarn-extra-implicit",   // Warn when more than one implicit parameter section is defined.
    "-Ywarn-numeric-widen",    // Warn when numerics are widened.
    "-Ywarn-unused:implicits", // Warn if an implicit parameter is unused.
    "-Ywarn-unused:imports",   // Warn if an import selector is not referenced.
    "-Ywarn-unused:locals",    // Warn if a local definition is unused.
    "-Ywarn-unused:params",    // Warn if a value parameter is unused.
    "-Ywarn-unused:patvars",   // Warn if a variable bound in a pattern is unused.
    "-Ywarn-unused:privates",  // Warn if a private member is unused.
    "-Ywarn-value-discard",    // Warn when non-Unit expression results are unused.
    "-Ybackend-parallelism",
    "8", // Enable paralellisation — change to desired number!
    "-Ycache-plugin-class-loader:last-modified", // Enables caching of classloaders for compiler plugins
    "-Ycache-macro-class-loader:last-modified" // and macro definitions. This can lead to performance improvements.
  )

  val scalac3Options = Seq(
    "-language:implicitConversions",
    "-source:3.0-migration"
  )

  val scala2Settings = Seq()

  val scala3Settings = Seq()

  def isScala3 = Def.setting[Boolean] { scalaVersion.value.startsWith("3.") }

  def commonSettings = Seq(
    scalacOptions ++= commonScalacOptions ++ (if (isScala3.value) scalac3Options
                                              else scalac2Options),
    libraryDependencies ++= (if (!isScala3.value) Dependencies.silencer else Nil),
    libraryDependencies += Dependencies.scalaCollectionCompat.value,
    Compile / unmanagedSourceDirectories += (Compile / scalaSource).value.getParentFile / (if (
                                                                                             isScala3.value
                                                                                           )
                                                                                             "scala-3"
                                                                                           else
                                                                                             "scala-2"),
    Test / unmanagedSourceDirectories += (Test / scalaSource).value.getParentFile / (if (
                                                                                       isScala3.value
                                                                                     )
                                                                                       "scala-3"
                                                                                     else
                                                                                       "scala-2"),
    assembly / assemblyMergeStrategy := {
      case PathList("scala", "annotation", "nowarn.class" | "nowarn$.class") =>
        MergeStrategy.first
      case x =>
        (assembly / assemblyMergeStrategy).value.apply(x)
    },
    compileOrder  := CompileOrder.JavaThenScala,
    versionScheme := Some("early-semver")
  )

  object Compiler {
    val generateVersionFile = Compile / sourceGenerators += Def.task {
      val file = (Compile / sourceManaged).value / "scalapb" / "compiler" / "Version.scala"
      IO.write(
        file,
        s"""package scalapb.compiler
           |object Version {
           |  val scalapbVersion = "${version.value}"
           |  val protobufVersion = "${versions.protobuf}"
           |  val grpcJavaVersion = "${versions.grpc}"
           |}""".stripMargin
      )
      Seq(file)
    }

    val generateEncodingFile = Compile / sourceGenerators += Def.task {
      val src =
        (LocalRootProject / baseDirectory).value / "scalapb-runtime" / "src" / "main" / "scala" / "scalapb" / "Encoding.scala"
      val dest =
        (Compile / sourceManaged).value / "scalapb" / "compiler" / "internal" / "Encoding.scala"
      val s = IO.read(src).replace("package scalapb", "package scalapb.internal")
      IO.write(dest, s"// DO NOT EDIT. Copy of $src\n\n" + s)
      Seq(dest)
    }

    val shadeTarget = settingKey[String]("Target to use when shading")
  }

  val scalajsSourceMaps = scalacOptions += {
    val a = (LocalRootProject / baseDirectory).value.toURI.toString
    val g = "https://raw.githubusercontent.com/scalapb/ScalaPB/" + sys.process
      .Process("git rev-parse HEAD")
      .lineStream_!
      .head
    val flag = if (isScala3.value) "-scalajs-mapSourceURI" else "-P:scalajs:mapSourceURI"
    s"$flag:$a->$g/"
  }
}
