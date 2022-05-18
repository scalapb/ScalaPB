package scalapb.compiler
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, FileOptions}
import com.google.protobuf.Descriptors.FileDescriptor
import scalapb.options.Scalapb
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.options.Scalapb.ScalaPbOptions.OptionsScope
import scala.jdk.CollectionConverters._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scalapb.options.Scalapb.PreprocessorOutput

class FileOptionsCacheSpec extends AnyFlatSpec with Matchers {
  "parentPackages" should "return correct parent packages" in {
    FileOptionsCache.parentPackages("foo.bar.baz") must be(
      Seq("foo.bar", "foo")
    )

    FileOptionsCache.parentPackages("foo.bar") must be(
      Seq("foo")
    )

    FileOptionsCache.parentPackages("foo") must be(
      Seq()
    )
  }

  def file(
      name: String,
      protoPackage: String,
      scope: OptionsScope,
      scalaPackage: Option[String] = None,
      imports: Seq[String] = Nil,
      singleFile: Option[Boolean] = None,
      preprocessors: Seq[String] = Nil
  ): FileDescriptor = {
    val optionsBuilder = ScalaPbOptions.newBuilder()

    if (scope != OptionsScope.FILE) {
      optionsBuilder.setScope(scope)
    }

    scalaPackage.foreach(optionsBuilder.setPackageName)
    singleFile.foreach(optionsBuilder.setSingleFile)
    optionsBuilder.addAllImport(imports.asJava)
    optionsBuilder.addAllPreprocessors(preprocessors.asJava)

    val proto = FileDescriptorProto
      .newBuilder()
      .setName(name)
      .setPackage(protoPackage)
      .setOptions(FileOptions.newBuilder.setExtension(Scalapb.options, optionsBuilder.build))
      .build()

    FileDescriptor.buildFrom(proto, Array())
  }

  "buildCache" should "merge options correctly" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true)
    )

    val p1_x_p2 = file(
      name = "p1.x.p2.proto",
      protoPackage = "p1.x.p2",
      scope = OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1.p2"),
      imports = Seq("i2")
    )

    val p1_x_p2_x_p3 = file(
      name = "p1.x.p2.x.p3.proto",
      protoPackage = "p1.x.p2.x.p3",
      scope = OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1.p2.p3"),
      singleFile = Some(false)
    )

    val p1_file = file(
      name = "p1.f.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      scalaPackage = Some("scc.p1_custom")
    )

    val p1_x_file = file(
      name = "p1.x.proto",
      protoPackage = "p1.x",
      scope = OptionsScope.FILE,
      singleFile = Some(false)
    )

    val p1_x_p2_t_file = file(
      name = "p1.x.p2.x.t.proto",
      protoPackage = "p1.x.p2.x.t",
      scope = OptionsScope.FILE,
      imports = Seq("tt")
    )

    FileOptionsCache
      .buildCache(
        Seq(
          p1_file,
          p1,
          p1_x_file,
          p1_x_p2_t_file,
          p1_x_p2,
          p1_x_p2_x_p3
        ),
        SecondaryOutputProvider.empty
      )
      .map { case (k, v) => (k, v.toString) } must be(
      Map(
        p1 ->
          """package_name: "scc.p1"
            |import: "i1"
            |single_file: true
            |scope: PACKAGE
            |""".stripMargin,
        p1_file ->
          """package_name: "scc.p1_custom"
            |import: "i1"
            |single_file: true
            |scope: FILE
            |""".stripMargin,
        p1_x_file ->
          """package_name: "scc.p1"
            |import: "i1"
            |single_file: false
            |scope: FILE
            |""".stripMargin,
        p1_x_p2_t_file ->
          """package_name: "scc.p1.p2"
            |import: "i1"
            |import: "i2"
            |import: "tt"
            |single_file: true
            |scope: FILE
            |""".stripMargin,
        p1_x_p2 ->
          """package_name: "scc.p1.p2"
            |import: "i1"
            |import: "i2"
            |single_file: true
            |scope: PACKAGE
            |""".stripMargin,
        p1_x_p2_x_p3 ->
          """package_name: "scc.p1.p2.p3"
            |import: "i1"
            |import: "i2"
            |single_file: false
            |scope: PACKAGE
            |""".stripMargin
      )
    )
  }

  it should "throw exception when no secondary outputs available" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("preproc")
    )
    intercept[GeneratorException](
      FileOptionsCache
        .buildCache(
          Seq(
            p1
          ),
          SecondaryOutputProvider.empty
        )
    ).message must startWith("p1.proto: No secondary outputs available.")
  }

  it should "throw exception when preprocessor name is missing" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("foo")
    )
    intercept[GeneratorException](
      FileOptionsCache
        .buildCache(
          Seq(
            p1
          ),
          SecondaryOutputProvider.fromMap(Map.empty)
        )
    ).message must startWith("p1.proto: Preprocessor 'foo' was not found.")
  }

  it should "throw exception when preprocessor returns package scoped options" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("preproc")
    )

    val provider = SecondaryOutputProvider.fromMap(
      Map(
        "preproc" -> PreprocessorOutput
          .newBuilder()
          .putOptionsByFile(
            "p1.proto",
            ScalaPbOptions
              .newBuilder()
              .setScope(OptionsScope.PACKAGE)
              .build()
          )
          .build()
      )
    )

    intercept[GeneratorException](
      FileOptionsCache
        .buildCache(
          Seq(
            p1
          ),
          provider
        )
    ).message must startWith(
      "Preprocessor options must be file-scoped. Preprocessor 'preproc' provided scope 'PACKAGE' for file p1.proto"
    )
  }

  it should "merge preprocessor output for files" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("preproc")
    )

    val p2 = file(
      name = "p2.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("preproc")
    )

    val provider = SecondaryOutputProvider.fromMap(
      Map(
        "preproc" -> PreprocessorOutput
          .newBuilder()
          .putOptionsByFile(
            "p1.proto",
            ScalaPbOptions
              .newBuilder()
              .addImport("i2")
              .setSingleFile(false)
              .build()
          )
          .build()
      )
    )

    FileOptionsCache
      .buildCache(
        Seq(
          p1,
          p2
        ),
        provider
      )
      .map { case (k, v) => (k, v.toString) } must be(
      Map(
        p1 ->
          """|import: "i2"
             |import: "i1"
             |single_file: true
             |scope: FILE
             |preprocessors: "preproc"
             |""".stripMargin,
        p2 ->
          """|import: "i1"
             |single_file: true
             |preprocessors: "preproc"
             |""".stripMargin
      )
    )
  }

  it should "merge preprocessor output when preprocessor is referenced through package-scoped option" in {
    val p1 = file(
      name = "p1.proto",
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true),
      preprocessors = Seq("preproc")
    )

    val p1_x = file(
      name = "p1.x.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      imports = Seq("i2"),
      singleFile = Some(true)
    )

    val p1_y = file(
      name = "p1.y.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      imports = Seq("i3"),
      singleFile = Some(true)
    )

    val p1_z = file(
      name = "p1.z.proto",
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      imports = Seq("i4"),
      singleFile = Some(true),
      preprocessors = Seq("-preproc")
    )

    val provider = SecondaryOutputProvider.fromMap(
      Map(
        "preproc" ->
          PreprocessorOutput
            .newBuilder()
            .putOptionsByFile(
              "p1.x.proto",
              ScalaPbOptions
                .newBuilder()
                .addImport("i4")
                .setSingleFile(false)
                .build()
            )
            .putOptionsByFile(
              "p1.proto",
              ScalaPbOptions
                .newBuilder()
                .addImport("iz")
                .setScope(OptionsScope.FILE)
                .setSingleFile(false)
                .build()
            )
            .putOptionsByFile(
              "p1.z.proto", // should not apply since preprocessor is excluded.
              ScalaPbOptions
                .newBuilder()
                .addImport("ip1z")
                .setScope(OptionsScope.FILE)
                .setSingleFile(false)
                .build()
            )
            .build()
      )
    )

    FileOptionsCache
      .buildCache(
        Seq(
          p1,
          p1_x,
          p1_y,
          p1_z
        ),
        provider
      )
      .map { case (k, v) => (k.getName(), v.toString) } must be(
      Map(
        "p1.proto" ->
          """|package_name: "scc.p1"
             |import: "iz"
             |import: "i1"
             |single_file: true
             |scope: PACKAGE
             |preprocessors: "preproc"
             |""".stripMargin,
        "p1.x.proto" ->
          """|package_name: "scc.p1"
             |import: "iz"
             |import: "i1"
             |import: "i4"
             |import: "i2"
             |single_file: true
             |scope: FILE
             |preprocessors: "preproc"
             |""".stripMargin,
        "p1.y.proto" ->
          """|package_name: "scc.p1"
             |import: "iz"
             |import: "i1"
             |import: "i3"
             |single_file: true
             |scope: FILE
             |preprocessors: "preproc"
             |""".stripMargin,
        "p1.z.proto" ->
          """|package_name: "scc.p1"
             |import: "iz"
             |import: "i1"
             |import: "i4"
             |single_file: true
             |scope: FILE
             |preprocessors: "preproc"
             |preprocessors: "-preproc"
             |""".stripMargin
      )
    )
  }
}
