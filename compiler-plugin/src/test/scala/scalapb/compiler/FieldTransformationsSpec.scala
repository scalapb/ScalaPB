package scalapb.compiler

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

import scala.jdk.CollectionConverters._
import com.google.protobuf.DynamicMessage
import com.google.protobuf.TextFormat
import com.google.protobuf.DescriptorProtos.FieldOptions
import com.google.protobuf.UnknownFieldSet
import com.google.protobuf.UnknownFieldSet.Field
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
         |}
         |message Int32Rules {
         |  optional int32 gt = 1;
         |  optional int32 gte = 2;
         |  optional int32 lt = 3;
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
         |   {
         |     when {
         |       [opts.rules] {
         |         int32: {gt: 17}
         |       }
         |     }
         |     set {
         |       type: "MatchGt17"
         |     }
         |    }
         |  ]
         |};
         |message X {
         |  int32 x = 1 [(opts.rules).int32 = {gt: 17}];
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
         |      [opts.rules] {
         |        int32: {gt: 12}
         |      }
         |    }
         |    set {
         |      type: "MatchGt12"
         |    }
         |  },
         |  {
         |    when {
         |      [opts.rules] {
         |        int32: {lt: 1}
         |      }
         |    }
         |    match_type: PRESENCE
         |    set {
         |      type: "MatchLt[$([opts.rules].int32.lt)]"
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
         |  int32 y = 2 [(opts.rules).int32 = {lt: 317}];
         |}
         |""".stripMargin,
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
    val when = FieldOptions
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
                      scalapb.options.Scalapb.FieldOptions.newBuilder().setType("PositiveInt")
                    )
                    .build()
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
  val ignores    = files.find(_.getFullName() == "ignores.proto").get
  val extensions = FieldTransformations.fieldExtensionsForFile(inherits)

  def fieldRules(s: String): FieldOptions = {
    val fieldRulesDesc = files
      .flatMap { f => f.getMessageTypes().asScala }
      .find(_.getFullName == "opts.FieldRules")
      .get
    val dm = DynamicMessage.newBuilder(fieldRulesDesc)
    TextFormat.merge(s, dm)
    dm.build()
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
                dm.build().toByteString()
              )
              .build()
          )
          .build()
      )
      .build()
  }

  def matchPresence(msg: String, pattern: String): Boolean = {
    FieldTransformations.matchPresence(
      FieldTransformations.fieldMap("-", fieldRules(msg), extensions),
      FieldTransformations.fieldMap("-", fieldRules(pattern), extensions)
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
      .find(_.getTarget() == "pkg.X.y")
      .get
      .getOptions()
      .getType() must be("MatchLt[317]")
    cache(ignores)
      .getAuxFieldOptionsList()
      .asScala
      .find(_.getTarget() == "pkg.I.x") must be(None)
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
      .map(_.toString())
      .seq must be(
      Seq(
        """|target: "injected.Foo2.x"
           |options {
           |  type: "PositiveInt"
           |}
           |""".stripMargin
      )
    )
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
  }

  def fieldByPath(fo: FieldOptions, path: String) =
    FieldTransformations.fieldByPath(fo, path, extensions)

  "fieldByPath" should "return correct result" in {
    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "[opts.rules].int32.gt"
    ) must be("1")

    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "[opts.rules].int32.lt"
    ) must be("2")

    fieldByPath(
      fieldRules("int32: {gt: 1, lt: 2}"),
      "[opts.rules].int32.gte"
    ) must be("0")

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        "[opts.rules].int32.foo"
      )
    }.getMessage must be("Could not find field named foo when resolving [opts.rules].int32.foo")

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        "[opts.rules].int32.gt.lt"
      )
    }.getMessage must be(
      "Type INT32 does not have a field lt in [opts.rules].int32.gt.lt"
    )

    intercept[GeneratorException] {
      fieldByPath(
        fieldRules("int32: {gt: 1, lt: 2}"),
        ""
      )
    }.getMessage() must be("Got an empty path")
  }

  import FieldTransformations.interpolateStrings

  def fieldOptions(s: String) = TextFormat.parse(s, classOf[scalapb.options.Scalapb.FieldOptions])

  def scalapbOptions(s: String) = TextFormat.parse(s, classOf[ScalaPbOptions])

  "interpolateStrings" should "interpolate strings" in {
    interpolateStrings(
      fieldOptions("type: \"Thingie($([opts.rules].int32.gt))\""),
      fieldRules("int32: {gt: 1, lt: 2}"),
      extensions
    ) must be(
      fieldOptions("type: \"Thingie(1)\"")
    )

    interpolateStrings(
      fieldOptions("type: \"Thingie($([opts.rules].int32.gt), $([opts.rules].int32.lt))\""),
      fieldRules("int32: {gt: 1, lt: 2}"),
      extensions
    ) must be(
      fieldOptions("type: \"Thingie(1, 2)\"")
    )

    interpolateStrings(
      fieldOptions("type: \"Thingie($([opts.rules].int32.gte))\""),
      fieldRules("int32: {gt: 1, lt: 2}"),
      extensions
    ) must be(
      fieldOptions("type: \"Thingie(0)\"")
    )

    // To test that it looks into nested fields:
    interpolateStrings(
      scalapbOptions(
        "aux_field_options { options: { type: \"Thingie($([opts.rules].int32.gt))\" } }"
      ),
      fieldRules("int32: {gt: 1, lt: 2}"),
      extensions
    ) must be(
      scalapbOptions("aux_field_options: { options: {type: \"Thingie(1)\"} }")
    )

    intercept[GeneratorException] {
      interpolateStrings(
        fieldOptions("type: \"Thingie($([opts.rules].int32.gtx))\""),
        fieldRules("int32: {gt: 1, lt: 2}"),
        extensions
      )
    }.getMessage() must be(
      "Could not find field named gtx when resolving [opts.rules].int32.gtx"
    )
  }
}
