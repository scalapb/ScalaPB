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

  def apply(e: String, enclosingType: EnclosingType, mustCopy: Boolean = false): String =
    ExpressionBuilder.run(this)(e, enclosingType, mustCopy)
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

  def convertCollection(expr: String, enclosingType: EnclosingType): String = {
    val convert = List(enclosingType match {
      case Collection(DescriptorImplicits.ScalaVector)   => MethodApplication("toVector")
      case Collection(DescriptorImplicits.ScalaSeq)      => MethodApplication("toSeq")
      case Collection(DescriptorImplicits.ScalaMap)      => MethodApplication("toMap")
      case Collection(DescriptorImplicits.ScalaIterable) => MethodApplication("toIterable")
      case Collection(cc)                                => FunctionApplication("_root_.scalapb.internal.compat.convertTo")
      case _                                             => Identity
    })
    runSingleton(convert)(expr)
  }

  def runCollection(
      es: List[LiteralExpression]
  )(e0: String, enclosingType: EnclosingType, mustCopy: Boolean): String = {
    require(enclosingType != EnclosingType.None)
    val nontrivial: List[LiteralExpression] = es.filterNot(_.isIdentity)
    val needVariable =
      nontrivial
        .filterNot(_.isIdentity)
        .dropRight(1)
        .exists(_.isFunctionApplication)

    val e = if (enclosingType.isInstanceOf[Collection]) {
      e0 + ".iterator"
    } else e0

    if (needVariable)
      convertCollection(s"""$e.map(__e => ${runSingleton(nontrivial)("__e")})""", enclosingType)
    else if (nontrivial.nonEmpty) {
      val f = nontrivial match {
        case List(FunctionApplication(name)) =>
          name
        case _ =>
          runSingleton(nontrivial)("_")
      }
      convertCollection(s"""$e.map($f)""", enclosingType)
    } else if (mustCopy) {
      convertCollection(s"""$e.map(_root_.scala.Predef.identity)""", enclosingType)
    } else e0
  }

  def run(
      es: List[LiteralExpression]
  )(e: String, enclosingType: EnclosingType, mustCopy: Boolean): String =
    enclosingType match {
      case EnclosingType.None =>
        runSingleton(es)(e)
      case _ =>
        runCollection(es)(e, enclosingType, mustCopy)
    }

  def run(es: Expression)(e: String, enclosingType: EnclosingType, mustCopy: Boolean): String =
    es match {
      case ExpressionList(l)       => run(l)(e, enclosingType, mustCopy)
      case expr: LiteralExpression => run(expr :: Nil)(e, enclosingType, mustCopy)
    }
}

sealed trait EnclosingType

object EnclosingType {
  case object None        extends EnclosingType
  case object ScalaOption extends EnclosingType

  /** Indicates that the result should be a collection with type constructor cc, such as List, Map.
    */
  case class Collection(cc: String) extends EnclosingType
}
