---
title: "Upgrade guide"
sidebar_label: "Upgrading"
---

## Migrating to ScalaPB 0.7.x

From version 0.7.0 and onwards, ScalaPB artifacts are published under the `com.thesamet.scalapb` group id instead of the com.trueaccord.scalapb group id.

In addition, all classes in the `com.trueaccord.scalapb` are moved to the `scalapb` top-level
package. During 0.7.x, we will keep type aliases and references in the original `com.trueaccord.scalapb`
location so you may get deprecation warnings, but your code is unlikely to break.

You will need to "sbt clean" and "sbt compile" again, to make sure the old generated classes
are removed and new ones are generated. The generated code should not yield any deprecation warnings.

Since the artifact name has changed, you will need to make sure all your other ScalaPB
dependencies, such as `scalapb-json4s` and `sparksql-scalapb`, are updated to 0.7.x. Otherwise,
they will pull an additional copy of ScalaPB from the old group id name.

## Migrating from sbt-scalapb to sbt-protoc

In `project/scalapb.proto` or `project/plugins.proto`, remove the library
dependency on `sbt-scalapb`.

**Add** `sbt-protoc` plugin:

    addSbtPlugin("com.thesamet" % "sbt-protoc" % "@sbt_protoc@")

If you had a dependency on `protoc-jar` it can be removed, since it is now
provided through `sbt-protoc`.

In `build.sbt`:

**Remove** this line:

    import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

**Remove** this line:

    PB.protobufSettings

If you set up `protoc-jar`, you can also **remove** this setting, since this
is the default:

    PB.runProtoc in PB.protobufConfig := (args =>
      com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

**Add** a value to `gen.targets`:

    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )

If you need Java Conversions, flat packages, etc see
[ScalaPB SBT Settings](sbt-settings.md).

If you are using files like `scalapb.proto` and Google's well-known proto
change the library dependency from:

    "com.trueaccord.scalapb" %% "scalapb-runtime" % "@scalapb@" % PB.protobufConfig

to:

    "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

