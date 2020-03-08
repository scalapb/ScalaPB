package scalapb

import java.io.File

import protocbridge.{ProtocBridge, ProtocCodeGenerator}
import coursier.parse.DependencyParser
import coursier.core.Configuration
import com.github.ghik.silencer.silent

case class Config(
    version: String = "-v" + scalapb.compiler.Version.protobufVersion,
    throwException: Boolean = false,
    args: Seq[String] = Seq.empty,
    customProtocLocation: Option[String] = None,
    namedGenerators: Seq[(String, ProtocCodeGenerator)] = Seq("scala" -> ScalaPbCodeGenerator),
    artifacts: Seq[String] = Seq.empty
)

class ScalaPbcException(msg: String) extends RuntimeException(msg)

object ScalaPBC {
  private val CustomPathArgument     = "--protoc="
  private val CustomGenArgument      = "--custom-gen="
  private val PluginArtifactArgument = "--plugin-artifact="

  def processArgs(args: Array[String]): Config = {
    case class State(cfg: Config, passThrough: Boolean)

    args
      .foldLeft(State(Config(), false)) {
        case (state, item) =>
          (state.passThrough, item) match {
            case (false, "--")      => state.copy(passThrough = true)
            case (false, "--throw") => state.copy(cfg = state.cfg.copy(throwException = true))
            case (false, p) if p.startsWith(CustomGenArgument) =>
              val Array(genName, klassName) = p.substring(CustomGenArgument.length).split('=')
              val klass                     = Class.forName(klassName + "$")
              val gen                       = klass.getField("MODULE$").get(klass).asInstanceOf[ProtocCodeGenerator]
              state.copy(
                cfg =
                  state.cfg.copy(namedGenerators = state.cfg.namedGenerators :+ (genName -> gen))
              )
            case (false, p) if p.startsWith(CustomPathArgument) =>
              state.copy(
                cfg = state.cfg
                  .copy(customProtocLocation = Some(p.substring(CustomPathArgument.length)))
              )
            case (false, p) if p.startsWith(PluginArtifactArgument) =>
              state.copy(cfg =
                state.cfg
                  .copy(artifacts =
                    state.cfg.artifacts :+ p.substring(PluginArtifactArgument.length())
                  )
              )
            case (false, v) if v.startsWith("-v") => state.copy(cfg = state.cfg.copy(version = v))
            case (_, other) =>
              state.copy(passThrough = true, cfg = state.cfg.copy(args = state.cfg.args :+ other))
          }
      }
      .cfg
  }

  @silent("method right in class Either is deprecated")
  def main(args: Array[String]): Unit = {
    import coursier._
    val config = processArgs(args)

    val arts: Seq[Either[String, (Dependency, java.io.File)]] = for {
      art <- config.artifacts
      maybeDep = DependencyParser.dependency(
        art,
        scala.util.Properties.versionNumberString,
        Configuration.empty
      )
    } yield for {
      dep <- maybeDep.right
      elm <- Fetch().addDependencies(dep).run() match {
        case List(elm) => Right(elm)
        case _         => Left(s"Could not find artifact for $art")
      }
    } yield dep -> elm

    val pluginArgs: Seq[String] = arts.map {
      case Left(err) =>
        if (!config.throwException) {
          System.err.println(err)
          sys.exit(1)
        } else {
          throw new ScalaPbcException(s"Error: $err")
        }
      case Right((dep, file)) =>
        file.setExecutable(true)
        s"--plugin=${dep.module.name.value}=${file.getAbsolutePath()}"
    }

    val code = ProtocBridge.runWithGenerators(
      protoc = config.customProtocLocation match {
        case Some(path) =>
          val executable = new File(path)
          a =>
            com.github.os72.protocjar.Protoc
              .runProtoc(executable.getAbsolutePath, config.version +: a.toArray)
        case None =>
          a => com.github.os72.protocjar.Protoc.runProtoc(config.version +: a.toArray)
      },
      namedGenerators = config.namedGenerators,
      params = config.args ++ pluginArgs
    )

    if (!config.throwException) {
      sys.exit(code)
    } else {
      if (code != 0) {
        throw new ScalaPbcException(s"Exit with code $code")
      }
    }
  }
}
