package scalapb

import java.io.File

import protocbridge.{ProtocBridge, ProtocCodeGenerator}

case class Config(
    version: String = "-v380",
    throwException: Boolean = false,
    args: Seq[String] = Seq.empty,
    customProtocLocation: Option[String] = None,
    namedGenerators: Seq[(String, ProtocCodeGenerator)] = Seq("scala" -> ScalaPbCodeGenerator)
)

class ScalaPbcException(msg: String) extends RuntimeException(msg)

object ScalaPBC {
  private val CustomPathArgument = "--protoc="
  private val CustomGenArgument  = "--custom-gen="

  def processArgs(args: Array[String]): Config = {
    case class State(cfg: Config, passThrough: Boolean)

    args
      .foldLeft(State(Config(), false)) {
        case (state, item) =>
          (state.passThrough, item) match {
            case (false, v) if v.startsWith("-v") => state.copy(cfg = state.cfg.copy(version = v))
            case (false, "--throw")               => state.copy(cfg = state.cfg.copy(throwException = true))
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
            case (_, other) =>
              state.copy(passThrough = true, cfg = state.cfg.copy(args = state.cfg.args :+ other))
          }
      }
      .cfg
  }

  def main(args: Array[String]): Unit = {
    val config = processArgs(args)

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
      params = config.args
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
