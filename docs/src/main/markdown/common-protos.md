---
title: "Common protos"
layout: docs
---

# Common protos

This page lists packages that contain compiled ScalaPB classes for common third-party protobuf libraries.

Each of these packages require you to add two library dependencies in your `build.sbt`, regular one that would add the generated classes to the class path, and one suffixed with `protobuf` so the third-party protos are unpacked and your own protos can import them.

# Adding new packages

If you don't see your favorite third-party proto package here, and there is already a maven package for it that provides the proto files (with possibly Java generated classes), you can send a pull request to common-protos to have it added. See instruction on the [ScalaPB Common Protos project page on Github](https://github.com/scalapb/common-protos).

# Available packages

```scala mdoc:passthrough
scalapb.docs.CommonProtos.printTable()
```