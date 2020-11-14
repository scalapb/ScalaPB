package scalapb.compiler
import com.google.protobuf.DescriptorProtos.{FileDescriptorProto, FileOptions}
import com.google.protobuf.Descriptors.FileDescriptor
import scalapb.options.Scalapb
import scalapb.options.Scalapb.ScalaPbOptions
import scalapb.options.Scalapb.ScalaPbOptions.OptionsScope
import scala.jdk.CollectionConverters._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers

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
      protoPackage: String,
      scope: OptionsScope,
      scalaPackage: Option[String] = None,
      imports: Seq[String] = Nil,
      singleFile: Option[Boolean] = None
  ): FileDescriptor = {
    val optionsBuilder = ScalaPbOptions.newBuilder()

    if (scope != OptionsScope.FILE) {
      optionsBuilder.setScope(scope)
    }

    scalaPackage.foreach(optionsBuilder.setPackageName)
    singleFile.foreach(optionsBuilder.setSingleFile)
    optionsBuilder.addAllImport(imports.asJava)

    val proto = FileDescriptorProto
      .newBuilder()
      .setPackage(protoPackage)
      .setOptions(FileOptions.newBuilder.setExtension(Scalapb.options, optionsBuilder.build))
      .build()

    FileDescriptor.buildFrom(proto, Array())
  }

  "buildCache" should "merge options correctly" in {
    val p1 = file(
      protoPackage = "p1",
      OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1"),
      imports = Seq("i1"),
      singleFile = Some(true)
    )

    val p1_x_p2 = file(
      protoPackage = "p1.x.p2",
      scope = OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1.p2"),
      imports = Seq("i2")
    )

    val p1_x_p2_x_p3 = file(
      protoPackage = "p1.x.p2.x.p3",
      scope = OptionsScope.PACKAGE,
      scalaPackage = Some("scc.p1.p2.p3"),
      singleFile = Some(false)
    )

    val p1_file = file(
      protoPackage = "p1",
      scope = OptionsScope.FILE,
      scalaPackage = Some("scc.p1_custom")
    )

    val p1_x_file = file(
      protoPackage = "p1.x",
      scope = OptionsScope.FILE,
      singleFile = Some(false)
    )

    val p1_x_p2_t_file = file(
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
        )
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
}
