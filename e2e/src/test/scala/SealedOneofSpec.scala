import com.thesamet.proto.e2e.sealed_oneof._
import com.thesamet.proto.e2e.sealed_oneof_in_oneof._
import com.thesamet.proto.e2e.{sealed_oneof_single_file => f}
import com.thesamet.proto.e2e.{sealed_oneof_nested_single_file => f2}
import com.thesamet.proto.e2e.sealed_oneof_extends._
import com.thesamet.proto.e2e.sealed_oneof_extends_nested.{NestedPlayerBaseTrait, Parent}
import com.thesamet.proto.e2e.sealed_oneof_in_oneof_nested.Zoo
import com.thesamet.proto.e2e.sealed_oneof_nested.Nested
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.TestUtil.isAnyVal

class SealedOneofSpec extends AnyFlatSpec with Matchers {

  val expr       = Add(Lit(1), Add(Lit(2), Lit(3)))
  val nestedExpr = Nested.Add(Nested.Lit(1), Nested.Add(Nested.Lit(2), Nested.Lit(3)))

  "Expr.toExprMessage.toExpr" should "roundtrip" in {
    assert(expr == expr.asMessage.toExpr)
  }

  "Nested.Expr.toExprMessage.toExpr" should "roundtrip" in {
    assert(nestedExpr == nestedExpr.asMessage.toExpr)
  }

  "ExprMessage.toByteArray" should "work via Expr" in {
    val expr2 = ExprMessage.parseFrom(expr.asMessage.toByteArray).toExpr
    assert(expr == expr2)
  }

  "Nested.ExprMessage.toByteArray" should "work via Expr" in {
    val expr2 = Nested.ExprMessage.parseFrom(nestedExpr.asMessage.toByteArray).toExpr
    assert(nestedExpr == expr2)
  }

  "fields of sealed_oneof type" should "default Empty" in {
    assert(Add() == Add(Expr.Empty, Expr.Empty))
  }

  "fields of nested sealed_oneof type" should "default Empty" in {
    assert(Nested.Add() == Nested.Add(Nested.Expr.Empty, Nested.Expr.Empty))
  }

  "fields of repeated sealed_oneof type" should "work like normal" in {
    val programs =
      Programs(programs = List(expr, expr), optionalExpr = expr, exprMap = Map("44" -> expr))
    val programs2 = Programs.parseFrom(programs.toByteArray)
    assert(programs == programs2)
  }

  "fields of repeated nested sealed_oneof type" should "work like normal" in {
    val programs =
      Nested.Programs(
        programs = List(nestedExpr, nestedExpr),
        optionalExpr = nestedExpr,
        exprMap = Map("44" -> nestedExpr)
      )
    val programs2 = Nested.Programs.parseFrom(programs.toByteArray)
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
      fexpr.asMessage.toProtoString ==
        expr.asMessage.toProtoString
    )
  }

  "single_file=true" should "work with nested sealed_oneof" in {
    val fexpr = f2.Nested.Add(f2.Nested.Lit(1), f2.Nested.Add(f2.Nested.Lit(2), f2.Nested.Lit(3)))
    assert(
      fexpr.asMessage.toProtoString ==
        expr.asMessage.toProtoString
    )
  }

  "sealed_oneof message" should "work as a oneof case of another message" in {
    // Exhaustive matching of all possible cases
    assertCompiles("""
                     |Animal.defaultInstance.value match {
                     |  case Animal.Value.Empty =>
                     |  case Animal.Value.Mammal(v) =>
                     |    v match {
                     |      case Mammal.Empty =>
                     |      case Dog(_) =>
                     |      case Cat(_) =>
                     |    }
                     |  case Animal.Value.Bird(v) =>
                     |    v match {
                     |      case Bird.Empty =>
                     |      case Eagle(_) =>
                     |      case Sparrow(_) =>
                     |    }
                     |}
      """.stripMargin)
  }

  "nested sealed_oneof message" should "work as a oneof case of another message" in {
    // Exhaustive matching of all possible cases
    assertCompiles("""
                     |Zoo.Animal.defaultInstance.value match {
                     |  case Zoo.Animal.Value.Empty =>
                     |  case Zoo.Animal.Value.Mammal(v) =>
                     |    v match {
                     |      case Zoo.Mammal.Empty =>
                     |      case Zoo.Dog(_) =>
                     |      case Zoo.Cat(_) =>
                     |    }
                     |  case Zoo.Animal.Value.Bird(v) =>
                     |    v match {
                     |      case Zoo.Bird.Empty =>
                     |      case Zoo.Eagle(_) =>
                     |      case Zoo.Sparrow(_) =>
                     |    }
                     |}
      """.stripMargin)
  }

  "messages using sealed_oneof_extends" should "all extend from custom user trait" in {
    PlayerShim.defaultInstance mustBe a[PlayerBaseTrait]
    BasketBallPlayer() mustBe a[PlayerBaseTrait]
    SoccerPlayer() mustBe a[PlayerBaseTrait]
    SoccerPlayer() mustBe a[scalapb.GeneratedSealedOneof]
  }

  "messages extends AnyVal using sealed_oneof_extends Any" should "all extend from custom user trait" in {
    universal.PlayerShim.defaultInstance mustBe a[universal.PlayerBaseTrait]
    universal.BasketBallPlayer() mustBe an[universal.PlayerBaseTrait]
    universal.BasketBallPlayer() mustBe a[scalapb.GeneratedSealedOneof]
    isAnyVal(universal.BasketBallPlayer())
    universal.SoccerPlayer() mustBe an[universal.PlayerBaseTrait]
    universal.SoccerPlayer() mustBe a[scalapb.GeneratedSealedOneof]
    isAnyVal(universal.SoccerPlayer())
  }

  "nested messages using sealed_oneof_extends" should "all extend from custom user trait" in {
    Parent.PlayerShim.defaultInstance mustBe a[NestedPlayerBaseTrait]
    Parent.BasketBallPlayer() mustBe a[NestedPlayerBaseTrait]
    Parent.SoccerPlayer() mustBe a[NestedPlayerBaseTrait]
    Parent.SoccerPlayer() mustBe a[scalapb.GeneratedSealedOneof]
  }

  "asNonEmpty" should "return Some or None" in {
    expr.asNonEmpty must be(Some(expr))
    expr.lhs.asNonEmpty must be(Some(expr.lhs))
    Expr.Empty.asNonEmpty must be(None)
    expr.asNonEmpty match {
      case Some(Add(_, _, _)) => "add"
      case Some(Lit(_, _))    => "add"
      case None               => ""
    }
  }

  "or-empty sealed oneofs" should "Work" in {
    import com.thesamet.proto.e2e.optional.{sealed_oneof_optional => OO}
    OO.Programs(optionalExpr = None)
    OO.Programs(optionalExpr = Some(OO.Lit(32)))
    OO.Programs(programs = Seq(Some(OO.Lit(32))))
  }
}
