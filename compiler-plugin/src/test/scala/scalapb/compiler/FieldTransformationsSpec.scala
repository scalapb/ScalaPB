package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.jdk.CollectionConverters._
import com.google.protobuf.DynamicMessage
import com.google.protobuf.TextFormat
import com.google.protobuf.DescriptorProtos.{FieldDescriptorProto, FieldOptions}
import com.google.protobuf.UnknownFieldSet
import com.google.protobuf.UnknownFieldSet.Field
import scalapb.options.Scalapb
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.options.Scalapb.FieldTransformation
import scalapb.options.Scalapb.PreprocessorOutput
import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors.FileDescriptor

class FieldTransformationsSpec extends AnyFlatSpec with Matchers with ProtocInvocationHelper {
  val base = Seq(
    "myvalidate.proto" ->
      """|syntax = "proto2";
         |package opts;
         |import "google/protobuf/descriptor.proto";
         |message FieldRules {
         |  optional Int32Rules int32 = 1;
         |  optional StringRules string = 2;
         |  optional RepeatedRules repeated = 3;
         |}
         |message RepeatedRules {
         |  optional FieldRules items = 1;
         |}
         |message Int32Rules {
         |  optional int32 gt = 1;
         |  optional int32 gte = 2;
         |  optional int32 lt = 3;
         |}
         |message StringRules {
         |  optional string const = 1;
         |  repeated string in = 2;
         |}
         |extend google.protobuf.FieldOptions {
         |  optional FieldRules rules = 50001;
         |};
        """.stripMargin,
    "locals.proto" ->
      """|syntax = "proto3";
         |package local;
         |import "myvalidate.proto";
         |import "scalapb/scalapb.proto";
         |option (scalapb.options) = {
         |  field_transformations: [
         |    {
         |      when {
         |        options {
         |          [opts.rules] {
         |            int32: {gt: 17}
         |          }
         |        }
         |      }
         |      set {
         |         [scalapb.field] {
         |           type: "MatchGt17"
         |         }
         |      }
         |    }
         |  ]
         |};
         |message X {
         |  int32 x = 1 [(opts.rules).int32 = {gt: 17}];
         |  int32 y = 2 [(opts.rules).string = {const: "foo"}];
         |}
         |""".stripMargin,
    "package.proto" ->
      """|syntax = "proto3";
         |package pkg;
         |import "myvalidate.proto";
         |import "scalapb/scalapb.proto";
         |option (scalapb.options) = {
         |  scope: PACKAGE
         |  field_transformations: [
         |  {
         |    when {
         |      type: TYPE_INT32
         |      options {
         |        [opts.rules] {
         |          int32: {gt: 12}
         |        }
         |      }
         |    }
         |    set {
         |      [scalapb.field] {
         |        type: "MatchGt12"
         |      }
         |    }
         |  },
         |  {
         |    when {
         |      options {
         |        [opts.rules] {
         |          int32: {lt: 1}
         |        }
         |      }
         |    }
         |    match_type: PRESENCE
         |    set {
         |      [scalapb.field] {
         |        type: "MatchLt[\"$(type)\",$(options.[opts.rules].int32.lt)]"
         |      }
         |    }
         |  }
         |  ]
         |};
         |""".stripMargin,
    "inherits.proto" ->
      """|syntax = "proto3";
         |package pkg;
         |import "myvalidate.proto";
         |message X {
         |  int32 x = 1 [(opts.rules).int32 = {gt: 12}];
         |  int64 x_no = 2 [(opts.rules).int32 = {gt: 12}];  // should not be impacted since matching for only int32
         |  int32 y = 3 [(opts.rules).int32 = {lt: 317}];
         |  string z = 4 [(opts.rules).string = {const: "foo"}];
         |}
         |""".stripMargin,
    "has_scalapb.proto" ->
      """|syntax = "proto3";
         |package pkg;
         |import "scalapb/scalapb.proto";
         |option (scalapb.options).flat_package = false; // avoid unused warning
      """.stripMargin,
    "ignores.proto" ->
      """|syntax = "proto3";
         |package pkg;
         |import "myvalidate.proto";
         |import "scalapb/scalapb.proto";
         |option (scalapb.options) = {
         |  ignore_all_transformations: true
         |};
         |message I {
         |  int32 x = 1 [(opts.rules).int32 = {gt: 12}];
         |}
         |""".stripMargin
  )
  val files = generateFileSet(base)

  def preproc = {
    val when =
      FieldDescriptorProto
        .newBuilder()
        .setOptions(
          FieldOptions
            .newBuilder()
            .setUnknownFields(
              UnknownFieldSet
                .newBuilder()
                .addField(
                  50001,
                  Field
                    .newBuilder()
                    .addLengthDelimited(
                      ByteString.copyFrom(Array[Byte](10, 2, 8, 0)) // {int32: gt{0}}
                    )
                    .build()
                )
                .build()
            )
            .build()
        )
        .build()
    SecondaryOutputProvider.fromMap(
      Map(
        "myproc" ->
          PreprocessorOutput.newBuilder
            .putOptionsByFile(
              "injected_options.proto",
              ScalaPbOptions.newBuilder
                .addFieldTransformations(
                  FieldTransformation.newBuilder
                    .setWhen(when)
                    .setSet(
                      FieldOptions.newBuilder
                        .setExtension(
                          Scalapb.field,
                          scalapb.options.Scalapb.FieldOptions
                            .newBuilder()
                            .setType("PositiveInt")
                            .build()
                        )
                    )
                )
                .build()
            )
            .build()
      )
    )
  }

  def options(cache: Map[FileDescriptor, ScalaPbOptions], name: String): ScalaPbOptions =
    cache.find(_._1.getFullName == name).get._2

  val cache      = FileOptionsCache.buildCache(files, SecondaryOutputProvider.empty)
  val locals     = files.find(_.getFullName() == "locals.proto").get
  val inherits   = files.find(_.getFullName() == "inherits.proto").get
  val hasScalapb = files.find(_.getFullName() == "has_scalapb.proto").get
  val ignores    = files.find(_.getFullName() == "ignores.proto").get
  val context    =
    ExtensionResolutionContext("-", FieldTransformations.fieldExtensionsForFile(inherits))
  val contextWithScalaPB =
    ExtensionResolutionContext("-", FieldTransformations.fieldExtensionsForFile(hasScalapb))

  def fieldRules(
      rules: String,
      scalapbFieldOptions: Option[String] = None
  ): FieldDescriptorProto = {
    val fieldRulesDesc = files
      .flatMap { f => f.getMessageTypes().asScala }
      .find(_.getFullName == "opts.FieldRules")
      .get
    val dm = DynamicMessage.newBuilder(fieldRulesDesc)
    TextFormat.merge(rules, dm)
    dm.build()
    val unknownOptions =
      UnknownFieldSet
        .newBuilder()
        .addField(
          50001,
          Field
            .newBuilder()
            .addLengthDelimited(
              dm.build().toByteString()
            )
            .build()
        )
    scalapbFieldOptions.foreach(text =>
      unknownOptions.addField(
        Scalapb.field.getNumber(),
        Field
          .newBuilder()
          .addLengthDelimited(
            TextFormat.parse(text, classOf[scalapb.options.Scalapb.FieldOptions]).toByteString()
          )
          .build()
      )
    )
    val fieldOptions =
      FieldOptions
        .newBuilder()
        .setUnknownFields(
          unknownOptions
            .build()
        )

    val builder = FieldDescriptorProto.newBuilder
      .setOptions(
        fieldOptions.build()
      )
    builder.build()
  }

  def matchPresence(msg: String, pattern: String): Boolean = {
    FieldTransformations.matchPresence(
      fieldRules(msg),
      FieldTransformations.fieldMap(fieldRules(pattern), context),
      context
    )
  }

  def matchContains(msg: String, pattern: String): Boolean = {
    FieldTransformations.matchContains(
      fieldRules(msg),
      FieldTransformations.fieldMap(fieldRules(pattern), context),
      context
    )
  }

  "splitPath" should "split path correctly" in {
    intercept[GeneratorException] {
      FieldTransformations.splitPath("")
    }.getMessage() must startWith("Got empty path component")
    FieldTransformations.splitPath("foo.bar.baz") must be(List("foo", "bar", "baz"))
    FieldTransformations.splitPath("foo") must be(List("foo"))
    intercept[GeneratorException] {
      FieldTransformations.splitPath("foo.")
    }.getMessage() must startWith("Got empty path component")
    FieldTransformations.splitPath("[foo].bar.baz") must be(List("[foo]", "bar", "baz"))
    FieldTransformations.splitPath("[foo].x") must be(List("[foo]", "x"))
    intercept[GeneratorException] {
      FieldTransformations.splitPath("[foo]")
    }.getMessage() must startWith("Extension can not be the last")
    intercept[GeneratorException] {
      FieldTransformations.splitPath("[foo].")
    }.getMessage() must startWith("Got empty path component")
  }

  "field transformations" should "work when local, inherited and ignored" in {
    cache(locals)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "local.X.x")
      .get
      .getOptions()
      .getType() must be("MatchGt17")
    cache(locals)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "local.X.y") must be(None)
    cache(inherits)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.X.x")
      .get
      .getOptions()
      .getType() must be("MatchGt12")
    cache(inherits)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.X.x_no") must be(None)
    cache(inherits)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.X.z") must be(None)
    cache(inherits)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.X.y")
      .get
      .getOptions()
      .getType() must be("MatchLt[\"TYPE_INT32\",317]")
    cache(ignores)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.I.x") must be(None)
  }

  it should "throw exception when setting no ScalaPB options" in {
    val settingOthers =
      Map(
        "e1.proto" ->
          """|syntax = "proto3";
             |package local;
             |import "myvalidate.proto";
             |import "scalapb/scalapb.proto";
             |option (scalapb.options) = {
             |  field_transformations: [
             |    {
             |      when {
             |        options {
             |          [opts.rules] {
             |            int32: {gt: 17}
             |          }
             |        }
             |      }
             |      set {
             |         [opts.rules] {}
             |      }
             |    }
             |  ]
             |};
             |""".stripMargin
      )
    intercept[GeneratorException] {
      FileOptionsCache.buildCache(
        generateFileSet(base ++ settingOthers),
        SecondaryOutputProvider.empty
      )
    }.getMessage() must startWith(
      "e1.proto: FieldTransformation.set must contain only [scalapb.field] field"
    )
  }

  it should "throw exception when import is missing on injected transformations" in {
    val extraNoImport = Map(
      "injected_options.proto" ->
        """|syntax = "proto3";
           |package injected;
           |
           |import "scalapb/scalapb.proto";
           |option (scalapb.options) = {preprocessors: ["myproc"]; scope: PACKAGE};
         """.stripMargin
    )
    intercept[GeneratorException] {
      FileOptionsCache.buildCache(generateFileSet(base ++ extraNoImport), preproc)
    }.getMessage() must startWith("injected_options.proto: Could not find extension number 50001")
  }

  it should "should be inherited to package when injected" in {
    val extraWithImport = Map(
      "injected_options.proto" ->
        """|syntax = "proto3";
           |package injected;
           |
           |import "scalapb/scalapb.proto";
           |import "myvalidate.proto";
           |option (scalapb.options) = {preprocessors: ["myproc"]; scope: PACKAGE};
           |message F { opts.FieldRules r = 1; } // avoid unused import protoc warning
         """.stripMargin,
      "injected_inherits_no_include.proto" ->
        """|syntax = "proto3";
           |package injected;
           |
           |message Foo { int32 x = 1; }
         """.stripMargin,
      "injected_inherits_uses.proto" ->
        """|syntax = "proto3";
           |package injected;
           |import "myvalidate.proto";
           |
           |message Foo2 {  int32 x = 1 [(opts.rules) = {int32: {gt: 0}}]; }
         """.stripMargin
    )
    val cache = FileOptionsCache.buildCache(generateFileSet(base ++ extraWithImport), preproc)
    options(cache, "injected_inherits_no_include.proto")
      .getFieldTransformationsCount() must be(1)
    options(cache, "injected_inherits_uses.proto")
      .getAuxFieldOptionsList()
      .asScala
      .map(_.toString()) must be(
      Seq(
        """|target: "injected.Foo2.x"
           |options {
           |  type: "PositiveInt"
           |}
           |""".stripMargin
      )
    )
  }

  "matchContains" must "match correctly" in {
    matchContains(
      msg = "",
      pattern = ""
    ) must be(true)

    matchContains(
      msg = "",
      pattern = "int32: {gt: 1}"
    ) must be(false)

    matchContains(
      msg = "int32: {gt: 1, lt: 2}",
      pattern = "int32: {gt: 1}"
    ) must be(true)

    matchContains(
      msg = "int32: {gt: 1, lt: 2}",
      pattern = "int32: {gt: 1, lt: 2}"
    ) must be(true)

    matchContains(
      msg = "int32: {gt: 1, lt: 2}",
      pattern = "int32: {gt: 1, lt: 3}"
    ) must be(false)

    matchContains(
      msg = "int32: {gt: 1, lt: 2}",
      pattern = "repeated { items { int32: {gt: 1}}}"
    ) must be(false)

    matchContains(
      msg = "repeated { items { int32: {gt: 1, lt: 2} } }",
      pattern = "repeated { items { int32: {gt: 1}}}"
    ) must be(true)

    matchContains(
      msg = "",
      pattern = "int32: {gt: 0}"
    ) must be(false)

    matchContains(
      msg = """string: {in: ["a", "b"]}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(true)

    matchContains(
      msg = """string: {in: ["a", "b", "c"]}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(false)

    matchContains(
      msg = """string: {in: ["a"]}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(false)

    matchContains(
      msg = """string: {const: "a"}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(false)
  }

  "matchPresence" must "match correctly" in {
    matchPresence(
      msg = "",
      pattern = ""
    ) must be(true)

    matchPresence(
      msg = "",
      pattern = "int32: {gt: 1}"
    ) must be(false)

    matchPresence(
      msg = "int32: {gt: 2}",
      pattern = "int32: {gt: 1}"
    ) must be(true)

    matchPresence(
      msg = "int32: {gt: 0}",
      pattern = "int32: {gt: 1}"
    ) must be(true)

    matchPresence(
      msg = "int32: {gt: 0}",
      pattern = "int32: {gt: 0}"
    ) must be(true)

    matchPresence(
      msg = "int32: {}",
      pattern = "int32: {}"
    ) must be(true)

    matchPresence(
      msg = "int32: {}",
      pattern = "int32: {gt: 0}"
    ) must be(false)

    matchPresence(
      msg = "int32: {gt: 0}",
      pattern = "int32: {}"
    ) must be(true)

    matchPresence(
      msg = "int32: {gt: 0, lt: 0}",
      pattern = "int32: {gt: 0}"
    ) must be(true)

    matchPresence(
      msg = """string: {in: ["a", "b", "c"]}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(true)

    matchPresence(
      msg = """string: {in: ["a"]}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(true)

    matchPresence(
      msg = """string: {in: []}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(false)

    matchPresence(
      msg = """string: {const: "a"}""",
      pattern = """string: {in: ["a", "b"]}"""
    ) must be(false)
  }

  def fieldByPath(fo: FieldDescriptorProto, path: String) =
    FieldTransformations.fieldByPath(fo, path, context)

  "fieldByPath" should "return correct result" in {
    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "options.[opts.rules].int32.gt"
    ) must be("1")

    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "options.[opts.rules].int32.lt"
    ) must be("2")

    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "options.[opts.rules].int32.gte"
    ) must be("0")

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        "options.[opts.rules].int32.foo"
      )
    }.getMessage must be(
      "Could not find field named foo when resolving options.[opts.rules].int32.foo"
    )

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        "options.[opts.rules].int32.gt.lt"
      )
    }.getMessage must be(
      "Type INT32 does not have a field lt in options.[opts.rules].int32.gt.lt"
    )

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        ""
      )
    }.getMessage() must be("Got an empty path")
  }

  import FieldTransformations.interpolateStrings

  def fieldDescriptor(s: String) =
    FieldDescriptorProto.newBuilder
      .setOptions(
        FieldOptions.newBuilder
          .setExtension(
            Scalapb.field,
            TextFormat.parse(s, classOf[scalapb.options.Scalapb.FieldOptions])
          )
          .build()
      )
      .build()

  def scalapbOptions(s: String) = TextFormat.parse(s, classOf[ScalaPbOptions])

  "interpolateStrings" should "interpolate strings" in {
    interpolateStrings(
      fieldDescriptor("type: \"Thingie($(options.[opts.rules].int32.gt))\""),
      fieldRules("int32: {gt: 1, lt: 2}"),
      context
    ) must be(
      fieldDescriptor("type: \"Thingie(1)\"")
    )

    interpolateStrings(
      fieldDescriptor(
        "type: \"Thingie($(options.[opts.rules].int32.gt), $(options.[opts.rules].int32.lt))\""
      ),
      fieldRules("int32: {gt: 1, lt: 2}"),
      context
    ) must be(
      fieldDescriptor("type: \"Thingie(1, 2)\"")
    )

    interpolateStrings(
      fieldDescriptor("type: \"Thingie($(options.[opts.rules].int32.gte))\""),
      fieldRules("int32: {gt: 1, lt: 2}"),
      context
    ) must be(
      fieldDescriptor("type: \"Thingie(0)\"")
    )

    // To test that it looks into nested fields:
    interpolateStrings(
      scalapbOptions(
        "aux_field_options { options: { type: \"Thingie($(options.[opts.rules].int32.gt))\" } }"
      ),
      fieldRules("int32: {gt: 1, lt: 2}"),
      context
    ) must be(
      scalapbOptions("aux_field_options: { options: {type: \"Thingie(1)\"} }")
    )

    interpolateStrings(
      fieldDescriptor("type: \"Thingie($(options.[opts.rules].string.const))\""),
      fieldRules("string: {const: \"r/(.{7}).*/$1xxxxx/\"}"),
      context
    ) must be(
      fieldDescriptor("type: \"Thingie(\"\"r/(.{7}).*/$1xxxxx/\"\")\"")
    )

    interpolateStrings(
      fieldDescriptor("type: \"Thingie($(options.[scalapb.field].type))\""),
      fieldRules("int32: {gt: 1, lt: 2}", scalapbFieldOptions = Some("type: \"CUSTOM\"")),
      contextWithScalaPB
    ) must be(fieldDescriptor("type: \"Thingie(CUSTOM)\""))

    intercept[GeneratorException] {
      interpolateStrings(
        fieldDescriptor("type: \"Thingie($(options.[opts.rules].int32.gtx))\""),
        fieldRules("int32: {gt: 1, lt: 2}"),
        context
      )
    }.getMessage() must be(
      "Could not find field named gtx when resolving options.[opts.rules].int32.gtx"
    )

    intercept[GeneratorException] {
      interpolateStrings(
        fieldDescriptor("type: \"Thingie($([opts.rules].int32.gt))\""),
        fieldRules("int32: {gt: 1, lt: 2}"),
        context
      )
    }.getMessage() must be(
      "Extension [opts.rules] is not an extension of google.protobuf.FieldDescriptorProto, " +
        "it is an extension of google.protobuf.FieldOptions. Did you mean options.[opts.rules] ?"
    )

    intercept[GeneratorException] {
      interpolateStrings(
        fieldDescriptor("type: \"Thingie($([scalapb.field].int32.gt))\""),
        fieldRules("int32: {gt: 1, lt: 2}"),
        contextWithScalaPB
      )
    }.getMessage() must be(
      "Extension [scalapb.field] is not an extension of google.protobuf.FieldDescriptorProto, " +
        "it is an extension of google.protobuf.FieldOptions. Did you mean options.[scalapb.field] ?"
    )

    intercept[GeneratorException] {
      interpolateStrings(
        fieldDescriptor("type: \"Thingie($(options.[opts.rules].int32.[opts.rules].gt))\""),
        fieldRules("int32: {gt: 1, lt: 2}"),
        context
      )
    }.getMessage() must be(
      "Extension [opts.rules] is not an extension of opts.Int32Rules, it is an extension of " +
        "google.protobuf.FieldOptions. Did you mean options.[opts.rules] ?"
    )
  }
}
