package scalapb.textformat

import java.math.BigInteger

import com.github.ghik.silencer.silent
import com.google.protobuf.ByteString
import scalapb.TextFormatError

import scala.collection.mutable

private[scalapb] object TextFormatUtils {
  import Constants._

  sealed trait ByteParsingState extends Any

  case object Default extends ByteParsingState

  case object EscapeMode extends ByteParsingState

  case object Hex0 extends ByteParsingState

  case class Hex1(b: Int) extends AnyVal with ByteParsingState

  case class Octal1(b: Int) extends AnyVal with ByteParsingState

  case class Octal2(b: Int) extends AnyVal with ByteParsingState

  case class Error(s: String) extends AnyVal with ByteParsingState

  private val HEXDIGIT = "0123456789abcdefABCDEF"

  def isHexDigit(c: Char) = HEXDIGIT.contains(c)

  private def digitValue(c: Byte) = {
    if ('0' <= c && c <= '9')
      (c - '0')
    else if ('a' <= c && c <= 'z')
      (c - 'a' + 10)
    else
      (c - 'A' + 10)
  }

  def escapeBytes(bytes: ByteString): String = {
    val sb = new mutable.StringBuilder()
    bytes.foreach {
      case CH_SLASH_A => sb.append("\\a")
      case CH_SLASH_B => sb.append("\\b")
      case CH_SLASH_F => sb.append("\\f")
      case CH_SLASH_N => sb.append("\\n")
      case CH_SLASH_R => sb.append("\\r")
      case CH_SLASH_T => sb.append("\\t")
      case CH_SLASH_V => sb.append("\\v")
      case CH_SLASH   => sb.append("\\\\")
      case CH_SQ      => sb.append("\\\'")
      case CH_DQ      => sb.append("\\\"")
      case b if b >= 0x20 =>
        sb.append(b.toChar)
      case b =>
        sb.append('\\')
        sb.append((48 + ((b >>> 6) & 3)).toChar)
        sb.append((48 + ((b >>> 3) & 7)).toChar)
        sb.append((48 + (b & 7)).toChar)
    }
    sb.result()
  }

  // JavaConversions is removed in 2.13. Using JavaConverters doesn't work for us; asScala is not available
  // on the pure Scala implementation of ByteString.
  implicit class JavaConversions(val iter: java.lang.Iterable[java.lang.Byte]) extends AnyVal {
    import scala.jdk.CollectionConverters._
    def foldLeft[B](z: B)(op: (B, java.lang.Byte) => B) = iter.asScala.foldLeft(z)(op)

    def foreach[U](f: java.lang.Byte => U): Unit = iter.asScala.foreach(f)
  }

  def unescapeBytes(
      charString: String
  ): Either[TextFormatError, ByteString] with Product with Serializable = {
    val input: ByteString = ByteString.copyFromUtf8(charString)
    val result            = mutable.ArrayBuilder.make[Byte]
    result.sizeHint(input.size)

    def defaultHandle(b: Byte) = {
      if (b == CH_SLASH) EscapeMode
      else {
        result += b
        Default
      }
    }

    val endState = input.foldLeft[ByteParsingState](Default) { (state, b) =>
      (state, b) match {
        case (e: Error, _)                           => e
        case (Default, b)                            => defaultHandle(b)
        case (EscapeMode, b) if b >= '0' && b <= '7' => Octal1(digitValue(b))
        case (EscapeMode, CH_A)                      => result += 7; Default
        case (EscapeMode, CH_B)                      => result += '\b'; Default
        case (EscapeMode, CH_F)                      => result += '\f'; Default
        case (EscapeMode, CH_N)                      => result += '\n'; Default
        case (EscapeMode, CH_R)                      => result += '\r'; Default
        case (EscapeMode, CH_T)                      => result += '\t'; Default
        case (EscapeMode, CH_V)                      => result += 11; Default
        case (EscapeMode, CH_SLASH)                  => result += '\\'; Default
        case (EscapeMode, CH_SQ)                     => result += '\''; Default
        case (EscapeMode, CH_DQ)                     => result += '\"'; Default
        case (EscapeMode, CH_X)                      => Hex0
        case (EscapeMode, _)                         => Error("Invalid escape sequence: " + b.toChar)
        case (Octal1(i), b) if b >= '0' && b <= '7' =>
          Octal2(i * 8 + digitValue(b))
        case (Octal1(i), b) =>
          result += i.toByte
          defaultHandle(b)
        case (Octal2(i), b) if b >= '0' && b <= '7' =>
          result += (i * 8 + digitValue(b)).toByte
          Default
        case (Octal2(i), b) =>
          result += i.toByte
          defaultHandle(b)
        case (Hex0, b) if (isHexDigit(b.toChar)) => Hex1(digitValue(b))
        case (Hex0, _)                           => Error("'\\x' with no digits")
        case (Hex1(i), b) if (isHexDigit(b.toChar)) =>
          result += (16 * i + digitValue(b)).toByte
          Default
        case (Hex1(i), b) =>
          result += i.toByte
          defaultHandle(b)
      }
    }

    endState match {
      case Error(e)   => Left(TextFormatError(e))
      case Hex0       => Left(TextFormatError("'\\x' with no digits"))
      case EscapeMode => Left(TextFormatError("Invalid escape sequence '\\' at end of string."))
      case Hex1(i) =>
        result += i.toByte
        Right(ByteString.copyFrom(result.result()))
      case Octal1(i) =>
        result += i.toByte
        Right(ByteString.copyFrom(result.result()))
      case Octal2(i) =>
        result += i.toByte
        Right(ByteString.copyFrom(result.result()))
      case Default =>
        Right(ByteString.copyFrom(result.result()))
    }
  }

  def escapeText(input: String): String =
    escapeBytes(ByteString.copyFromUtf8(input))

  @silent("method right in class Either is deprecated")
  def unescapeText(input: String): Either[TextFormatError, String] =
    unescapeBytes(input).right.map(_.toStringUtf8())

  /** Convert an unsigned 32-bit integer to a string. */
  def unsignedToString(value: Int): String = {
    if (value >= 0) java.lang.Integer.toString(value)
    else java.lang.Long.toString(value & 0X00000000FFFFFFFFL)
  }

  /** Convert an unsigned 64-bit integer to a string. */
  def unsignedToString(value: Long): String = {
    if (value >= 0) java.lang.Long.toString(value)
    else BigInteger.valueOf(value & 0X7FFFFFFFFFFFFFFFL).setBit(63).toString
  }

  /**
    * Escape double quotes and backslashes in a String for unicode output of a message.
    */
  def escapeDoubleQuotesAndBackslashes(input: String): String = {
    input.replace("\\", "\\\\").replace("\"", "\\\"")
  }
}
