---
title: "Using third-party protos"
---

The protos you are trying to generate code may depend on other protos which are currently are not a part of your build. We need to solve two problems:

1. Making those proto files available to `protoc`'s import search path so the `import` statements in your protos don't cause errors and code is generated.
1. Providing Scala classes for those third-party protos.

There are multiple ways to solve those problems.

## Common protos: maybe a Scala package for the protos already exists?

Check whether a [ScalaPB Common Protos](common-protos.md) package is already avaiable for the protos. If the proto library you are looking for, consider making a PR or file a feature request.

You will add such libraries to your project twice: once with a `protobuf` suffix and once without:

```
libraryDependencies ++= Seq(
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_1.0" % "1.17.0-0" % "protobuf"
  "com.thesamet.scalapb.common-protos" %% "proto-google-common-protos-scalapb_1.0" % "1.17.0-0"
)
```

The first one makes `sbt-protoc` unpack the protos from the jar and add them to the import search path so protoc can import them (Problem 1 above), and the second import adds the compiled Scala classes to your classpath (Problem 2 above). In this solution, protoc will not generate code for the third-party protos: the provided package already gives you compiled classes for the generated code.

## There is a library on Maven with the protos (and possibly generated Java code)

Consider [adding it to Common Protos](https://github.com/scalapb/common-protos). If this is not possible (for example, maybe the package is on a repository internal to your company), then you can have your SBT project download the library and build it by using the `protobuf-src` config. For example:

```
libraryDependencies += "com.somepackage" %% "with-protos" % "1.0" % "protobuf-src" intransitive()
```

This would make sbt-protoc download this JAR unpack it to `target/protobuf_external_src`, and make it both available for imports and generate code for it (which solves both problems above at once).

Without the `intransitive()` modifier, sbt-protoc would generate code for all the dependencies of this package, and this is generally undesirable - since this is likely to lead to duplicate classes being generated.

If the given package has dependencies, you will need to manually add them. The dependencies should be added with `protobuf-src` scope if you want to build them too. If you already have compiled packages for these dependencies, add the package both with `protobuf` and without like in the "common protos" example above.

It is recommended to create a separate SBT sub-project for the third-party protos. For example:

```scala
lazy val externalProtos = (project in file("ext-protos"))
  .settings(
    libraryDependencies ++= Seq(
      "com.thesamet.test" % "test-protos" % "0.1" % "protobuf-src" intransitive(),
      "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"
    ),

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )

// myProject contains its own protos which rely on protos from externalProtos
lazy val myProject = (project in file("my-project"))
  .dependsOn(externalProtos)
  .settings(
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
  )
```

See [full example here](https://github.com/thesamet/sbt-protoc/tree/master/examples/multi-with-external-jar).
