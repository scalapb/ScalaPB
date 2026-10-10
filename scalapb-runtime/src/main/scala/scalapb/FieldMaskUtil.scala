package scalapb

import com.google.protobuf.field_mask.FieldMask
import scala.annotation.tailrec

object FieldMaskUtil {
  private[this] def isLower(c: Char): Boolean = {
    'a' <= c && c <= 'z'
  }

  private[this] def isUpper(c: Char): Boolean = {
    'A' <= c && c <= 'Z'
  }

  private[this] def toUpper(c: Char): Char = {
    if (isLower(c)) {
      (c - 32).asInstanceOf[Char]
    } else {
      c
    }
  }

  private[this] def toLower(c: Char): Char = {
    if (isUpper(c)) {
      (c + 32).asInstanceOf[Char]
    } else {
      c
    }
  }

  private[this] def toLowerCase(s: String, b: Appendable): Unit = {
    @tailrec
    def loop(i: Int): Unit = {
      if (i < s.length) {
        b.append(toLower(s(i)))
        loop(i + 1)
      }
    }

    loop(0)
  }

  private[scalapb] def lowerSnakeCaseToCamelCase(name: String): String = {
    lowerSnakeCaseToCamelCaseWithBuffer(name, new java.lang.StringBuilder(name.length)).toString
  }

  private[scalapb] def lowerSnakeCaseToCamelCaseWithBuffer(
      name: String,
      buf: Appendable
  ): buf.type = {
    def toProperCase(s: String): Unit = if (!s.isEmpty) {
      buf.append(toUpper(s(0)))
      toLowerCase(s.substring(1), buf)
    }

    val array = name.split("\\_")

    @tailrec
    def loop(i: Int): Unit = {
      if (i < array.length) {
        toProperCase(array(i))
        loop(i + 1)
      }
    }

    if (array.nonEmpty) {
      toLowerCase(array(0), buf)
      loop(1)
    }
    buf
  }

  private[scalapb] def camelCaseToSnakeCase(str: String): String = {
    if (str.isEmpty) {
      ""
    } else {
      val buf = new java.lang.StringBuilder(str.length)
      buf.append(toLower(str(0)))

      @tailrec
      def loop(i: Int): String = {
        if (i < str.length) {
          val c = str(i)
          if (isUpper(c)) {
            buf.append('_')
            buf.append((c + 32).asInstanceOf[Char])
          } else {
            buf.append(c)
          }
          loop(i + 1)
        } else {
          buf.toString
        }
      }

      loop(1)
    }
  }

  def toJsonString(fieldMask: FieldMask): String = {
    val buf   = new java.lang.StringBuilder()
    var first = true
    fieldMask.paths.foreach { path =>
      if (!path.isEmpty) {
        if (!first) {
          buf.append(',')
        }
        lowerSnakeCaseToCamelCaseWithBuffer(path, buf)
        first = false
      }
    }
    buf.toString
  }

  def fromJsonString(value: String): FieldMask = {
    val result = value
      .split(",")
      .iterator
      .withFilter(_.nonEmpty)
      .map { path => camelCaseToSnakeCase(path) }
      .toList
    FieldMask(result)
  }

  /** Applies a field mask to a message.
    */
  def applyFieldMask[M <: GeneratedMessage: GeneratedMessageCompanion](
      message: M,
      fieldMask: FieldMask
  ): M = {
    FieldMaskTree(fieldMask).applyToMessage(message)
  }

  /** Checks that a field mask selects a certain message field number.
    */
  def containsFieldNumber[M <: GeneratedMessage: GeneratedMessageCompanion](
      fieldMask: FieldMask,
      fieldNumber: Int
  ): Boolean = {
    FieldMaskTree(fieldMask).containsField[M](fieldNumber)
  }

  /** Checks that field masks corresponds to message schema.
    */
  def isValid[M <: GeneratedMessage: GeneratedMessageCompanion](fieldMask: FieldMask): Boolean = {
    FieldMaskTree(fieldMask).isValidFor[M]
  }

  /** Constructs a field mask based on message field numbers.
    * @return
    *   Some(mask) if all fields number are valid, `None` otherwise.
    */
  def fromFieldNumbers[M <: GeneratedMessage: GeneratedMessageCompanion](
      fieldNumbers: Int*
  ): Option[FieldMask] = {
    val companion = implicitly[GeneratedMessageCompanion[M]]
    val fields    = fieldNumbers.map { fieldNumber =>
      companion.scalaDescriptor.findFieldByNumber(fieldNumber)
    }
    val fieldNames = fields.foldLeft[Option[Vector[String]]](Some(Vector.empty)) {
      case (Some(acc), Some(field)) => Some(acc :+ field.name)
      case _                        => None
    }
    fieldNames.map(names => FieldMask(paths = names))
  }

  /** Constructs a field mask based on a predicate for message field numbers.
    */
  def selectFieldNumbers[M <: GeneratedMessage: GeneratedMessageCompanion](
      fieldNumberPredicate: Int => Boolean
  ): FieldMask = {
    val companion    = implicitly[GeneratedMessageCompanion[M]]
    val fieldNumbers = companion.scalaDescriptor.fields.map(_.number).filter(fieldNumberPredicate)
    fromFieldNumbers[M](fieldNumbers: _*).get
  }

  /** Unions two or more field masks.
    */
  def union(fieldMask: FieldMask, otherMasks: FieldMask*): FieldMask = {
    val tree = otherMasks.foldLeft(FieldMaskTree(fieldMask)) { case (acc, nextMask) =>
      FieldMaskTree.union(acc, FieldMaskTree(nextMask))
    }
    tree.fieldMask
  }
}
