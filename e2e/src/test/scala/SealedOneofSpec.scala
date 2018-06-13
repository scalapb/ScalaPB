
import com.trueaccord.proto.e2e.sealed_oneof._
import com.trueaccord.proto.e2e.{sealed_oneof_single_file => f}

import org.scalatest._

class SealedOneofSpec extends FlatSpec with MustMatchers {

  val expr = Add(Lit(1), Add(Lit(2), Lit(3)))

  "Expr.toExprMessage.toExpr" should "roundtrip" in {
    assert(expr == expr.toExprMessage.toExpr)
  }

  "ExprMessage.toByteArray" should "work via Expr" in {
    val expr2  = ExprMessage.parseFrom(expr.toExprMessage.toByteArray).toExpr
    assert(expr == expr2)
  }

  "fields of sealed_oneof type" should "default Empty" in {
    assert(Add() == Add(Expr.Empty, Expr.Empty))
  }

  "fields of repeated sealed_oneof type" should "work like normal" in {
    val programs = Programs(List(expr, expr))
    val programs2 = Programs.parseFrom(programs.toByteArray)
    assert(programs == programs2)
  }

  trait UnsealedExpr

  "Expr" should "be sealed" in {
    assertCompiles("class Foo extends UnsealedExpr")
    assertTypeError("class Foo extends Expr")
  }

  "single_file=true" should "work with sealed_oneof" in {
    val fexpr = f.Add(f.Lit(1), f.Add(f.Lit(2), f.Lit(3)))
    assert(
      fexpr.toExprMessage.toProtoString ==
      expr.toExprMessage.toProtoString
    )
  }

  // Negative tests below: assert that the following classes are not sealed oneofs by
  // checking that they don't have the "Message" suffix.

  "sealed oneof children" should "only have a single parent" in {
    DuplicateSealedOneof().withLit(Lit(3))
  }

  "oneof name" should "be 'sealed_value'" in {
    BadNameSealedOneof().withAdd(Add(Lit(1), Expr.Empty))
  }

  "primitives types" should "not trigger sealed oneof" in {
    PrimitiveSealedOneof().withPrimitive("String")
  }

  "imported types" should "not trigger sealed oneof" in {
    ImportedSealedOneof().withAdd(Add(Lit(1)))
  }

  "sealed oneof types" should "be distinct" in {
    NonDistinctSealedOneof()
      .withAdd1(Add(Lit(1)))
      .withAdd2(Add(Lit(2)))
  }

}

