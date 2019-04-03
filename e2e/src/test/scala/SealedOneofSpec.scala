
import com.thesamet.proto.e2e.sealed_oneof._
import com.thesamet.proto.e2e.sealed_oneof_in_oneof._
import com.thesamet.proto.e2e.{sealed_oneof_single_file => f}
import com.thesamet.proto.e2e.sealed_oneof_extends._
import org.scalatest._

class SealedOneofSpec extends FlatSpec with MustMatchers {

  val expr = Add(Lit(1), Add(Lit(2), Lit(3)))

  "Expr.toExprMessage.toExpr" should "roundtrip" in {
    assert(expr == expr.asMessage.toExpr)
  }

  "ExprMessage.toByteArray" should "work via Expr" in {
    val expr2  = ExprMessage.parseFrom(expr.asMessage.toByteArray).toExpr
    assert(expr == expr2)
  }

  "fields of sealed_oneof type" should "default Empty" in {
    assert(Add() == Add(Expr.Empty, Expr.Empty))
  }

  "fields of repeated sealed_oneof type" should "work like normal" in {
    val programs = Programs(programs = List(expr, expr), optionalExpr = expr, exprMap = Map("44" -> expr))
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
      fexpr.asMessage.toProtoString ==
      expr.asMessage.toProtoString
    )
  }

  "sealed_oneof message" should "work as a oneof case of another message" in {
    // Exhaustive matching of all possible cases
    assertCompiles(
      """
        |Animal.defaultInstance.value match {
        |  case Animal.Value.Empty =>
        |  case Animal.Value.Mammal(v) =>
        |    v match {
        |      case Mammal.Empty =>
        |      case Dog() =>
        |      case Cat() =>
        |    }
        |  case Animal.Value.Bird(v) =>
        |    v match {
        |      case Bird.Empty =>
        |      case Eagle() =>
        |      case Sparrow() =>
        |    }
        |}
      """.stripMargin)
  }

  "messages using sealed_oneof_extends" should "all extend from custom user trait" in {
    PlayerShim.defaultInstance mustBe a [PlayerBaseTrait]
    BasketBallPlayer() mustBe a [PlayerBaseTrait]
    SoccerPlayer() mustBe a [PlayerBaseTrait]
  }
}

