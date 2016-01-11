package com.trueaccord.scalapb

import protocbridge.ProtocBridge

import scalapb.ScalaPbCodeGenerator

object ScalaPBC extends App {
  val code = ProtocBridge.runWithGenerators(
    args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray),
    args,
    Seq("scala" -> ScalaPbCodeGenerator))

  sys.exit(code)
}
