package com.trueaccord.scalapb

import protocbridge.ProtocBridge

import scalapb.ScalaPbCodeGenerator

object ScalaPBC {
  def main(args: Array[String]): Unit = {
    val (versionFlag, protocArgs) =
      if (args.length >= 1 && args(0).startsWith("-v")) {
        (args.head, args.tail)
      } else {
        ("-v300", args)
      }

    val code = ProtocBridge.runWithGenerators(
      a => com.github.os72.protocjar.Protoc.runProtoc(versionFlag +: a.toArray),
      protocArgs,
      Seq("scala" -> ScalaPbCodeGenerator))

    sys.exit(code)
  }
}
