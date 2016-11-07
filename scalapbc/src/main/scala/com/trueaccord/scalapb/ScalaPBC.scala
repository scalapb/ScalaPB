package com.trueaccord.scalapb

import protocbridge.ProtocBridge

import scalapb.ScalaPbCodeGenerator

case class Config(
  version: String = "-v310",
  throwException: Boolean = false,
  args: Seq[String] = Seq.empty)

class ScalaPbcException(msg: String) extends RuntimeException(msg)

object ScalaPBC {
  def processArgs(args: Array[String]): Config = {
    case class State(cfg: Config, passThrough: Boolean)

    args.foldLeft(State(Config(), false)) {
      case (state, item) =>
        (state.passThrough, item) match {
          case (false, v) if v.startsWith("-v") => state.copy(cfg = state.cfg.copy(version = v))
          case (false, "--throw") => state.copy(cfg = state.cfg.copy(throwException = true))
          case (_, other) => state.copy(
            passThrough = true, cfg=state.cfg.copy(args = state.cfg.args :+ other))
        }
    }.cfg
  }

  def main(args: Array[String]): Unit = {
    val config = processArgs(args)

    val code = ProtocBridge.runWithGenerators(
      protoc = a => com.github.os72.protocjar.Protoc.runProtoc(config.version +: a.toArray),
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
