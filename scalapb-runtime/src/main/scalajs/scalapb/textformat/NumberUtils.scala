package scalapb.textformat

private[scalapb] object NumberUtils {
  def doubleToString(v: Double): String = {
    import scalajs.js.JSNumberOps._
    val t = if (v.abs >= 1e7) { v.toExponential() }
    else if (v.isWhole) (v.toString + ".0")
    else v.toString
    t.replace("e-", "E-")
      .replace("e+", "E")
  }

  def floatToString(v: Float): String = {
    doubleToString(v.toDouble)
  }
}
