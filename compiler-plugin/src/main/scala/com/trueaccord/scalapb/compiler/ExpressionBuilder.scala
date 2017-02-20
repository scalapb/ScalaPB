package com.trueaccord.scalapb.compiler

import ExpressionBuilder.OuterType

sealed trait Expression extends Product with Serializable {
  def andThen(other: Expression) = (this, other) match {
    case (Identity, e2: LiteralExpression) => e2
    case (e1, Identity) => e1
    case (ExpressionList(l1), ExpressionList(l2)) => ExpressionList(l2 ++ l1)
    case (ExpressionList(l1), e: LiteralExpression) => ExpressionList(e :: l1)
    case (e: LiteralExpression, ExpressionList(l2)) => ExpressionList(l2 :+ e)
    case (e1: LiteralExpression, e2: LiteralExpression) => ExpressionList(e2 :: e1 :: Nil)
  }

  def apply(e: String, tpe: OuterType): String = ExpressionBuilder.run(this)(e, tpe)
}

case class ExpressionList(l: List[LiteralExpression]) extends Expression

sealed trait LiteralExpression extends Expression {
  def isIdentity: Boolean
  def isFunctionApplication: Boolean
}

case object Identity extends LiteralExpression {
  def isIdentity: Boolean = true
  def isFunctionApplication: Boolean = false
}

case class FunctionApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = true
}

case class MethodApplication(name: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = false
}

case class OperatorApplication(op: String) extends LiteralExpression {
  def isIdentity: Boolean = false
  def isFunctionApplication: Boolean = false
}

object ExpressionBuilder {
  def runSingleton(es: List[LiteralExpression])(e: String): String = es match {
    case Nil => e
    case Identity :: tail => runSingleton(tail)(e)
    case FunctionApplication(name) :: tail => s"$name(${runSingleton(tail)(e)})"
    case MethodApplication(name) :: tail => s"${runSingleton(tail)(e)}.$name"
    case OperatorApplication(name) :: tail => s"${runSingleton(tail)(e)} $name"
  }

  def runCollection(es: List[LiteralExpression], isCollection: Boolean)(e: String): String = {
    val nontrivial = es.filterNot(_.isIdentity)
    val needVariable =
      nontrivial.filterNot(_.isIdentity)
        .dropRight(1).exists(_.isFunctionApplication)

    val convert = if(isCollection) {
      "(collection.breakOut)"
    } else {
      ""
    }

    if (needVariable)
      s"""$e.map(__e => ${runSingleton(nontrivial)("__e")})${convert}"""
    else if (nontrivial.nonEmpty)
      s"""$e.map(${runSingleton(nontrivial)("_")})${convert}"""
    else e
  }

  def run(es: List[LiteralExpression])(e: String, tpe: OuterType): String =
    tpe match {
      case Not =>
        runSingleton(es)(e)
      case ScalaOption =>
        runCollection(es, false)(e)
      case Collection =>
        runCollection(es, true)(e)
    }

  def run(es: Expression)(e: String, tpe: OuterType): String = es match {
    case ExpressionList(l) => run(l)(e, tpe)
    case expr: LiteralExpression => run(expr :: Nil)(e, tpe)
  }

  sealed abstract class OuterType
  case object Not extends OuterType
  case object ScalaOption extends OuterType
  case object Collection extends OuterType
}
