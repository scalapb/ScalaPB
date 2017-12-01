package scalapb

import java.io.File

import protocbridge.ProtocBridge

case class Config(
  version: String = "-v310",
  throwException: Boolean = false,
  args: Seq[String] = Seq.empty,
  customProtocLocation: Option[String] = None)

class ScalaPbcException(msg: String) extends RuntimeException(msg)

object ScalaPBC {
  private val customPathArgument =  "--protoc="

  def processArgs(args: Array[String]): Config = {
    case class State(cfg: Config, passThrough: Boolean)

    args.foldLeft(State(Config(), false)) {
      case (state, item) =>
        (state.passThrough, item) match {
          case (false, v) if v.startsWith("-v") => state.copy(cfg = state.cfg.copy(version = v))
          case (false, "--throw") => state.copy(cfg = state.cfg.copy(throwException = true))
          case (false, p) if p.startsWith(customPathArgument) => state.copy(cfg = state.cfg.copy(customProtocLocation = Some(p.substring(customPathArgument.length))))
          case (_, other) => state.copy(
            passThrough = true, cfg=state.cfg.copy(args = state.cfg.args :+ other))
        }
    }.cfg
  }

  def main(args: Array[String]): Unit = {
    val config = processArgs(args)

    val code = ProtocBridge.runWithGenerators(
      protoc = config.customProtocLocation match {
        case Some(path) =>
          val executable = new File(path)
          a => com.github.os72.protocjar.Protoc.runProtoc(executable.getAbsolutePath, config.version +: a.toArray)
        case None =>
          a => com.github.os72.protocjar.Protoc.runProtoc(config.version +: a.toArray)
      },
      namedGenerators = Seq("scala" -> ScalaPbCodeGenerator),
      params = config.args)

    if (!config.throwException) {
      sys.exit(code)
    } else {
      if (code != 0) {
        throw new ScalaPbcException(s"Exit with code $code")
      }
    }
  }
}
