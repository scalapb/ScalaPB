package scalapb.descriptors

import scala.collection.mutable
import scala.ref.WeakReference

/** JVM implementation of ConcurrentWeakReferenceMap. We provide a separate implementation here
  * since some of the classes used are not available in JS runtime.
  */
private[descriptors] class ConcurrentWeakReferenceMap[K, V <: AnyRef] {
  private val underlying = mutable.WeakHashMap.empty[K, WeakReference[V]]

  def getOrElseUpdate(key: K, newValue: => V): V = this.synchronized {
    underlying.get(key) match {
      case Some(WeakReference(v)) => v
      case None =>
        val r: V = newValue
        underlying.put(key, new WeakReference[V](r))
        r
    }
  }
}
