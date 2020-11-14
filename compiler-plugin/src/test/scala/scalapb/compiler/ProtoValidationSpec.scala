package scalapb.compiler

import java.io.{File, FileInputStream, PrintWriter}
import java.nio.file.Files

import scala.jdk.CollectionConverters._
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.FileDescriptor
import com.google.protobuf.ExtensionRegistry
import scalapb.options.Scalapb
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class ProtoValidationSpec extends AnyFlatSpec with Matchers {
  def generateFileSet(files: Seq[(String, String)]) = {
    val tmpDir = Files.createTempDirectory("validation").toFile
    val fileNames = files.map { case (name, content) =>
      val file = new File(tmpDir, name)
      val pw   = new PrintWriter(file)
      pw.write(content)
      pw.close()
      file.getAbsoluteFile
    }
    val outFile = new File(tmpDir, "descriptor.out")

    require(
      ProtocRunner.runProtoc(
        Version.protobufVersion,
        Seq(
          "-I",
          tmpDir.toString + ":protobuf:third_party",
          s"--descriptor_set_out=${outFile.toString}",
          "--include_imports"
        ) ++ fileNames.map(_.toString)
      ) == 0,
      "protoc exited with an error"
    )

    val fileset: Seq[FileDescriptor] = {
      val fin      = new FileInputStream(outFile)
      val registry = ExtensionRegistry.newInstance()
      Scalapb.registerAllExtensions(registry)
      val fileset =
        try {
          FileDescriptorSet.parseFrom(fin, registry)
        } finally {
          fin.close()
        }
      fileset.getFileList.asScala
        .foldLeft[Map[String, FileDescriptor]](Map.empty) { case (acc, fp) =>
          val deps = fp.getDependencyList.asScala.map(acc)
          acc + (fp.getName -> FileDescriptor.buildFrom(fp, deps.toArray))
        }
        .values
        .toVector
    }
    fileset
  }

  def runValidation(generatorParams: GeneratorParams, files: (String, String)*): Unit = {
    val fileset    = generateFileSet(files)
    val validation = new ProtoValidation(new DescriptorImplicits(generatorParams, fileset))
    validation.validateFiles(fileset)
    ()
  }

  def runValidation(files: (String, String)*): Unit = {
    runValidation(new GeneratorParams(), files: _*)
  }

  "simple message" should "validate" in {
    runValidation(
      "file.proto" ->
        """
          |syntax = "proto2";
          |message Foo { };
    """.stripMargin
    )
  }

  "UNRECOGNIZED enum value" should "fail validation" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |enum MyEnum { UNRECOGNIZED = 3; };
          """.stripMargin
      )
    }.message must include(
      "The enum value 'UNRECOGNIZED' in MyEnum is not allowed due to conflict with the catch-all Unrecognized"
    )
  }

  "oneof checker" should "fail when not top-level" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Foo { message Bar { oneof sealed_value { Foo foo = 1; } } }
          """.stripMargin
      )
    }.message must include("sealed oneofs must be top-level messages")
  }

  it should "fail when fields outside oneof" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  optional int32 extra = 2;
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must have all their fields inside a single oneof")
  }

  it should "fail when there are non-message cases" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |    int32 extra = 2;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs must have all their fields be message types")
  }

  it should "fail when oneof contains another oneof" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message Another {
            |  oneof sealed_value {
            |    MyOneof my_oneof = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not be a case within")
  }

  it should "fail when oneof case is not top-level" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Foo { message Case1 {} }
            |message MyOneof {
            |  oneof sealed_value {
            |    Foo.Case1 case1 = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneof cases must be top-level")
  }

  it should "fail when non distinct case types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |    Case1 case2 = 2;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneof cases must be of a distinct message type")
  }

  it should "fail when there are nested message types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  message Foo {}
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not contain nested messages")
  }

  it should "fail when there are nested enum types" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |  enum Foo { UNKNOWN = 0; }
            |}
          """.stripMargin
      )
    }.message must include("sealed oneofs may not contain nested enums")
  }

  it should "fail when a case is shared between two oneofs" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message AnotherOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
          """.stripMargin
      )
    }.message must include("message may belong to at most one sealed oneof")
  }

  it should "fail when a sealed oneof field is typemapped" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Case1 {}
            |message MyOneof {
            |  oneof sealed_value {
            |    Case1 case1 = 1;
            |  }
            |}
            |message Use {
            |  optional MyOneof usage = 1 [(scalapb.field).type="SomeType"];
            |}
          """.stripMargin
      )
    }.message must include("Sealed oneofs can not be type mapped. Use regular oneofs instead.")
  }

  it should "fail when sealed_oneof_extends used outside of a sealed oneof type" in {
    intercept[GeneratorException] {
      runValidation(
        "file.proto" ->
          """
            |syntax = "proto2";
            |import "scalapb/scalapb.proto";
            |message Foo {
            |  option (scalapb.message).sealed_oneof_extends = "SomeTrait";
            |}
          """.stripMargin
      )
    }.message must include(
      "is not a Sealed oneof and may not contain a sealed_oneof_extends message option. Use extends instead."
    )
  }

  // package scoped options
  it should "fail when multiple scoped option objects found for same package" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin,
        "file2.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin
      )
    }.message must be(
      "Multiple files contain package-scoped options for package 'a.b': file1.proto, file2.proto"
    )
  }

  it should "fail when package scoped option defined when no package specified" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            };""".stripMargin
      )
    }.message must be(
      "file1.proto: a package statement is required when package-scoped options are used"
    )
  }

  it should "fail when package scoped option defined an object name" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |option (scalapb.options) = {
            |  scope: PACKAGE
            |  object_name: "FOO"
            };""".stripMargin
      )
    }.message must be(
      "file1.proto: object_name is not allowed in package-scoped options."
    )
  }

  it should "fail when oneof contains a no_box message" in {
    intercept[GeneratorException] {
      runValidation(
        "file1.proto" ->
          """
            |syntax = "proto3";
            |
            |package a.b;
            |
            |import "scalapb/scalapb.proto";
            |
            |message NoBox {
            |  option (scalapb.message).no_box = true;
            |}
            |
            |message Msg {
            |  oneof foo {
            |    NoBox field = 1;
            |  }
            |}
            """.stripMargin
      )
    }.message must be(
      "a.b.Msg.field: message fields in oneofs are not allowed to have no_box set."
    )
  }
}
