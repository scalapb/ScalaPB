package scalapb

package object codec {
  /**
   * From https://developers.google.com/protocol-buffers/docs/encoding#cheat-sheet:
   * 
   * message   := (tag value)*     You can think of this as “key value”
   *
   * tag       := (field << 3) BIT_OR wire_type, encoded as varint
   * value     := (varint|zigzag) for wire_type==0 |
   *              fixed32bit      for wire_type==5 |
   *              fixed64bit      for wire_type==1 |
   *              delimited       for wire_type==2 |
   *              group_start     for wire_type==3 | This is like “open parenthesis”
   *              group_end       for wire_type==4   This is like “close parenthesis”
   *
   * varint       := int32 | int64 | uint32 | uint64 | bool | enum, encoded as varints
   * zigzag       := sint32 | sint64, encoded as zig-zag varints
   * fixed32bit   := sfixed32 | fixed32 | float, encoded as 4-byte little-endian;
   *                 memcpy of the equivalent C types (u?int32_t, float)
   * fixed64bit   := sfixed64 | fixed64 | double, encoded as 8-byte little-endian;
   *                 memcpy of the equivalent C types (u?int64_t, double)
   *
   * delimited := size (message | string | bytes | packed), size encoded as varint
   * message   := valid protobuf sub-message
   * string    := valid UTF-8 string (often simply ASCII); max 2GB of bytes
   * bytes     := any sequence of 8-bit bytes; max 2GB
   * packed    := varint* | fixed32bit* | fixed64bit*,
   *              consecutive values of the type described in the protocol definition
   *
   * varint encoding: sets MSB of 8-bit byte to indicate “no more bytes”
   * zigzag encoding: sint32 and sint64 types use zigzag encoding.
   */
}
