package com.trueaccord.scalapb

import java.io.{FilterInputStream, InputStream}

/**
 * Based on com.google.protobuf.AbstractMessageLite.Builder#LimitedInputStream.
 */
class LimitedInputStream(val is: InputStream, private var limit: Int) extends FilterInputStream(is) {

  override def available(): Int = super.available min limit

  override def read(): Int =
    if (limit <= 0) -1
    else {
      val result = super.read()
      if (result >= 0) {
        limit -= 1
      }
      result
    }

  override def read(bytes: Array[Byte], off: Int, len: Int): Int =
    if (limit <= 0) {
      -1
    } else {
      val actualLen = len min limit
      val result = super.read(bytes, off, actualLen)
      if (result >= 0) {
        limit -= result
      }
      result
    }

  override def skip(n: Long): Long = {
    val result = super.skip(n min limit)
    if (result >= 0) {
      limit = (limit - result).toInt
    }
    result
  }
}
