package scalapb.textformat

class TokenizerSpec extends munit.FunSuite {

  def tokenize(text: String): Vector[String] = {
    val b = Vector.newBuilder[String]
    val t = new Tokenizer(text)
    while (t.hasNext) b += t.next()
    b.result()
  }

  test("Tokenizer is tokenizing") {
    assertEquals(tokenize("hello world"), Vector("hello", "world"))
    assertEquals(tokenize("hello   world\n\tbar\n"), Vector("hello", "world", "bar"))
    assertEquals(tokenize("hello world foo # foo"), Vector("hello", "world", "foo"))
    assertEquals(tokenize("hello world foo\n# foo"), Vector("hello", "world", "foo"))
    assertEquals(
      tokenize("hello world { foo }\n# foo"),
      Vector("hello", "world", "{", "foo", "}")
    )
    assertEquals(
      tokenize("this is a \"quoted string\" here"),
      Vector("this", "is", "a", "\"quoted string\"", "here")
    )
    assertEquals(
      tokenize("this is a 'quoted string' here"),
      Vector("this", "is", "a", "'quoted string'", "here")
    )
    assertEquals(
      tokenize("this is a 'quoted string \\\nfoo'"),
      Vector("this", "is", "a", "'quoted string \\", "foo", "'")
    )
    assertEquals(
      tokenize("repeated_foreign_enum: [FOREIGN_FOO, FOREIGN_BAR]"),
      Vector("repeated_foreign_enum", ":", "[", "FOREIGN_FOO", ",", "FOREIGN_BAR", "]")
    )
  }
}
