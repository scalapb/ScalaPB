package scalapb.derives

import scalapb.{GeneratedMessage, GeneratedSealedOneof}
import scala.compiletime.erasedValue
import scala.annotation.targetName

trait Show[T]:
  def show(t: T): String

object Show:
  @targetName("derivedMessage") def derived[T <: GeneratedMessage]: Show[T] = new Show[T]:
    def show(t: T): String = t.toProtoString

  @targetName("derivedSealedOneof") def derived[T <: GeneratedSealedOneof]: Show[T] = new Show[T]:
    def show(t: T): String = "Sealed!"

trait TC[T]:
  def tc(t: T): Unit

object TC:
  def derived[T <: GeneratedMessage]: TC[T] = null
