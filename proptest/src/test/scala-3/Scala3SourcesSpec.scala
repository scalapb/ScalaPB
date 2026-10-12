import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.compiler._

class Scala3SourcesSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  if (SchemaGenerators.isScala3Sources) {
    for (fileOption <- Seq(false, true)) {
      "Scala 3 sources" should s"compile contextual instances and lens usage (file option: $fileOption)" in {
        val files = generateFileSet(
          Seq(
            "future.proto" -> s"""
                                 |syntax = "proto3";
                                 |package futuretest;
                                 |import "scalapb/scalapb.proto";
                                 |option (scalapb.options) = { flat_package: true scala3_sources: $fileOption };
                                 |enum Kind { UNKNOWN = 0; }
                                 |message Child { int32 value = 1; }
                                 |message Msg {
                                 |  Child child = 1;
                                 |  map<string, Child> children = 2;
                                 |  Kind kind = 3;
                                 |}
                                 |message Choice { oneof sealed_value { First first = 1; Second second = 2; } }
                                 |message First {}
                                 |message Second {}
                                 |message OptionalChoice { oneof sealed_value_optional { Third third = 1; } }
                                 |message Third {}
                                 |service Test { rpc Get(Msg) returns (Msg); }
                                 |""".stripMargin
          )
        )
        val params = GeneratorParams(scala3Sources = !fileOption, grpc = true)
        @scala.annotation.nowarn("cat=deprecation")
        val implicits = new DescriptorImplicits(params, files)
        val generated = new ProtobufGenerator(params, implicits)
          .generateMultipleScalaFilesForFileDescriptor(files.find(_.getName == "future.proto").get)
        val dir = Files.createTempDirectory("scalapb-future")
        generated.foreach { source =>
          source.getContent must not include "implicit "
          val path = dir.resolve(source.getName)
          Files.createDirectories(path.getParent)
          Files.write(path, source.getContent.getBytes(UTF_8))
        }
        Files.write(
          dir.resolve("Usage.scala"),
          """package futuretest
            |import scala.language.implicitConversions
            |object Usage {
            |  val message = summon[scalapb.GeneratedMessageCompanion[Msg]]
            |  val enumCompanion = summon[scalapb.GeneratedEnumCompanion[Kind]]
            |  val reads = summon[scalapb.descriptors.Reads[Msg]]
            |  val service = summon[scalapb.grpc.ServiceCompanion[TestGrpc.Test]]
            |  val stub = summon[io.grpc.stub.AbstractStub.StubFactory[TestGrpc.TestStub]]
            |  val mapper = summon[scalapb.TypeMapper[Msg.ChildrenEntry, (String, Child)]]
            |  val choiceMapper = summon[scalapb.TypeMapper[ChoiceMessage, Choice]]
            |  val optionalMapper = summon[scalapb.TypeMapper[OptionalChoiceMessage, Option[OptionalChoice]]]
            |  val updated = Msg().update(_.child.value := 42)
            |  val nestedUpdate = Msg().update(_.child.update(_.value := 43))
            |  val customField = Msg().update(_.child.field(_.value)((c, v) => c.copy(value = v)) := 44)
            |  val namedLens = new Msg.MsgLens(scalapb.lenses.Lens.unit[Msg])
            |  val convertedLens: Msg.MsgLens[Msg] = scalapb.lenses.Lens.unit[Msg]
            |  def verify(): Unit = {
            |    assert(convertedLens.child.value.get(updated) == 42)
            |    assert(updated.getChild.value == 42)
            |    assert(nestedUpdate.getChild.value == 43)
            |    assert(customField.getChild.value == 44)
            |    assert(message eq Msg)
            |    assert(enumCompanion eq Kind)
            |    assert(service eq TestGrpc.Test)
            |    assert(stub eq TestGrpc.TestStub)
            |    assert(reads eq Msg.messageReads)
            |    assert(mapper.toCustom(mapper.toBase(("key", Child(7)))) == (("key", Child(7))))
            |    assert(choiceMapper.toCustom(choiceMapper.toBase(First())) == First())
            |    assert(optionalMapper.toCustom(optionalMapper.toBase(Some(Third()))) == Some(Third()))
            |  }
            |}
            |""".stripMargin.getBytes(UTF_8)
        )
        SchemaGenerators.compileScalaInDir(dir.toFile)
        val loader = new java.net.URLClassLoader(Array(dir.toUri.toURL), getClass.getClassLoader)
        try loader.loadClass("futuretest.Usage").getMethod("verify").invoke(null)
        finally loader.close()
      }
    }
  }
}
