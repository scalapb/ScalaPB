package com.trueaccord.scalapb.compiler

sealed trait Expression extends Product with Serializable {
  def andThen(other: Expression) = (this, other) match {
    case (Identity, e2: LiteralExpression) => e2
    case (e1, Identity) => e1
    case (ExpressionList(l1), ExpressionList(l2)) => ExpressionList(l2 ++ l1)
    case (ExpressionList(l1), e: LiteralExpression) => ExpressionList(e :: l1)
    case (e: LiteralExpression, ExpressionList(l2)) => ExpressionList(l2 :+ e)
    case (e1: LiteralExpression, e2: LiteralExpression) => ExpressionList(e2 :: e1 :: Nil)
  }

  def apply(e: String, isCollection: Boolean): String = ExpressionBuilder.run(this)(e, isCollection)
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

  def runCollection(es: List[LiteralExpression])(e: String): String = {
    val nontrivial = es.filterNot(_.isIdentity)
    val needVariable =
      nontrivial.filterNot(_.isIdentity)
        .dropRight(1).exists(_.isFunctionApplication)

    if (needVariable)
      s"""$e.map(__e => ${runSingleton(nontrivial)("__e")})"""
    else if (nontrivial.nonEmpty)
      s"""$e.map(${runSingleton(nontrivial)("_")})"""
    else e
  }

  def run(es: List[LiteralExpression])(e: String, isCollection: Boolean): String =
    if (isCollection) runCollection(es)(e)
    else runSingleton(es)(e)

  def run(es: Expression)(e: String, isCollection: Boolean): String = es match {
    case ExpressionList(l) => run(l)(e, isCollection)
    case expr: LiteralExpression => run(expr :: Nil)(e, isCollection)
  }
}
