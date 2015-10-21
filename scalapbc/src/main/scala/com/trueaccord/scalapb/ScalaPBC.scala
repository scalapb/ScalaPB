package com.trueaccord.scalapb

import com.trueaccord.scalapb.compiler.ProtocDriverFactory

object ScalaPBC extends App {
  val code = ProtocDriverFactory.create().buildRunner({
    args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
  })(args)
  sys.exit(code)
}
