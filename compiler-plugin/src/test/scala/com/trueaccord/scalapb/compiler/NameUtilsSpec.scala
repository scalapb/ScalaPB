package com.trueaccord.scalapb.compiler

import org.scalatest.{FlatSpec, MustMatchers}

class NameUtilsSpec extends FlatSpec with MustMatchers {
  "snakeCaseToCamelCase" should "work for normal names" in {
    NameUtils.snakeCaseToCamelCase("scala_pb") must be("scalaPb")
    NameUtils.snakeCaseToCamelCase("foo_bar") must be("fooBar")
    NameUtils.snakeCaseToCamelCase("foo_bar_123baz") must be("fooBar123Baz")
    NameUtils.snakeCaseToCamelCase("foo_bar_123_baz") must be("fooBar123Baz")
    NameUtils.snakeCaseToCamelCase("__foo_bar") must be("FooBar")
    NameUtils.snakeCaseToCamelCase("_foo_bar") must be("FooBar")
    NameUtils.snakeCaseToCamelCase("_scala_pb") must be("ScalaPb")
    NameUtils.snakeCaseToCamelCase("foo__bar") must be("fooBar")
    NameUtils.snakeCaseToCamelCase("123bar") must be("123Bar")
    NameUtils.snakeCaseToCamelCase("123_bar") must be("123Bar")
  }

  "snakeCaseToCamelCase" should "work when already in camel case" in {
    NameUtils.snakeCaseToCamelCase("fooBar") must be("fooBar")
    NameUtils.snakeCaseToCamelCase("fooBar_baz") must be("fooBarBaz")
    NameUtils.snakeCaseToCamelCase("FooBar") must be("fooBar")
  }

}
