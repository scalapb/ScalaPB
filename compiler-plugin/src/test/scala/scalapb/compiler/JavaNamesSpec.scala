package scalapb.compiler

import com.google.protobuf.DescriptorProtos.FileDescriptorSet
import com.google.protobuf.Descriptors.{Descriptor, FileDescriptor}
import java.net.URLClassLoader
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.tools.ToolProvider
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import scala.jdk.CollectionConverters._

class JavaNamesSpec extends AnyFlatSpec with Matchers {
  "Java names" should "match compiled protoc output across legacy syntax and Editions" in {
    val dir                                     = Files.createTempDirectory("scalapb-java-names")
    def write(name: String, text: String): Unit = {
      val path = dir.resolve(name)
      Files.createDirectories(path.getParent)
      Files.write(path, text.getBytes(StandardCharsets.UTF_8))
      ()
    }
    // Use the protos shipped with the same protobuf-java as the compiler, including
    // Java's Editions features. Do not depend on the older third_party snapshots.
    Seq("descriptor.proto", "java_features.proto").foreach { name =>
      val resource = s"google/protobuf/$name"
      val input    = classOf[FileDescriptor].getClassLoader.getResourceAsStream(resource)
      require(input != null, s"Missing protobuf resource: $resource")
      val target = dir.resolve(resource)
      Files.createDirectories(target.getParent)
      try Files.copy(input, target)
      finally input.close()
    }
    val cases = Seq(
      (
        "under_score2-name.protodevel",
        """syntax = "proto3"; package filenames;
        option java_multiple_files = true;
        message Message {}""",
        "UnderScore2Name"
      ),
      (
        "empty_package.proto",
        """syntax = "proto2"; package ignored;
        option java_package = "";
        message Message {}""",
        "EmptyPackage"
      ),
      (
        "legacy.proto",
        """syntax = "proto2"; package legacy;
        message Legacy { message Inner {} enum Kind { ZERO = 0; } }
        enum Top { ZERO = 0; }""",
        "LegacyOuterClass"
      ),
      (
        "enum_collision.proto",
        """syntax = "proto3"; package enum_collision;
        enum EnumCollision { ZERO = 0; }""",
        "EnumCollisionOuterClass"
      ),
      (
        "service_collision.proto",
        """syntax = "proto2"; package service_collision;
        option java_generic_services = true;
        service ServiceCollision {}""",
        "ServiceCollisionOuterClass"
      ),
      (
        "nested_collision.proto",
        """syntax = "proto3"; package nested_collision;
        message Parent { message NestedCollision {} }""",
        "NestedCollisionOuterClass"
      ),
      (
        "nested_enum.proto",
        """syntax = "proto2"; package nested_enum;
        message Parent { enum NestedEnum { ZERO = 0; } }""",
        "NestedEnumOuterClass"
      ),
      (
        "multiple.proto",
        """syntax = "proto3"; package multiple;
        option java_multiple_files = true; option java_outer_classname = "Custom";
        option java_package = "java_names.type";
        message Given { message Inner {} enum Kind { ZERO = 0; } }
        enum Top { ZERO = 0; }""",
        "Custom"
      ),
      (
        "override.proto",
        """syntax = "proto2"; package overrides;
        option java_package = "java_names.match"; option java_outer_classname = "object";
        message type { message given {} enum match { ZERO = 0; } }
        enum Top { ZERO = 0; }""",
        "object"
      ),
      ("no_package.proto", """syntax = "proto3"; message NoPackage {}""", "NoPackageOuterClass"),
      (
        "edition23.proto",
        """edition = "2023"; package edition23;
        message Edition23 {}""",
        "Edition23OuterClass"
      ),
      (
        "edition24.proto",
        """edition = "2024"; package edition24;
        message Edition24 { message Inner {} enum Kind { ZERO = 0; } }
        enum Top { ZERO = 0; }""",
        "Edition24Proto"
      ),
      (
        "mixed.proto",
        """edition = "2024"; package mixed;
        import "google/protobuf/java_features.proto";
        option java_package = "java_names.mixed";
        option java_outer_classname = "MixedFile";
        message Nested {
          option features.(pb.java).nest_in_file_class = YES;
          message Inner {} enum Kind { ZERO = 0; }
        }
        message Separate { message Inner {} enum Kind { ZERO = 0; } }
        enum NestedEnum {
          option features.(pb.java).nest_in_file_class = YES;
          ZERO = 0;
        }
        enum SeparateEnum { ONE = 0; }""",
        "MixedFile"
      ),
      (
        "explicit_nesting.proto",
        """edition = "2024"; package explicit_nesting;
        import "google/protobuf/java_features.proto";
        message Nested {
          option features.(pb.java).nest_in_file_class = YES;
          message Inner {}
        }
        message Separate { option features.(pb.java).nest_in_file_class = NO; }
        enum SeparateEnum {
          option features.(pb.java).nest_in_file_class = NO;
          ZERO = 0;
        }""",
        "ExplicitNestingProto"
      )
    )
    cases.foreach { case (name, content, _) => write(name, content) }
    val classes     = Files.createDirectory(dir.resolve("classes"))
    val descriptors = dir.resolve("files.pb")
    try {
      ProtocRunner
        .forVersion(Version.protobufVersion)
        .run(
          Seq(
            s"-I$dir",
            s"--java_out=$dir",
            s"--descriptor_set_out=$descriptors",
            "--include_imports"
          ) ++
            cases.map(c => dir.resolve(c._1).toString),
          Seq.empty
        ) must be(0)
      val paths   = Files.walk(dir)
      val sources =
        try paths.iterator.asScala.filter(_.toString.endsWith(".java")).toVector
        finally paths.close()
      val protobufJar =
        classOf[FileDescriptor].getProtectionDomain.getCodeSource.getLocation.getPath
      val javac = ToolProvider.getSystemJavaCompiler
      require(javac != null, "Java naming integration tests require a JDK")
      javac.run(
        null,
        null,
        null,
        (Seq("-classpath", protobufJar, "-d", classes.toString) ++ sources.map(_.toString)): _*
      ) must be(0)
      // Parse without a Java extension registry, just like incoming plugin requests.
      val protos = FileDescriptorSet.parseFrom(Files.readAllBytes(descriptors))
      val files  = protos.getFileList.asScala.foldLeft(Map.empty[String, FileDescriptor]) {
        case (acc, proto) =>
          acc + (proto.getName -> FileDescriptor.buildFrom(
            proto,
            proto.getDependencyList.asScala.map(acc).toArray
          ))
      }
      val implicits = new DescriptorImplicits(
        GeneratorParams(),
        files.values.toSeq,
        SecondaryOutputProvider.empty
      )
      import implicits._
      val loader = new URLClassLoader(Array(classes.toUri.toURL), this.getClass.getClassLoader)
      try {
        // Search actual compiled classes by their canonical source name. This
        // deliberately does not use GeneratorNames to predict a binary name.
        val classPaths  = Files.walk(classes)
        val binaryNames =
          try
            classPaths.iterator.asScala
              .filter(_.toString.endsWith(".class"))
              .map(p =>
                classes
                  .relativize(p)
                  .toString
                  .stripSuffix(".class")
                  .replace(java.io.File.separatorChar, '.')
              )
              .toVector
          finally classPaths.close()
        def checkType(name: String, fullName: String, file: FileDescriptor): Unit = {
          file.fullJavaName(fullName) must be(name)
          val cls = binaryNames.iterator
            .map(loader.loadClass)
            .find(_.getCanonicalName == name.replace("`", ""))
            .getOrElse(fail(s"No generated Java class for $fullName: $name"))
          val descriptor = cls
            .getMethod("getDescriptor")
            .invoke(null)
            .asInstanceOf[com.google.protobuf.Descriptors.GenericDescriptor]
          descriptor.getFullName must be(fullName)
          ()
        }
        def checkMessage(message: Descriptor): Unit = {
          checkType(message.javaTypeName, message.getFullName, message.getFile)
          message.getNestedTypes.asScala.foreach(checkMessage)
          message.getEnumTypes.asScala.foreach(e =>
            checkType(e.javaTypeName, e.getFullName, e.getFile)
          )
        }
        cases.foreach { case (name, _, outer) =>
          val file = files(name)
          file.javaOuterClassName must be(outer)
          val params             = GeneratorParams(javaConversions = true)
          val generatorImplicits =
            new DescriptorImplicits(params, files.values.toSeq, SecondaryOutputProvider.empty)
          val generated = new ProtobufGenerator(params, generatorImplicits)
            .generateMultipleScalaFilesForFileDescriptor(file)
          generated.map(_.getContent).mkString("\n") must include(
            s"${file.javaFullOuterClassName}.getDescriptor()"
          )
          val cls = loader.loadClass(file.javaFullOuterClassName.replace("`", ""))
          cls.getMethod("getDescriptor").invoke(null).asInstanceOf[FileDescriptor].getName must be(
            name
          )
          file.getMessageTypes.asScala.foreach(checkMessage)
          file.getEnumTypes.asScala.foreach(e => checkType(e.javaTypeName, e.getFullName, file))
          file.getServices.asScala.foreach(s =>
            checkType(file.fullJavaName(s.getFullName), s.getFullName, file)
          )
        }
        files("override.proto").javaFullOuterClassName must be("java_names.`match`.`object`")
        files("override.proto").findMessageTypeByName("type").javaTypeName must be(
          "java_names.`match`.`object`.`type`"
        )
        files("mixed.proto").findMessageTypeByName("Nested").javaTypeName must be(
          "java_names.mixed.MixedFile.Nested"
        )
        files("mixed.proto").findMessageTypeByName("Separate").javaTypeName must be(
          "java_names.mixed.Separate"
        )
      } finally loader.close()
    } finally {
      val paths = Files.walk(dir)
      try paths.iterator.asScala.toVector.reverse.foreach(Files.delete)
      finally paths.close()
    }
  }
}
