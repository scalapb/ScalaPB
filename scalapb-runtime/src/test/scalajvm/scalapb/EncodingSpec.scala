package scalapb

import org.apache.commons.codec.binary.Base64
import munit.ScalaCheckSuite
import org.scalacheck.Prop._

class EncodingSpec extends ScalaCheckSuite {
  property("fromBase64 is the inverse of toBase64") {
    forAll { (b: Array[Byte]) =>
      assertEquals(Encoding.fromBase64(Encoding.toBase64(b)).toSeq, b.toSeq)
    }
  }

  property("fromBase64 is compatible with javax.printBase64") {
    forAll { (b: Array[Byte]) =>
      assertEquals(Encoding.fromBase64(Base64.encodeBase64String(b)).toSeq, b.toSeq)
    }
  }

  property("toBase64 is compatible with javax.parseBase64") {
    forAll { (b: Array[Byte]) =>
      assertEquals(Base64.decodeBase64(Encoding.toBase64(b)).toSeq, b.toSeq)
    }
  }
}
