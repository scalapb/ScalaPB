import cats.data.NonEmptyMap
import com.google.protobuf.{ByteString, CodedOutputStream}
import com.thesamet.pb.PersonId
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.UnknownFieldSet
import scalapb.deterministic.deterministic_default.DefaultMessage
import scalapb.deterministic.deterministic.DeterministicMessage

import java.io.ByteArrayOutputStream
import scala.collection.immutable.{ListMap, SortedMap}

class DeterministicSerializationSpec extends AnyFlatSpec with Matchers {

  private def message(reverse: Boolean): DeterministicMessage = {
    def ordered[K, V](entries: (K, V)*): Map[K, V] =
      ListMap((if (reverse) entries.reverse else entries): _*)
    val unknown = UnknownFieldSet(
      ordered(
        100 -> UnknownFieldSet.Field(varint = Seq(9L, 1L), fixed32 = Seq(7, 2)),
        50  -> UnknownFieldSet.Field(lengthDelimited = Seq(ByteString.copyFromUtf8("opaque")))
      )
    )
    val adapted = SortedMap(2 -> "two", -1 -> "minus one", 0 -> "zero")(
      if (reverse) Ordering.Int.reverse else Ordering.Int
    )
    DeterministicMessage(
      strings = ordered("z" -> "z", "a" -> "a", "\uD800\uDC00" -> "unicode", "\uE000" -> "bmp"),
      signedInts = ordered(Int.MinValue -> "min", 0 -> "zero", -1 -> "negative"),
      unsignedInts = ordered(-1 -> "max", 0 -> "zero", Int.MinValue -> "high"),
      signedLongs = ordered(Long.MinValue -> "min", 0L -> "zero", -1L -> "negative"),
      unsignedLongs = ordered(-1L -> "max", 0L -> "zero", Long.MinValue -> "high"),
      bools = ordered(true -> "yes", false -> "no"),
      customKeys = ordered(PersonId("z") -> 1, PersonId("a") -> 2),
      adapted = NonEmptyMap.fromMapUnsafe(adapted),
      repeatedValues = Seq(3, 1, 2),
      fixedInts = ordered(-1 -> "max", 0 -> "zero", Int.MinValue -> "high"),
      fixedLongs = ordered(-1L -> "max", 0L -> "zero", Long.MinValue -> "high"),
      unknownFields = unknown
    )
  }

  "Deterministic serialization" should "sort all key types, mapped keys, adapters and unknown fields" in {
    val a = message(false)
    val b = message(true)
    a must be(b)
    a.toByteArray.toVector must be(b.toByteArray.toVector)
    DeterministicMessage.parseFrom(a.toByteArray) must be(a)
  }

  it should "propagate through map values, optional, repeated and oneof messages" in {
    def nested(reverse: Boolean) = {
      val leaf = message(reverse)
      leaf.copy(
        children = ListMap("nested" -> leaf),
        child = Some(leaf),
        repeatedChildren = Seq(leaf, leaf),
        choice = DeterministicMessage.Choice.ChosenChild(leaf)
      )
    }
    val a = nested(false)
    val b = nested(true)
    a must be(b)
    a.toByteArray.toVector must be(b.toByteArray.toVector)
    DeterministicMessage.parseFrom(a.toByteArray) must be(a)
  }

  it should "use numeric unsigned ordering and UTF-16 string ordering" in {
    val a        = message(false)
    val expected = a.copy(
      strings = ListMap("a" -> "a", "z" -> "z", "\uD800\uDC00" -> "unicode", "\uE000" -> "bmp"),
      signedInts = ListMap(Int.MinValue -> "min", -1 -> "negative", 0 -> "zero"),
      unsignedInts = ListMap(0 -> "zero", Int.MinValue -> "high", -1 -> "max"),
      signedLongs = ListMap(Long.MinValue -> "min", -1L -> "negative", 0L -> "zero"),
      unsignedLongs = ListMap(0L -> "zero", Long.MinValue -> "high", -1L -> "max"),
      bools = ListMap(false -> "no", true -> "yes"),
      customKeys = ListMap(PersonId("a") -> 2, PersonId("z") -> 1),
      fixedInts = ListMap(0 -> "zero", Int.MinValue -> "high", -1 -> "max"),
      fixedLongs = ListMap(0L -> "zero", Long.MinValue -> "high", -1L -> "max"),
      unknownFields = UnknownFieldSet(ListMap(a.unknownFields.asMap.toSeq.sortBy(_._1): _*))
    )
    val unsorted = DefaultMessage
      .parseFrom(a.toByteArray)
      .copy(
        strings = expected.strings,
        signedInts = expected.signedInts,
        unsignedInts = expected.unsignedInts,
        signedLongs = expected.signedLongs,
        unsignedLongs = expected.unsignedLongs,
        bools = expected.bools,
        customKeys = expected.customKeys,
        adapted = expected.adapted,
        repeatedValues = expected.repeatedValues,
        fixedInts = expected.fixedInts,
        fixedLongs = expected.fixedLongs,
        unknownFields = expected.unknownFields
      )
    a.toByteArray.toVector must be(unsorted.toByteArray.toVector)
  }

  it should "support each output API without changing repeated value order" in {
    val a        = message(true)
    val expected = a.toByteArray.toVector
    a.toByteString.toByteArray().toVector must be(expected)
    val stream = new ByteArrayOutputStream()
    a.writeTo(stream)
    stream.toByteArray.toVector must be(expected)
    val codedStream = new ByteArrayOutputStream()
    val coded       = CodedOutputStream.newInstance(codedStream)
    a.writeTo(coded)
    coded.flush()
    codedStream.toByteArray.toVector must be(expected)
    val delimited = new ByteArrayOutputStream()
    a.writeDelimitedTo(delimited)
    val input = com.google.protobuf.CodedInputStream.newInstance(delimited.toByteArray)
    input.readUInt32() must be(expected.size)
    input.readRawBytes(expected.size).toVector must be(expected)
    val parsed = DeterministicMessage.parseFrom(expected.toArray)
    parsed.repeatedValues must be(Seq(3, 1, 2))
    parsed.unknownFields.getField(100).get.varint must be(Seq(9L, 1L))
    parsed.unknownFields.getField(100).get.fixed32 must be(Seq(7, 2))
  }

  it should "leave messages generated without the option unchanged" in {
    import com.thesamet.proto.e2e.maps2.MapsTest2
    val a = MapsTest2(strToStr = ListMap("z" -> "last", "a" -> "first"))
    val b = a.copy(strToStr = ListMap("a" -> "first", "z" -> "last"))
    a must be(b)
    a.toByteArray.toVector must not be b.toByteArray.toVector
  }
  it should "respect imported message generation policies" in {
    val child = DefaultMessage.parseFrom(message(false).toByteArray)
    val a     = message(false).copy(importedChild =
      Some(child.copy(strings = ListMap("z" -> "z", "a" -> "a")))
    )
    val b = message(false).copy(importedChild =
      Some(child.copy(strings = ListMap("a" -> "a", "z" -> "z")))
    )
    a must be(b)
    a.toByteArray.toVector must not be b.toByteArray.toVector
  }

  it should "sort proto2 custom keys and support empty maps" in {
    import scalapb.deterministic.deterministic_proto2.Proto2Message
    val a = Proto2Message(customKeys = ListMap(PersonId("z") -> 1, PersonId("a") -> 2))
    val b = a.copy(customKeys = ListMap(PersonId("a") -> 2, PersonId("z") -> 1))
    a.toByteArray.toVector must be(b.toByteArray.toVector)
    Proto2Message.parseFrom(a.toByteArray) must be(a)
    Proto2Message().toByteArray.toVector must be(Vector.empty[Byte])
  }

}
