---
title: "Common protos"
layout: docs
---

# Common protos

This page lists packages that contain compiled ScalaPB classes for common third-party protobuf libraries.

Each of these packages require you to add two library dependencies in your `build.sbt`:

* The depenendcy with the `"protobuf"` unpacks the protos from the jar. This
  allows the protos in your own project to "import" the third-party protos
  (without this, protoc would fail with an error like: `"Import was not found or had errors"`)

* The generated code for your protos, may reference types that are defined in
  the protos in that jar. That requires a Scala class for them available in
  the classpath. This is accomplished by adding the library as a normal
  dependency.

# Adding new packages

If you don't see your favorite third-party proto package here, and there is already a maven package for it that provides the proto files (with possibly Java generated classes), you can send a pull request to common-protos to have it added. See instruction on the [ScalaPB Common Protos project page on Github](https://github.com/scalapb/common-protos).

# Available packages

```scala mdoc:passthrough
scalapb.docs.CommonProtos.printTable()
```
