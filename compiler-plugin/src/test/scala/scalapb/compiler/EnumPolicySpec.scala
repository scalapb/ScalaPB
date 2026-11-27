package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

class EnumPolicySpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  "Enum policy" should "always honor Editions and restrict the compatibility option to proto2" in {
    for (
      syntax <- Seq("proto2", "proto3", "editions"); option <- Seq(None, Some(false), Some(true))
    ) {
      val declaration =
        if (syntax == "editions") "edition = \"2023\";" else s"syntax = \"$syntax\";"
      val setting =
        option.map(v => s"option (scalapb.options).proto2_closed_enums = $v;").getOrElse("")
      val closed = if (syntax == "editions") "option features.enum_type = CLOSED;" else ""
      val files  =
        generateFileSet(Seq("enums.proto" -> s"""
                                                |$declaration
                                                |import "scalapb/scalapb.proto";
                                                |$setting
                                                |$closed
                                                |enum Value { ZERO = 0; }
                                                |message Message { ${if (syntax == "editions") ""
                                               else "optional"} Value value = 1; }
                                                |""".stripMargin))
      val params    = GeneratorParams(javaConversions = true)
      val implicits = new DescriptorImplicits(params, files, SecondaryOutputProvider.empty)
      val code      = new ProtobufGenerator(params, implicits)
        .generateMultipleScalaFilesForFileDescriptor(files.find(_.getName == "enums.proto").get)
        .map(_.getContent)
        .mkString("\n")
      val enforced = syntax == "editions" || (syntax == "proto2" && option.contains(true))
      withClue(s"syntax=$syntax option=$option: ") {
        code.contains("def _readEnum__") mustBe enforced
        code.contains("parseFrom(scalaPbSource.toByteArray)") mustBe enforced
      }
    }
  }
}
