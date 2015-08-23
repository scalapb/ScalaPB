package com.trueacord.scalapb

import com.trueaccord.scalapb.compiler.ProtocDriverFactory

object Spbc extends App {
  val code = ProtocDriverFactory.create().buildRunner({
    args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v261" +: args.toArray)
  })(args)
  sys.exit(code)
}
