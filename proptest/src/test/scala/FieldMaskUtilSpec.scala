package scalapb

import com.google.common.base.CaseFormat
import org.scalacheck.Gen
import org.scalatest.PropSpec
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class FieldMaskUtilSpec extends PropSpec with GeneratorDrivenPropertyChecks {

  // protobuf-java use guava
  // https://github.com/google/protobuf/blob/v3.6.0/java/util/src/main/java/com/google/protobuf/util/FieldMaskUtil.java#L175
  // https://github.com/google/protobuf/blob/v3.6.0/java/util/src/main/java/com/google/protobuf/util/FieldMaskUtil.java#L159

  // protobuf-java convert only ASCII characters
  // e.g. `'\u00E0'.toUpper == '\u00C0'` but protobuf-java does not convert these characters
  //
  // https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isLowerCase-char-
  // https://docs.oracle.com/javase/8/docs/api/java/lang/Character.html#isUpperCase-char-
  private[this] val strGen: Gen[String] = Gen
    .listOf(
      Gen.oneOf(
        Gen.asciiChar,
        Gen.oneOf(
          '\u00E0', '\u00E1', '\u00E2', '\u00E3', '\u00E4', '\u00E5', '\u00E6'
        ),
        Gen.oneOf(
          '\u00C0', '\u00C1', '\u00C2', '\u00C3', '\u00C4', '\u00C5', '\u00C6'
        )
      )
    )
    .map(_.mkString)

  property("lowerSnakeCaseToCamelCase") {
    forAll(strGen) { str =>
      val useScala = FieldMaskUtil.lowerSnakeCaseToCamelCase(str)
      val useJava  = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str)
      assert(useScala == useJava)
    }
  }

  property("camelCaseToSnakeCase") {
    forAll(strGen) { str =>
      val useScala = FieldMaskUtil.camelCaseToSnakeCase(str)
      val useJava  = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str)
      assert(useScala == useJava)
    }
  }
}
