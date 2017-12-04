package com.trueaccord.scalapb

package object scalapb {
  @deprecated("Please use scalapb.options package instead of com.trueaccord.scalapb.scalapb. " +
    "You might got this message since you are using an outdated version of scalapb.proto", "0.7.0")
  val ScalapbProto = _root_.scalapb.options.ScalapbProto
}
