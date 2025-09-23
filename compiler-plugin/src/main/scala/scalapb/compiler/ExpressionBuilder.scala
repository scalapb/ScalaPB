package scalapb.compiler
import scalapb.compiler.EnclosingType.Collection

sealed trait Expression extends Product with Serializable {
  def andThen(other: Expression) = (this, other) match {
    case (Identity, e2: LiteralExpression)              => e2
    case (e1, Identity)                                 => e1
    case (ExpressionList(l1), ExpressionList(l2))       => ExpressionList(l2 ++ l1)
    case (ExpressionList(l1), e: LiteralExpression)     => ExpressionList(e :: l1)
    case (e: LiteralExpression, ExpressionList(l2))     => ExpressionList(l2 :+ e)
    case (e1: LiteralExpression, e2: LiteralExpression) => ExpressionList(e2 :: e1 :: Nil)
  }

  def apply(
      e: String,
      sourceType: EnclosingType,
      targetType: EnclosingType,
      mustCopy: Boolean
  ): String =
    ExpressionBuilder.run(this, e, sourceType, targetType, mustCopy)

  def apply(e: String, sourceType: EnclosingType, targetType: EnclosingType): String =
    ExpressionBuilder.run(this, e, sourceType, targetType, false)

  def apply(e: String, sourceType: EnclosingType, mustCopy: Boolean): String =
    ExpressionBuilder.run(this, e, sourceType, sourceType, mustCopy)

  def apply(e: String, sourceType: EnclosingType): String =
    ExpressionBuilder.run(this, e, sourceType, sourceType, false)
}

case class ExpressionList(l: List[LiteralExpression]) extends Expression

sealed trait LiteralExpression extends Expression {
  def isIdentity: Boolean
  def isFunctionApplication: Boolean
}

case object Identity extends LiteralExpression {
  def isIdentity: Boolean            = true
  def isFunctionApplication: Boolean = false
}

case class FunctionApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean            = false
  def isFunctionApplication: Boolean = true
}

case class MethodApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean            = false
  def isFunctionApplication: Boolean = false
}

case class OperatorApplication(op: String) extends LiteralExpression {
  def isIdentity: Boolean            = false
  def isFunctionApplication: Boolean = false
}

object ExpressionBuilder {
  def runSingleton(es: List[LiteralExpression])(e: String): String = es match {
    case Nil                               => e
    case Identity :: tail                  => runSingleton(tail)(e)
    case FunctionApplication(name) :: tail => s"$name(${runSingleton(tail)(e)})"
    case MethodApplication(name) :: tail   => s"${runSingleton(tail)(e)}.$name"
    case OperatorApplication(name) :: tail => s"${runSingleton(tail)(e)} $name"
  }

  def convertCollection(expr: String, targetType: EnclosingType): String = {
    val convert = List(targetType match {
      case Collection(_, Some(tc)) => FunctionApplication(s"${tc}.fromIteratorUnsafe")
      case Collection(DescriptorImplicits.ScalaVector, _)   => MethodApplication("toVector")
      case Collection(DescriptorImplicits.ScalaSeq, _)      => MethodApplication("toSeq")
      case Collection(DescriptorImplicits.ScalaMap, _)      => MethodApplication("toMap")
      case Collection(DescriptorImplicits.ScalaIterable, _) =>
        FunctionApplication("_root_.scalapb.internal.compat.toIterable")
      case Collection(_, _) => FunctionApplication("_root_.scalapb.internal.compat.convertTo")
      case _                => Identity
    })
    runSingleton(convert)(expr)
  }

  def runCollection(
      es: List[LiteralExpression]
  )(e0: String, sourceType: EnclosingType, targetType: EnclosingType, mustCopy: Boolean): String = {
    require(sourceType != EnclosingType.None)
    val nontrivial: List[LiteralExpression] = es.filterNot(_.isIdentity)
    val needVariable                        =
      nontrivial
        .filterNot(_.isIdentity)
        .dropRight(1)
        .exists(_.isFunctionApplication)

    val e = sourceType match {
      case Collection(_, Some(tc))                             => s"$tc.toIterator($e0)"
      case Collection(DescriptorImplicits.ScalaIterator, None) => e0
      case Collection(_, None)                                 => s"$e0.iterator"
      case _                                                   => e0
    }

    val forceTypeConversion = sourceType match {
      case Collection(_, Some(_)) if sourceType != targetType => true
      case _                                                  => false
    }

    if (needVariable)
      convertCollection(s"""$e.map(__e => ${runSingleton(nontrivial)("__e")})""", targetType)
    else if (nontrivial.nonEmpty) {
      val f = nontrivial match {
        case List(FunctionApplication(name)) =>
          s"${name}(_)"
        case _ =>
          runSingleton(nontrivial)("_")
      }
      convertCollection(s"""$e.map($f)""", targetType)
    } else if (mustCopy) {
      convertCollection(s"""$e.map(_root_.scala.Predef.identity)""", targetType)
    } else if (forceTypeConversion) {
      convertCollection(e, targetType)
    } else e0
  }

  private[scalapb] def run(
      es: List[LiteralExpression],
      e: String,
      sourceType: EnclosingType,
      targetType: EnclosingType,
      mustCopy: Boolean
  ): String =
    sourceType match {
      case EnclosingType.None =>
        runSingleton(es)(e)
      case _ =>
        runCollection(es)(e, sourceType, targetType, mustCopy)
    }

  private[scalapb] def run(
      es: Expression,
      e: String,
      sourceType: EnclosingType,
      targetType: EnclosingType,
      mustCopy: Boolean
  ): String =
    es match {
      case ExpressionList(l)       => run(l, e, sourceType, targetType, mustCopy)
      case expr: LiteralExpression => run(expr :: Nil, e, sourceType, targetType, mustCopy)
    }

  @deprecated("0.10.10", "Use Expression()")
  def run(
      es: Expression
  )(e: String, sourceType: EnclosingType, mustCopy: Boolean): String =
    run(es, e, sourceType, sourceType, mustCopy)
}

sealed trait EnclosingType {
  def asType(enclosed: String): String = this match {
    case EnclosingType.None              => enclosed
    case EnclosingType.ScalaOption       => s"${DescriptorImplicits.ScalaOption}[$enclosed]"
    case EnclosingType.Collection(cc, _) => s"${cc}[$enclosed]"
  }
}

object EnclosingType {
  case object None        extends EnclosingType
  case object ScalaOption extends EnclosingType

  /** Indicates that the result should be a collection with type constructor cc, such as List, Map.
    */
  case class Collection(cc: String, typeClass: Option[String]) extends EnclosingType {
    def this(cc: String) = { this(cc, scala.None) }
  }
}
