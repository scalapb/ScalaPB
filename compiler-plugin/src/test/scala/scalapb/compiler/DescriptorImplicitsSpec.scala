package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class DescriptorImplicitsSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  val base = Seq(
    "disable_flat.proto" ->
      """|syntax = "proto2";
         |package disable_flat;
         |import "scalapb/scalapb.proto";
         |option (scalapb.options) = {
         |  flat_package: false;
         |  scope: PACKAGE
         |};
         |""".stripMargin,
    "enable_flat.proto" ->
      """|syntax = "proto2";
         |package enable_flat;
         |import "scalapb/scalapb.proto";
         |option (scalapb.options) = {
         |  flat_package: true;
         |  scope: PACKAGE
         |};
         |message Foo {}
         |""".stripMargin,
    "inside_disable_flat.proto" ->
      """|syntax = "proto2";
         |package disable_flat;
         |message A {};
         |""".stripMargin,
    "inside_enable_flat.proto" ->
      """|syntax = "proto2";
         |package enable_flat;
         |message B {};
         |""".stripMargin,
    "outside.proto" ->
      """|syntax = "proto2";
         |package outside;
         |message C {};
         |""".stripMargin
  )

  "flat package" should "be overridable to false when set as generator parameter" in {
    val files = generateFileSet(base)
    val implicits = new DescriptorImplicits(
      GeneratorParams(flatPackage = true),
      files,
      SecondaryOutputProvider.empty
    )
    import implicits._

    files
      .find(_.getFullName() == "inside_disable_flat.proto")
      .get
      .findMessageTypeByName("A")
      .scalaType
      .fullName must be("disable_flat.inside_disable_flat.A")
    files
      .find(_.getFullName() == "inside_enable_flat.proto")
      .get
      .findMessageTypeByName("B")
      .scalaType
      .fullName must be("enable_flat.B")
    files
      .find(_.getFullName() == "outside.proto")
      .get
      .findMessageTypeByName("C")
      .scalaType
      .fullName must be("outside.C")
  }

  "flat package" should "be overridable when not set as generator parameter" in {
    val files = generateFileSet(base)
    val implicits = new DescriptorImplicits(
      GeneratorParams(flatPackage = false),
      files,
      SecondaryOutputProvider.empty
    )
    import implicits._

    files
      .find(_.getFullName() == "inside_disable_flat.proto")
      .get
      .findMessageTypeByName("A")
      .scalaType
      .fullName must be("disable_flat.inside_disable_flat.A")
    files
      .find(_.getFullName() == "inside_enable_flat.proto")
      .get
      .findMessageTypeByName("B")
      .scalaType
      .fullName must be("enable_flat.B")
    files
      .find(_.getFullName() == "outside.proto")
      .get
      .findMessageTypeByName("C")
      .scalaType
      .fullName must be("outside.outside.C")
  }

  "disableOutput" should "be set for package option files" in {
    val files = generateFileSet(base)
    val implicits = new DescriptorImplicits(
      GeneratorParams(flatPackage = false),
      files,
      SecondaryOutputProvider.empty
    )
    import implicits._

    files
      .find(_.getFullName() == "disable_flat.proto")
      .get
      .disableOutput must be(true)

    files
      .find(_.getFullName() == "enable_flat.proto")
      .get
      .disableOutput must be(false) // has a message Foo

    files
      .find(_.getFullName() == "inside_disable_flat.proto")
      .get
      .disableOutput must be(false)

  }
}
