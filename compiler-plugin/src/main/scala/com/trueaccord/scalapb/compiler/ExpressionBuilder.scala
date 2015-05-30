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

sealed trait LiteralExpression extends Expression

case object Identity extends LiteralExpression

case class FunctionApplication(name: String) extends LiteralExpression

case class Function2Application(name: String, arg1: String) extends LiteralExpression

case class MethodApplication(name: String) extends LiteralExpression

object ExpressionBuilder {
  def runSingleton(es: List[LiteralExpression])(e: String): String = es match {
    case Nil => e
    case Identity :: tail => runSingleton(tail)(e)
    case FunctionApplication(name) :: tail => s"$name(${runSingleton(tail)(e)})"
    case Function2Application(name, arg1) :: tail => s"$name($arg1, ${runSingleton(tail)(e)})"
    case MethodApplication(name) :: tail => s"${runSingleton(tail)(e)}.$name"
  }

  def runCollection(es: List[LiteralExpression])(e: String): String = es match {
    case Nil => e
    case Identity :: tail => runCollection(tail)(e)
    case FunctionApplication(name) :: tail => s"${runCollection(tail)(e)}.map($name)"
    case Function2Application(name, arg1) :: tail => s"${runCollection(tail)(e)}.map($name($arg1, _))"
    case MethodApplication(name) :: tail => s"${runCollection(tail)(e)}.map(_.$name)"
  }

  def run(es: List[LiteralExpression])(e: String, isCollection: Boolean): String =
    if (isCollection) runCollection(es)(e)
    else runSingleton(es)(e)

  def run(es: Expression)(e: String, isCollection: Boolean): String = es match {
    case ExpressionList(l) => run(l)(e, isCollection)
    case expr: LiteralExpression => run(expr :: Nil)(e, isCollection)
  }
}
