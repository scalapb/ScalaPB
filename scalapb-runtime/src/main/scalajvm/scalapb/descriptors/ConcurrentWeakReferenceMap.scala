package scalapb.descriptors

import scala.collection.mutable
import scala.ref.WeakReference

/** JVM implementation of ConcurrentWeakReferenceMap. We provide a separate implementation here
  * since some of the classes used are not available in JS runtime.
  */
private[descriptors] class ConcurrentWeakReferenceMap[K, V <: AnyRef] {
  private val underlying = mutable.WeakHashMap.empty[K, WeakReference[V]]

  def getOrElseUpdate(key: K, newValue: => V): V = this.synchronized {
    val optVal = for {
      wr <- underlying.get(key)
      v  <- wr.get
    } yield v
    optVal.getOrElse {
      val r: V = newValue
      underlying.put(key, new WeakReference[V](r))
      r
    }
  }
}
