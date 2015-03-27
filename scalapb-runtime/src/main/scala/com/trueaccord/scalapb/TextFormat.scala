package com.trueaccord.scalapb

import java.math.BigInteger

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FieldDescriptor.Type
import com.google.protobuf.{TextFormat => TextFormatJava}
import com.trueaccord.scalapb.Descriptors._
import org.parboiled2._
import shapeless.{::, HNil}
import scala.collection.JavaConversions.asJavaIterable

import scala.collection.mutable
import scala.util.{Try, Success, Failure}

case class ParsedByteArray(byteArray: Array[Byte]) {
  override def toString = "ByteArray(\"" + byteArray.map(_.toInt).mkString(",") + "\", \"" + byteArray.map(_.toChar).mkString + "\")"
}

class TextFormat(val input: ParserInput) extends Parser with StringBuilding {

  def Root(v: MessageDescriptor) = rule {
    WhiteSpace ~ Message(v) ~ EOI ~> (v.companion.fromFieldsMap _)
  }

  type FieldToDescriptorMap = Map[String, FieldDescriptor]

  type FieldMap = Map[Int, Any]

  def Message(v: MessageDescriptor): Rule1[FieldMap] = {
    val fieldMap = v.fields.map(f => (f.name, f)).toMap
    rule {
      zeroOrMore(Pair(fieldMap)) ~> { pairs: Seq[Seq[(FieldDescriptor, Any)]] =>
        pairs.flatten.groupBy(_._1).mapValues(_.map(_._2)).map {
          case (fd, values) if fd.label == Descriptors.Optional =>
            (fd.number, values.lastOption)
          case (fd, values) if fd.label == Descriptors.Repeated =>
            (fd.number, values)
          case (fd, values) =>
            (fd.number, values.last)
        }
      }
    }
  }

  def identifier: Rule1[String] = rule {
    capture(oneOrMore(CharPredicate.AlphaNum ++ '_')) ~ WhiteSpace
  }

  def NewLine = rule("\r\n" | "\n")

  def Comment: Rule0 = rule(
      "#" ~ (!NewLine ~ ANY).* ~ &(NewLine | EOI)
  )

  def WhiteSpace = rule {
    quiet((anyOf(" \n\r\t\f") | Comment).*)
  }

  def ws(c: Char) = rule {
    (c ~ WhiteSpace).named(c.toString)
  }

  private def enumValueByName(enumDesc: EnumDescriptor, name: String): Either[String, GeneratedEnum] =
    enumDesc.companion.values.find(_.name == name).toRight(s"""Enum type "${enumDesc.name}" has no value named "$name"""")

  private def enumValueByNumber(enumDesc: EnumDescriptor, number: Int): Either[String, GeneratedEnum] =
    enumDesc.companion.values.find(_.id == number).toRight(s"""Enum type "${enumDesc.name}" has no value with number $number""")

  private val chset = java.nio.charset.StandardCharsets.UTF_8

  def MatchField(fieldMap: FieldToDescriptorMap): Rule1[FieldDescriptor] = rule {
    identifier ~> {
      (s: String) =>
        (test(fieldMap.contains(s)) | fail("a known field")) ~ push(fieldMap(s))
    }
  }

  def ParseValueByType(fd: FieldDescriptor): Rule1[(FieldDescriptor, Any)] = rule {
    run {
      fd.fieldType.fieldType match {
        case Type.DOUBLE => DoubleRule
        case Type.FLOAT => FloatRule
        case Type.INT64 => int64
        case Type.UINT64 => uint64
        case Type.INT32 => int32
        case Type.FIXED64 => uint64
        case Type.FIXED32 => uint32
        case Type.BOOL => boolean
        case Type.STRING => BytesLiteral ~> { _.toStringUtf8 }
        case Type.BYTES => BytesLiteral
        case Type.UINT32 => uint32
        case Type.SFIXED32 => int32
        case Type.SFIXED64 => int64
        case Type.SINT32 => int32
        case Type.SINT64 => int64
        case Type.GROUP => fail("groups are not supported") ~ int32
        case Type.MESSAGE => (
          ws('{').named("\"{\"") ~ SubMessage(fd).named("field values") ~ ws('}').named("\"}\"")
            | ws('<').named("\"<\"") ~ SubMessage(fd).named("field values") ~ ws('>').named("\">\"")
          ) ~> { map: FieldMap =>fd.fieldType.asInstanceOf[MessageType].m.companion.fromFieldsMap(map).asInstanceOf[Any] }
        case Type.ENUM => (
          int32 ~> { v: Int => enumValueByNumber(fd.fieldType.asInstanceOf[EnumType].descriptor, v.intValue())}
            | identifier ~> { v: String => enumValueByName(fd.fieldType.asInstanceOf[EnumType].descriptor, v)}
          ) ~> {
          v: Either[String, GeneratedEnum] => (test(v.isRight) | fail(v.left.get)) ~ push(v.right.get)
        }
      }
    } ~> { v: Any => (fd, v) }
  }

  def Colon = rule {
    ws(':').named("\":\"")
  }

  def ColonUnlessMessage = rule {
    run {
      fd: FieldDescriptor =>
        if (fd.fieldType.fieldType == Type.MESSAGE)
          (optional(Colon) ~ push(fd))
        else
          (Colon ~ push(fd))
    }
  }

  def RepeatedValues(fd: FieldDescriptor) = rule {
    ws('[') ~ zeroOrMore(ParseValueByType(fd)).separatedBy(ws(',')) ~ ws(']')
  }

  def Pair(fieldMap: FieldToDescriptorMap): Rule1[Seq[(FieldDescriptor, Any)]] = rule {
    MatchField(fieldMap) ~ ColonUnlessMessage.named("\":\"") ~> {
      fd: FieldDescriptor =>
        if (fd.label == Repeated) (RepeatedValues(fd) | ParseValueByType(fd) ~> { Seq(_) })
        else ParseValueByType(fd) ~> { Seq(_) }
    }
  }

  def SubMessage(fd: FieldDescriptor): Rule1[FieldMap] = rule {
    run {
      test(fd.fieldType.isInstanceOf[MessageType]) ~
        Message(fd.fieldType.asInstanceOf[MessageType].m)
    }
  }

  def boolean: Rule1[Boolean] = rule (
    atomic(("true" | "t" | "1") ~ push(true) |
    ("false" | "f" | "0") ~ push(false)).named("'true' or 'false'") ~ WhiteSpace
  )

  def int32: Rule1[Int] = rule {
    atomic(RangeCheckedBigIntRule(isSigned = true, isLong = false) ~> ((_: BigInteger).intValue()))
  }

  def uint32: Rule1[Int] = rule {
    atomic(RangeCheckedBigIntRule(isSigned = false, isLong = false) ~> ((_: BigInteger).intValue()))
  }

  def int64: Rule1[Long] = rule {
    atomic(RangeCheckedBigIntRule(isSigned = true, isLong = true) ~> ((_: BigInteger).longValue()))
  }

  def uint64: Rule1[Long] = rule {
    atomic(RangeCheckedBigIntRule(isSigned = false, isLong = true) ~> ((_: BigInteger).longValue()))
  }

  def RangeCheckedBigIntRule(isSigned: Boolean = true, isLong: Boolean): Rule1[BigInteger] = rule {
    BigIntRule ~ run {
      v: BigInteger =>
        test({
          val negative = v.signum() == -1
          val maxBits = if (isLong) {
            if (isSigned) 63 else 64
          } else {
            if (isSigned) 31 else 32
          }
          (isSigned || !negative) && // if unsigned, must be non-negative
            v.bitLength() <= maxBits
        }).named("number is in range") ~ push(v)
    }
  }

  def BigIntRule: Rule1[BigInteger] = rule {
    (optional(capture("-")) ~> { (v: Option[String]) => v.isDefined}) ~ (
      "0x" ~ capture(oneOrMore(CharPredicate.HexDigit)) ~> {
        v: String => new BigInteger(v, 16)
      } | "0" ~ capture(oneOrMore(OctalDigit)) ~> {
        v: String => new BigInteger(v, 8)
      } | capture(Number) ~> ((v: String) => new BigInteger(v))) ~ WhiteSpace ~> {
      (n: Boolean, num: BigInteger) => if (n) num.negate() else num
    }
  }

  def FloatRule: Rule1[Float] = rule(
    atomic(optional(capture('-')) ~ (
      ignoreCase("infinityf") | ignoreCase("infinity") | ignoreCase("inff") | ignoreCase("inf")) ~> {
      sign: Option[String] => sign.fold(Float.PositiveInfinity)(_ => Float.NegativeInfinity)
    }
      | (ignoreCase("nanf") | ignoreCase("nan")) ~ push(Float.NaN)
      | capture(Number) ~> { v: String => v.toFloat }) ~ WhiteSpace
  )

  def DoubleRule: Rule1[Double] = rule (
    atomic(optional(capture('-')) ~ (ignoreCase("infinity") | ignoreCase("inf")) ~> {
      sign: Option[String] => sign.fold(Double.PositiveInfinity)(_ => Double.NegativeInfinity)
    }
      | ignoreCase("nan") ~ push(Double.NaN)
      | capture(Number) ~> { v: String => v.toDouble }) ~ WhiteSpace
  )

  def Number = rule {
    optional('.') ~ anyOf("0123456789+-") ~ zeroOrMore(CharPredicate.AlphaNum ++ "_.+-")
  }

  def concatByteStrings(s: java.lang.Iterable[ByteString]): ByteString =
    ByteString.copyFrom(s)

  def BytesLiteral: Rule1[ByteString] = rule {
    oneOrMore(SingleBytesLiteral.named("string")) ~> { s: Iterable[ByteString] => ByteString.copyFrom(s) }
  }

  def SingleBytesLiteral: Rule1[ByteString] = rule {
    atomic(('\"' ~ capture((noneOf("\"\n\\") | ('\\' ~ ANY)).*) ~ ws('\"') |
      '\'' ~ capture((noneOf("\'\n\\") | ('\\' ~ ANY)).*) ~ ws('\'')) ~ run {
      s: String =>
        Try(TextFormat.unescapeBytes(s)) match {
          case Success(bs) => push(bs)
          case Failure(TextFormat.StringParsingException(msg)) => fail(msg) ~ push(null)
          case Failure(_) => fail(s"Error occurred when parsing string: '$s'") ~ push(null)
        }
    })
  }

  val OctalDigit = CharPredicate('0' to '7')
}

class TextGenerator(singleLine: Boolean = true) {
  private val sb = mutable.StringBuilder.newBuilder
  private var indentLevel = 0
  private var lineStart = true

  private def maybeNewLine(): Unit = {
    if (lineStart) {
      if (!singleLine)
        sb.append(" " * (indentLevel * 2))
      else if (sb.nonEmpty) sb.append(' ')
    }
  }

  def add(s: String): TextGenerator = {
    maybeNewLine()
    sb.append(s)
    lineStart = false
    this
  }

  def addNewLine(s: String): TextGenerator = {
    maybeNewLine()
    sb.append(s)
    if (!singleLine) {
      sb.append('\n')
    }
    lineStart = true
    this
  }

  def indent(): TextGenerator = {
    indentLevel += 1
    this
  }

  def outdent(): TextGenerator = {
    indentLevel -= 1
    this
  }

  def result(): String = sb.result()
}

class Printer(singleLineMode: Boolean) {
  def print(t: GeneratedMessage, out: TextGenerator): Unit = {
    t.getAllFields.sortBy(_._1.number).foreach {
      case (fd, value) =>
        printField(fd, value, out)
    }
  }

  def printToString(t: GeneratedMessage) = {
    val out = new TextGenerator(singleLine = singleLineMode)
    print(t, out)
    out.result()
  }

  def printField(fd: FieldDescriptor, value: Any, out: TextGenerator) = {
    if (fd.label == Repeated) value.asInstanceOf[Seq[Any]].foreach {
      v: Any => printSingleField(fd, v, out)
    } else if (fd.label == Optional) value.asInstanceOf[Option[Any]].foreach {
      v: Any => printSingleField(fd, v, out)
    } else {
      printSingleField(fd, value, out)
    }
  }

  def printSingleField(fd: FieldDescriptor, value: Any, out: TextGenerator) = {
    out.add(fd.name)
    if (fd.fieldType.fieldType == Type.MESSAGE) {
      out.addNewLine(" {").indent()
    } else {
      out.add(": ")
    }
    printFieldValue(fd, value, out)
    if (fd.fieldType.fieldType == Type.MESSAGE) {
      out.outdent().addNewLine("}")
    } else {
      out.addNewLine("")
    }
  }

  def printFieldValue(fd: FieldDescriptor, value: Any, out: TextGenerator): Unit =
    fd.fieldType.fieldType match {
      case Type.INT32 | Type.SINT32 | Type.SFIXED32 =>
        out.add(value.asInstanceOf[Int].toString)
      case Type.INT64 | Type.SINT64 | Type.SFIXED64 =>
        out.add(value.asInstanceOf[Long].toString)
      case Type.BOOL =>
        out.add(value.asInstanceOf[Boolean].toString)
      case Type.FLOAT =>
        out.add(value.asInstanceOf[Float].toString)
      case Type.DOUBLE =>
        out.add(value.asInstanceOf[Double].toString)
      case Type.UINT32 | Type.FIXED32 =>
        out.add(TextFormatJava.unsignedToString(value.asInstanceOf[Int]))
      case Type.UINT64 | Type.FIXED64 =>
        out.add(TextFormatJava.unsignedToString(value.asInstanceOf[Long]))
      case Type.STRING =>
        val escapeNonAscii = true
        out
          .add("\"")
          .add(
            if(escapeNonAscii)
              TextFormat.escapeText(value.asInstanceOf[String])
            else
              TextFormatJava.escapeDoubleQuotesAndBackslashes(value.asInstanceOf[String]))
          .add("\"")

      case Type.BYTES =>
        out
          .add("\"")
            .add(TextFormat.escapeBytes(value.asInstanceOf[ByteString]))
            .add("\"")

      case Type.ENUM =>
          out.add(value.asInstanceOf[GeneratedEnum].name)

      case Type.GROUP => ???

      case Type.MESSAGE =>
        print(value.asInstanceOf[GeneratedMessage], out)
    }
}

object TextFormat {
  sealed trait ByteParsingState extends Any

  case object Default extends ByteParsingState

  case object EscapeMode extends ByteParsingState

  case object Hex0 extends ByteParsingState
  case class Hex1(b: Int) extends AnyVal with ByteParsingState

  case class Octal1(b: Int) extends AnyVal with ByteParsingState
  case class Octal2(b: Int) extends AnyVal with ByteParsingState

  private lazy val DEFAULT_PRINTER = new Printer(singleLineMode = false)

  def printToString(m: GeneratedMessage) = DEFAULT_PRINTER.printToString(m)

  private def digitValue(c: Byte) = {
    if ('0' <= c && c <= '9')
      (c - '0')
    else if ('a' <= c && c <= 'z')
      (c - 'a' + 10)
    else
      (c - 'A' + 10)
  }

  private val CH_A: java.lang.Byte = 'a'.toByte
  private val CH_B: java.lang.Byte = 'b'.toByte
  private val CH_F: java.lang.Byte = 'f'.toByte
  private val CH_N: java.lang.Byte = 'n'.toByte
  private val CH_R: java.lang.Byte = 'r'.toByte
  private val CH_T: java.lang.Byte = 't'.toByte
  private val CH_V: java.lang.Byte = 'v'.toByte
  private val CH_SLASH: java.lang.Byte = '\\'.toByte
  private val CH_SQ: java.lang.Byte = '\''.toByte
  private val CH_DQ: java.lang.Byte = '\"'.toByte
  private val CH_X: java.lang.Byte = 'x'.toByte

  private val CH_SLASH_A: java.lang.Byte = 7.toByte
  private val CH_SLASH_B: java.lang.Byte = '\b'.toByte
  private val CH_SLASH_N: java.lang.Byte = '\n'.toByte
  private val CH_SLASH_F: java.lang.Byte = '\f'.toByte
  private val CH_SLASH_R: java.lang.Byte = '\r'.toByte
  private val CH_SLASH_T: java.lang.Byte = '\t'.toByte
  private val CH_SLASH_V: java.lang.Byte = 11.toByte

  case class StringParsingException(msg: String) extends RuntimeException(msg)

  def escapeBytes(bytes: ByteString): String = {
    import scala.collection.JavaConversions._
    val sb = mutable.StringBuilder.newBuilder
    bytes.foreach {
      case CH_SLASH_A => sb.append("\\a")
      case CH_SLASH_B => sb.append("\\b")
      case CH_SLASH_F => sb.append("\\f")
      case CH_SLASH_N => sb.append("\\n")
      case CH_SLASH_R => sb.append("\\r")
      case CH_SLASH_T => sb.append("\\t")
      case CH_SLASH_V => sb.append("\\v")
      case CH_SLASH => sb.append("\\\\")
      case CH_SQ => sb.append("\\\'")
      case CH_DQ  => sb.append("\\\"")
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

  def unescapeBytes(charString: String): ByteString = {
    import scala.collection.JavaConversions._
    val input: ByteString = ByteString.copyFromUtf8(charString)
    val result = mutable.ArrayBuilder.make[Byte]
    result.sizeHint(input.size)

    def defaultHandle(b: Byte) = {
      if (b == CH_SLASH) EscapeMode
      else {
        result += b
        Default
      }
    }

    val endState = input.foldLeft[ByteParsingState](Default) {
      (state, b) => (state, b) match {
        case (Default, b) => defaultHandle(b)
        case (EscapeMode, b) if b >= '0' && b <= '7' => Octal1(digitValue(b))
        case (EscapeMode, CH_A) => result += 7; Default
        case (EscapeMode, CH_B) => result += '\b'; Default
        case (EscapeMode, CH_F) => result += '\f'; Default
        case (EscapeMode, CH_N) => result += '\n'; Default
        case (EscapeMode, CH_R) => result += '\r'; Default
        case (EscapeMode, CH_T) => result += '\t'; Default
        case (EscapeMode, CH_V) => result += 11; Default
        case (EscapeMode, CH_SLASH) => result += '\\'; Default
        case (EscapeMode, CH_SQ) => result += '\''; Default
        case (EscapeMode, CH_DQ) => result += '\"'; Default
        case (EscapeMode, CH_X) => Hex0
        case (EscapeMode, _) => throw new StringParsingException("Invalid escape sequence: " + b.toChar)
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
        case (Hex0, b) if (CharPredicate.HexDigit(b.toChar)) => Hex1(digitValue(b))
        case (Hex0, b) => throw new StringParsingException("'\\x' with no digits")
        case (Hex1(i), b) if (CharPredicate.HexDigit(b.toChar)) =>
          result += (16 * i + digitValue(b)).toByte
          Default
        case (Hex1(i), b) =>
          result += i.toByte
          defaultHandle(b)
      }
    }

    endState match {
      case Hex0 => throw new StringParsingException("'\\x' with no digits")
      case EscapeMode => throw new StringParsingException("Invalid escape sequence '\\' at end of string.")
      case Hex1(i) => result += i.toByte
      case Octal1(i) => result += i.toByte
      case Octal2(i) => result += i.toByte
      case Default =>
    }

    ByteString.copyFrom(result.result())
  }

  def escapeText(input: String): String =
    escapeBytes(ByteString.copyFromUtf8(input))

  def unescapeText(input: String): String =
    unescapeBytes(input).toStringUtf8
}
