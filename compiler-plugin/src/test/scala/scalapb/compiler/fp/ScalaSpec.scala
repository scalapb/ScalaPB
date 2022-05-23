package scalapb.compiler.fp

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import Scala._
import scalapb.compiler.FunctionalPrinter

class ScalaSpec extends AnyFlatSpec with Matchers {
  "caseClass" should "gen right code" in {
    val res = caseClass(
      TypeDef("Foo", Seq(TypeArg("Bar"))),
      Seq(Arg("foo", Type("Bar", noRoot = true)), Arg("baz", Type("scala.Int"))),
      CtorCall(Type("java.lang.RuntimeException"), Seq(Seq("\"\""))),
      Seq(Type("Bax", Seq(Type("Bar", noRoot = true))))
    )(identity)(new FunctionalPrinter).result()
    val orig = s"""case class Foo[Bar](foo: Bar, baz: _root_.scala.Int) extends _root_.java.lang.RuntimeException("") with _root_.Bax[Bar] {
}"""
    res must equal(orig)
  }

  "caseClass" should "gen right code without extends" in {
    val res = caseClass(
      TypeDef("Foo", Seq(TypeArg("Bar", Type("String")))),
      Seq(Arg("foo", Type("Bar", noRoot = true)), Arg("baz", Type("scala.Int")))
    )(identity)(new FunctionalPrinter).result()
    val orig = s"""case class Foo[Bar <: _root_.String](foo: Bar, baz: _root_.scala.Int) {
}"""
    res must equal(orig)
  }

  "companionObject" should "gen right code" in {
    val res = companionObject(
      TypeDef("Foo"),
      CtorCall(Type("java.lang.RuntimeException"), Seq(Seq("\"\""))),
      Seq(Type("Bax"))
    )(identity)(new FunctionalPrinter).result()
    val orig = s"""case object Foo extends _root_.java.lang.RuntimeException("") with _root_.Bax {
}"""
    res must equal(orig)
  }
}
