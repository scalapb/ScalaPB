package scalapb

import munit.FunSuite

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

class TypeMapperSpec extends FunSuite {
  test("serialize") {
    val mapper = TypeMapper[String, Int](_.toInt)(_.toString)
    val buffer = new ByteArrayOutputStream()
    val stream = new ObjectOutputStream(buffer)
    stream.writeObject(mapper)
    stream.close()
    assert(buffer.toByteArray.length > 0, "TypeMapper is not serializable")
  }
}
