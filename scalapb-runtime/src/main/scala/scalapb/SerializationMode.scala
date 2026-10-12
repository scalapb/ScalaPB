package scalapb

/** Controls ScalaPB serialization, independently of the Java output stream's settings.
  * Deterministic serialization is not canonical across languages, schemas or library versions.
  */
sealed trait SerializationMode

object SerializationMode {

  /** Preserves collection iteration order (the historical behavior). */
  case object Default extends SerializationMode

  /** Sorts maps by their protobuf keys and unknown fields by field number, recursively. Repeated
    * values retain their order. Requires regenerated messages throughout the graph.
    */
  case object Deterministic extends SerializationMode
}
