---
title: "Common protos"
layout: docs
---

This page lists packages that contain compiled ScalaPB classes for common third-party protobuf libraries.

Each of these packages require you to add two library dependencies in your `build.sbt`:

* The dependency with the `"protobuf"` unpacks the protos from the jar. This
  allows the protos in your own project to "import" the third-party protos
  (without this, protoc would fail with an error like: `"Import was not found or had errors"`)

* The generated code for your protos, may reference types that are defined in
  the protos in that jar. That requires a Scala class for them available in
  the classpath. This is accomplished by adding the library as a normal
  dependency.

If you don't have any proto files that import the common protos, then you can omit the `"protobuf"` dependency.

## Adding new packages

If you don't see your favorite third-party proto package here, and there is already a maven package for it that provides the proto files (with possibly Java generated classes), you can send a pull request to common-protos to have it added. See instruction on the [ScalaPB Common Protos project page on Github](https://github.com/scalapb/common-protos).

## Available packages

These JVM artifacts are built for Scala 2.12.21, 2.13.18, and 3.3.8, using ScalaPB
0.11.21 and 1.0.0-alpha.6. Match the artifact's ScalaPB line to your generator and
runtime. The 1.0 line is experimental; Scala 3 consumers need Scala 3.3.8 or later.

Older ScalaPB 0.9 and 0.10 artifacts remain available at their existing versions on
[Maven Central](https://repo.maven.apache.org/maven2/com/thesamet/scalapb/common-protos/).
They are not part of these releases. The 0.10 line is frozen because its generator
cannot handle the proto3 optional fields used by current Google schemas.

The Google packages bring in protobuf-java 4.33.6 on the ScalaPB 0.11 line; the
ScalaPB 1.0 line resolves protobuf-java 4.35.0. Do not force protobuf-java 3 onto
these Google Java classes. Pub/Sub and Dataproc depend on the matching
common-protos artifact.

PGV remains available for existing consumers. Its upstream replacement,
[Protovalidate](https://github.com/bufbuild/protovalidate), uses different schemas
and is not a drop-in replacement for these artifacts.

```scala mdoc:passthrough
scalapb.docs.CommonProtos.printTable()
```
