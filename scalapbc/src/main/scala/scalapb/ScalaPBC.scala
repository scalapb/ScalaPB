package scalapb

import java.io.File

import protocbridge.{ProtocBridge, ProtocCodeGenerator}
import coursier.parse.DependencyParser
import coursier.core.Configuration
import coursier.core.Dependency
import java.net.URLClassLoader
import java.util.jar.JarInputStream
import java.io.FileInputStream
import protocbridge.SandboxedJvmGenerator
import scala.util.{Try, Success, Failure}
import protocbridge.ProtocRunner

case class Config(
    version: String = scalapb.compiler.Version.protobufVersion,
    throwException: Boolean = false,
    args: Seq[String] = Seq.empty,
    customProtocLocation: Option[String] = None,
    namedGenerators: Seq[(String, ProtocCodeGenerator)] = Seq("scala" -> ScalaPbCodeGenerator),
    executableArtifacts: Seq[String] = Seq.empty,
    jvmPlugins: Seq[(String, String)] = Seq.empty
)

class ScalaPbcException(msg: String) extends RuntimeException(msg)

object ScalaPBC {
  private val CustomPathArgument     = "--protoc="
  private val CustomGenArgument      = "--custom-gen="
  private val PluginArtifactArgument = "--plugin-artifact="
  private val JvmPluginArgument      = "--jvm-plugin="

  def processArgs(args: Array[String]): Config = {
    case class State(cfg: Config, passThrough: Boolean)

    args
      .foldLeft(State(Config(), false)) { case (state, item) =>
        (state.passThrough, item) match {
          case (false, "--")      => state.copy(passThrough = true)
          case (false, "--throw") => state.copy(cfg = state.cfg.copy(throwException = true))
          case (false, p) if p.startsWith(CustomGenArgument) =>
            val Array(genName, klassName) = p.substring(CustomGenArgument.length).split('=')
            val klass                     = Class.forName(klassName + "$")
            val gen = klass.getField("MODULE$").get(klass).asInstanceOf[ProtocCodeGenerator]
            state.copy(
              cfg = state.cfg.copy(namedGenerators = state.cfg.namedGenerators :+ (genName -> gen))
            )
          case (false, p) if p.startsWith(JvmPluginArgument) =>
            val Array(genName, artifactName) = p.substring(JvmPluginArgument.length).split('=')
            state.copy(
              cfg = state.cfg.copy(jvmPlugins = state.cfg.jvmPlugins :+ (genName -> artifactName))
            )
          case (false, p) if p.startsWith(CustomPathArgument) =>
            state.copy(
              cfg = state.cfg
                .copy(customProtocLocation = Some(p.substring(CustomPathArgument.length)))
            )
          case (false, p) if p.startsWith(PluginArtifactArgument) =>
            state.copy(cfg =
              state.cfg
                .copy(executableArtifacts =
                  state.cfg.executableArtifacts :+ p.substring(PluginArtifactArgument.length())
                )
            )
          case (false, v) if v.startsWith("-v") =>
            state.copy(cfg = state.cfg.copy(version = v.substring(2).trim))
          case (_, other) =>
            state.copy(passThrough = true, cfg = state.cfg.copy(args = state.cfg.args :+ other))
        }
      }
      .cfg
  }

  def fetchArtifact(artifact: String): Either[String, (Dependency, Seq[File])] = {
    import coursier._
    for {
      dep <- DependencyParser
        .dependency(
          artifact,
          scala.util.Properties.versionNumberString,
          Configuration.empty
        )
      runResult = Fetch().addDependencies(dep).run()
      outcome <-
        if (runResult.isEmpty) Left(s"Could not find artifact for $artifact")
        else Right(runResult)
    } yield (dep, outcome)
  }

  def fetchArtifacts(
      artifacts: Seq[(String, String)]
  ): Either[String, Seq[(String, (Dependency, Seq[File]))]] =
    artifacts.foldLeft[Either[String, Seq[(String, (Dependency, Seq[File]))]]](Right(Seq())) {
      case (Left(error), _)                  => Left(error)
      case (Right(result), (name, artifact)) =>
        fetchArtifact(artifact) match {
          case Right((dep, files)) => Right(result :+ ((name, (dep, files))))
          case Left(error)         => Left(error)
        }
    }

  def findMainClass(f: File): Either[String, String] = {
    val jin = new JarInputStream(new FileInputStream(f))
    try {
      val manifest = jin.getManifest()
      Option(manifest.getMainAttributes().getValue("Main-Class"))
        .toRight("Could not find main class for plugin")
        .map(_ + "$")
    } finally {
      jin.close()
    }
  }

  private def getProtoc(version: String): Either[String, String] = {
    Try(protocbridge.CoursierProtocCache.getProtoc(version)) match {
      case Success(f) => Right(f.getAbsolutePath())
      case Failure(e) => Left(e.getMessage)
    }
  }

  private[scalapb] def runProtoc(config: Config): Int = {
    if (
      config.namedGenerators
        .map(_._1)
        .toSet
        .intersect(config.jvmPlugins.map(_._1).toSet)
        .nonEmpty
    ) {
      throw new RuntimeException(
        s"Same plugin name provided by $PluginArtifactArgument and $JvmPluginArgument"
      )
    }

    def fatalError(err: String): Nothing = {
      if (config.throwException) {
        throw new ScalaPbcException(s"Error: $err")
      } else {
        System.err.println(err)
        sys.exit(1)
      }
    }

    val jvmGenerators = fetchArtifacts(
      config.jvmPlugins
    ) match {
      case Left(error) => fatalError(error)
      case Right(arts) =>
        arts.map { case (name, (_, files)) =>
          val urls      = files.map(_.toURI().toURL()).toArray
          val loader    = new URLClassLoader(urls, null)
          val mainClass = findMainClass(files.head) match {
            case Right(v)  => v
            case Left(err) => fatalError(err)
          }
          name -> SandboxedJvmGenerator.load(mainClass, loader)
        }
    }

    val pluginArgs = fetchArtifacts(
      config.executableArtifacts.map(a => ("", a))
    ) match {
      case Left(error) => fatalError(error)
      case Right(arts) =>
        arts.map {
          case (_, (dep, file :: Nil)) =>
            file.setExecutable(true)
            s"--plugin=${dep.module.name.value}=${file.getAbsolutePath()}"
          case (_, (dep, files)) =>
            fatalError(s"Got ${files.length} files for dependency $dep. Only one expected.")
        }
    }

    val protoc =
      config.customProtocLocation
        .getOrElse(getProtoc(config.version).fold(fatalError(_), identity(_)))

    ProtocBridge.runWithGenerators(
      ProtocRunner(protoc),
      namedGenerators = config.namedGenerators ++ jvmGenerators,
      params = config.args ++ pluginArgs
    )
  }

  def main(args: Array[String]): Unit = {
    val config = processArgs(args)
    val code   = runProtoc(config)

    if (!config.throwException) {
      sys.exit(code)
    } else {
      if (code != 0) {
        throw new ScalaPbcException(s"Exit with code $code")
      }
    }
  }
}
