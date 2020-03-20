package scalapb.internal

object Hashing {
  def hashLong(v: Long): Int = (v ^ (v >>> 32)).toInt

  def hashDouble(v: Double): Int = hashLong(java.lang.Double.doubleToLongBits(v))

  def hashFloat(v: Float): Int = java.lang.Float.floatToIntBits(v)

  def hashBoolean(v: Boolean): Int = if (v) 1231 else 1237
}
