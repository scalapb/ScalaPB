import scala.util.Try

object ScalaVersion {
  def isDotty: Boolean = Try(getClass().getClassLoader().loadClass("dotty.DottyPredef")).isSuccess
}
