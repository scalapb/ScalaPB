package scalapb.compiler

import java.io.{File, FileInputStream, PrintWriter}
import java.nio.file.Files

import scala.collection.JavaConverters._
import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.FileDescriptor
import org.scalatest.{FlatSpec, MustMatchers}

class ProtoValidationSpec extends FlatSpec with MustMatchers {
  def generateFileSet(files: Seq[(String, String)]) = {
    val tmpDir = Files.createTempDirectory("validation").toFile
    val fileNames = files.map {
      case (name, content) =>
        val file = new File(tmpDir, name)
        val pw   = new PrintWriter(file)
        pw.write(content)
        pw.close()
        file.getAbsoluteFile
    }
    val outFile = new File(tmpDir, "descriptor.out")

    require(
      com.github.os72.protocjar.Protoc.runProtoc(
        Array(
          "-I",
          tmpDir.toString,
          s"--descriptor_set_out=${outFile.toString}",
          "--include_imports"
        ) ++ fileNames.map(_.toString)
      ) == 0,
      "protoc exited with an error"
    )

    val fileset: Seq[FileDescriptor] = {
      val fin = new FileInputStream(outFile)
      val fileset = try {
        FileDescriptorSet.parseFrom(fin)
      } finally {
        fin.close()
      }
      fileset.getFileList.asScala
        .foldLeft[Map[String, FileDescriptor]](Map.empty) {
          case (acc, fp) =>
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
    fileset.foreach(validation.validateFile)
  }

  def runValidation(files: (String, String)*): Unit = {
    runValidation(new GeneratorParams(), files: _*)
  }

  "simple message" should "validate" in {
    val r = runValidation(
      "file.proto" ->
        """
          |syntax = "proto2";
          |message Foo { };
    """.stripMargin
    )
  }

  "No package name" should "fail validation when flat_package is true" in {
    intercept[GeneratorException] {
      runValidation(
         new GeneratorParams(flatPackage = true),
        "file.proto" ->
          """
            |syntax = "proto2";
          """.stripMargin
      )
    }.message must include(
      "a package name is required"
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
}
