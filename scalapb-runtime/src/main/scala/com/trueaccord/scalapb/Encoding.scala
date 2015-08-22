package com.trueaccord.scalapb

import scala.collection.mutable

/** Utility functions to encode/decode byte arrays as Base64 strings.
  *
  * Used internally between the protocol buffer compiler and the runtime to encode
  * messages.
  *
  * We could have used Apache Commons, but we would like to avoid an additional dependency.
  * java.xml.bind.DataTypeConverter.parseBase64Binary is not available on Android. And the Java
  * native java.util.Base64 is only available for Java 8...
  */
object Encoding {
  private val alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="

  def fromBase64(textInput: String): Array[Byte] = {
    fromBase64Inner(textInput.filter(alphabet.contains(_: Char)))
  }

  private def fromBase64Inner(input: String): Array[Byte] = {
    require(input.length % 4 == 0)
    val lastEqualsIndex = input.indexOf('=')
    val outputLength = (input.length * 3) / 4 - (
      if (lastEqualsIndex > 0) (input.length() - input.indexOf('=')) else 0)
    val builder = mutable.ArrayBuilder.make[Byte]
    builder.sizeHint(outputLength)

    for { i <- 0 until(input.length, 4) } {
      val b = input.substring(i, i + 4).map(alphabet.indexOf(_: Char).toByte)
      builder += ((b(0) << 2) | (b(1) >> 4)).toByte
      if (b(2) < 64) {
        builder += ((b(1) << 4) | (b(2) >> 2)).toByte
        if (b(3) < 64)  {
          builder += ((b(2) << 6) | b(3)).toByte
        }
      }
    }
    builder.result()
  }

  def toBase64(in: Array[Byte]): String = {
    val out = mutable.StringBuilder.newBuilder
    var b: Int = 0
    for { i <- 0 until (in.length, 3) } {
      b = (in(i) & 0xFC) >> 2
      out.append(alphabet(b))
      b = (in(i) & 0x03) << 4
      if (i + 1 < in.length)      {
        b |= (in(i + 1) & 0xF0) >> 4
        out.append(alphabet(b))
        b = (in(i + 1) & 0x0F) << 2
        if (i + 2 < in.length)  {
          b |= (in(i + 2) & 0xC0) >> 6
          out.append(alphabet(b))
          b = in(i + 2) & 0x3F
          out.append(alphabet(b))
        } else  {
          out.append(alphabet(b))
          out.append('=')
        }
      } else      {
        out.append(alphabet(b))
        out.append("==")
      }
    }
    out.result()
  }

}
