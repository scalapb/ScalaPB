package scalapb.descriptors

/** Javascript (and hence Scala.js) does not have WeakReferences and WeakHashMap, so we use a normal
  * HashMap.
  */
private[descriptors] class ConcurrentWeakReferenceMap[K, V] {
  private val underlying: collection.mutable.Map[K, V] = new collection.mutable.HashMap[K, V]()

  def getOrElseUpdate(key: K, newValue: => V): V = this.synchronized {
    underlying.getOrElseUpdate(key, newValue)
  }
}
