package scalapb

import com.google.protobuf.{ByteString, CodedOutputStream}
import munit.FunSuite

class UnknownFieldOrderingSpec extends FunSuite {
  private def bytes(write: CodedOutputStream => Unit): ByteString = {
    val stream = new java.io.ByteArrayOutputStream()
    val output = CodedOutputStream.newInstance(stream)
    write(output)
    output.flush()
    ByteString.copyFrom(stream.toByteArray)
  }
  private def parse(
      data: ByteString,
      builder: UnknownFieldSet.Builder = new UnknownFieldSet.Builder
  ): UnknownFieldSet = {
    val input = data.newCodedInput()
    var tag   = input.readTag()
    while (tag != 0) {
      builder.parseField(tag, input)
      tag = input.readTag()
    }
    builder.result()
  }
  private val occurrences: Vector[CodedOutputStream => Unit] = Vector(
    _.writeBytes(100, ByteString.copyFromUtf8("a")),
    _.writeUInt64(100, 1L),
    _.writeFixed32(100, 2),
    _.writeFixed64(100, 3L),
    out => {
      out.writeTag(100, WireType.WIRETYPE_START_GROUP)
      out.writeInt32(1, 4)
      out.writeTag(100, WireType.WIRETYPE_END_GROUP)
    },
    _.writeBytes(100, ByteString.copyFromUtf8("b")),
    _.writeUInt64(100, 5L)
  )
  private def encode(order: Seq[Int]): ByteString =
    bytes(out => order.foreach(i => occurrences(i)(out)))
  private val mixed = encode(Vector(0, 1, 2, 3, 4, 5, 6))

  test("every wire-type permutation preserves within-field ordering and serialized size") {
    for (order <- Vector(0, 1, 2, 3, 4).permutations) {
      val data   = encode(order ++ Vector(5, 6))
      val parsed = parse(data)
      assertEquals(parsed.toByteString, data)
      assertEquals(parsed.serializedSize, data.size)
      assertEquals(parsed.withField(100, parsed.getField(100).get.copy()).toByteString, data)
      assertEquals(new UnknownFieldSet.Builder(parsed).result().toByteString, data)
    }
  }

  test("builder snapshots are immutable and merging appends new occurrences") {
    val builder = new UnknownFieldSet.Builder
    val first   = parse(mixed, builder)
    val suffix  = encode(Vector(1, 0, 4))
    val second  = parse(suffix, builder)
    assertEquals(first.toByteString, mixed)
    assertEquals(second.toByteString, mixed.concat(suffix))
    assertEquals(
      parse(suffix, new UnknownFieldSet.Builder(first)).toByteString,
      mixed.concat(suffix)
    )
    val appended = new UnknownFieldSet.Builder(first)
    appended.addLengthDelimited(100, ByteString.copyFromUtf8("c"))
    appended.addVarint(100, 7)
    assertEquals(
      appended.result().toByteString,
      mixed.concat(bytes { out =>
        out.writeBytes(100, ByteString.copyFromUtf8("c")); out.writeUInt64(100, 7)
      })
    )
  }

  test("copy and lenses preserve occurrence slots, remove trailing slots and append extra values") {
    val parsed   = parse(mixed)
    val original = parsed.getField(100).get
    val replaced = original.copy(
      varint = Vector(10, 20, 30),
      lengthDelimited = Vector(ByteString.copyFromUtf8("x"))
    )
    val expected = bytes { out =>
      out.writeBytes(100, ByteString.copyFromUtf8("x")); out.writeUInt64(100, 10)
      occurrences(2)(out); occurrences(3)(out); occurrences(4)(out)
      out.writeUInt64(100, 20); out.writeUInt64(100, 30)
    }
    assertEquals(parsed.withField(100, replaced).toByteString, expected)
    val cleared = UnknownFieldSet.Field.varintLens.set(Vector.empty)(original)
    assertEquals(parsed.withField(100, cleared).toByteString, encode(Vector(0, 2, 3, 4, 5)))
    assertEquals(parsed.toByteString, mixed)
  }

  test("constructors, extractor, equality and hash codes retain the public value contract") {
    val field     = parse(mixed).getField(100).get
    val canonical = UnknownFieldSet.Field(
      field.varint,
      field.fixed64,
      field.fixed32,
      field.lengthDelimited,
      field.group
    )
    assertEquals(field, canonical)
    assertEquals(field.hashCode, canonical.hashCode)
    assertEquals(field.productArity, 5)
    val UnknownFieldSet.Field(v, f64, f32, ld, groups) = field
    assertEquals(
      (v, f64, f32, ld, groups),
      (
        canonical.varint,
        canonical.fixed64,
        canonical.fixed32,
        canonical.lengthDelimited,
        canonical.group
      )
    )
    val legacy = new UnknownFieldSet.Field(v, f64, f32, ld)
    assertEquals(legacy, UnknownFieldSet.Field(v, f64, f32, ld))
    val copied = field.copy(v, f64, f32, ld)
    assertEquals(
      UnknownFieldSet().withField(100, copied).toByteString,
      mixed
    )
    assertEquals(copied.group, field.group)
    assertEquals(parse(encode(Vector(1, 6))).toByteString, encode(Vector(1, 6)))
  }

  test("forwarding unknown packed and unpacked values preserves the eventual repeated values") {
    val input = bytes { out =>
      out.writeBytes(100, ByteString.copyFrom(Array[Byte](1, 2)))
      out.writeInt32(100, 3)
      out.writeBytes(100, ByteString.copyFrom(Array[Byte](4, 5)))
      out.writeInt32(100, 6)
    }
    val forwarded = parse(input).toByteString.newCodedInput()
    val values    = Vector.newBuilder[Int]
    var tag       = forwarded.readTag()
    while (tag != 0) {
      if ((tag & 7) == 2) {
        val packed = forwarded.readBytes().newCodedInput()
        while (!packed.isAtEnd) values += packed.readInt32()
      } else values += forwarded.readInt32()
      tag = forwarded.readTag()
    }
    assertEquals(values.result(), Vector(1, 2, 3, 4, 5, 6))
  }
}
