package scalapb.textformat

import scalapb.TextFormatException
import scala.annotation.tailrec

class Parser(text: String) {
  val it = new Tokenizer(text)

  @tailrec
  final def parseKeyValueList(closer: Option[String], acc: Seq[TField]): Seq[TField] = {
    if (!it.hasNext) closer match {
      case Some(cl) => throw parseException(s"Expected ${cl} but reached end of stream")
      case None     => acc
    }
    else {
      val token = it.next()
      val pos   = it.lastPosition
      closer match {
        case Some(cl) if token == cl => acc
        case _                       =>
          if (!Tokenizer.isIdentifier(token)) throw {
            throw parseException(s"Expected identifier, got $token")
          }
          if (!it.hasNext) throw parseException("Expected value after identifier")
          else {
            val value = parseValue(it.next())
            parseKeyValueList(closer, acc :+ TField(pos, token, value))
          }
      }

    }
  }

  @tailrec
  final def rep[T](acc: Seq[T], readyForNext: Boolean, parse: String => Option[T]): Seq[T] = {
    if (!it.hasNext) {
      throw parseException("Unclosed list of messages.")
    } else {
      val token = it.next()
      if (token == "]") {
        if (acc.nonEmpty && readyForNext)
          throw parseException("Expected value, found ']' (trailing commas not allowed)")
        else acc
      } else if (token == ",") {
        if (readyForNext) throw parseException("Unexpected comma")
        else rep(acc, true, parse)
      } else
        parse(token) match {
          case None      => throw parseException("Expected value")
          case Some(msg) => rep(acc :+ msg, false, parse)
        }
    }
  }

  def messageArray: Seq[TValue] = rep(Nil, true, tryMessage)

  def valueArray: Seq[TValue] = rep(Nil, true, m => tryMessage(m).orElse(tryPrimitiveValue(m)))

  def tryMessageArray(init: String): Option[TArray] =
    if (init == "[") Some(TArray(it.lastPosition, messageArray)) else None

  def tryValueArray(init: String): Option[TArray] =
    if (init == "[") Some(TArray(it.lastPosition, valueArray))
    else None

  def tryMessage(init: String): Option[TMessage] = init match {
    case "{" =>
      Some(TMessage(it.lastPosition, parseKeyValueList(Some("}"), Nil)))
    case "<" =>
      Some(TMessage(it.lastPosition, parseKeyValueList(Some(">"), Nil)))
    case _ => None
  }

  def tryHexIntegral(token: String, index: Int, sign: Int): Option[BigInt] = {
    val hexPrefix = (index + 1 < token.length) && token(index) == '0' && token(index + 1) == 'x'
    if (!hexPrefix) None
    else {
      val s = token.substring(index + 2)
      if (
        !s.forall(ch =>
          (ch >= '0' && ch <= '9') || (ch >= 'a' && ch <= 'f') || (ch >= 'A' && ch <= 'F')
        )
      )
        throw parseException(s"Invalid hex literal: $token")
      else
        Some(BigInt(s, 16) * sign)
    }
  }

  def tryOctIntegral(token: String, index: Int, sign: Int): Option[BigInt] = {
    val octPrefix = (index < token.length) && token(index) == '0'
    if (!octPrefix) None
    else {
      val s = token.substring(index)
      if (!s.forall(ch => ch >= '0' && ch <= '7'))
        throw parseException(s"Invalid octal literal: $token")
      else {
        Some(BigInt(s, 8) * sign)
      }
    }
  }

  def tryDecimal(token: String, index: Int, sign: Int): Option[BigInt] = {
    val s = token.substring(index)
    if (!s.forall(ch => ch >= '0' && ch <= '9'))
      None
    else {
      Some(BigInt(s, 10) * sign)
    }
  }

  def tryBigInt(token: String): Option[BigInt] = {
    if (token.isEmpty) None
    else {
      val (sign, index) =
        if (token.startsWith("-")) (-1, 1)
        else if (token.startsWith("+")) (1, 1)
        else (1, 0)
      tryHexIntegral(token, index, sign)
        .orElse(tryOctIntegral(token, index, sign))
        .orElse(tryDecimal(token, index, sign))
    }
  }

  def tryBigIntLiteral(token: String): Option[TIntLiteral] = {
    val pos = it.lastPosition
    tryBigInt(token).map(TIntLiteral(pos, _))
  }

  def tryLiteral(token: String): Option[TLiteral] =
    if (
      token.forall(c =>
        (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_') || (c == '-')
      )
    )
      Some(TLiteral(it.lastPosition, token))
    else None

  def tryFractional(token: String): Option[TLiteral] = {
    val pos = it.lastPosition
    if (Tokenizer.FRACTIONAL.matcher(token).matches()) Some(TLiteral(pos, token)) else None
  }

  def tryPrimitiveValue(token: String): Option[TPrimitive] = {
    tryFractional(token)
      .orElse(tryBigIntLiteral(token))
      .orElse(tryBytesLiteral(token))
      .orElse(tryLiteral(token))
  }

  def tryBytesLiteral(token: String): Option[TBytes] = {
    val pos = it.lastPosition
    val sb  = new StringBuilder
    if (!tryBytesLiteralSingle(token, sb)) {
      None
    } else {
      while (tryBytesLiteralSingle(it.currentToken, sb)) {
        it.nextToken()
      }
      Some(TBytes(pos, sb.result()))
    }
  }

  def tryBytesLiteralSingle(token: String, sb: StringBuilder): Boolean = {
    val quote = if (token.isEmpty()) ' ' else token(0)
    if (quote != '"' && quote != '\'') false
    else if (token.length() < 2 || token.last != quote)
      throw parseException("String missing ending quote")
    else {
      sb ++= (token.substring(1, token.length() - 1))
      true
    }
  }

  def tryColonValue(init: String): Option[TValue] =
    if (init != ":") None
    else if (!it.hasNext) throw parseException("Expected value")
    else {
      val token = it.next()
      tryMessage(token)
        .orElse(tryValueArray(token))
        .orElse(tryPrimitiveValue(token))
    }

  def parseValue(init: String): TValue =
    tryMessage(init)
      .orElse(tryMessageArray(init))
      .orElse(tryColonValue(init))
      .getOrElse(
        throw parseException(s"Expected ':', '{', '<', or '[', got '$init'")
      )

  def parseMessage: TMessage = {
    TMessage(it.lastPosition, parseKeyValueList(None, Nil))
  }

  def parseException(msg: String): TextFormatException = {
    throw new TextFormatException(
      s"$msg (line ${it.lastPosition.line + 1}, column ${it.lastPosition.col + 1})"
    )
  }

}
