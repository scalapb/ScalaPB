package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class CoursierCompatibilitySpec extends AnyFlatSpec with Matchers {
  "Coursier" should "parse Maven settings with the resolved scala-xml dependency" in {
    // setup-java writes settings.xml in CI. Exercise this path even when a local
    // developer has no Maven settings, so an XML ABI mismatch cannot go unnoticed.
    val settings = coursier.core.compatibility
      .xmlParseDom("<settings><mirrors/></settings>")
      .fold(error => fail(error), identity)
    settings.children.size mustBe 1
  }
}
