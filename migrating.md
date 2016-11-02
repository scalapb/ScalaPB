---
title: "Migrating from earlier versions"
layout: page
---

# Migrating from sbt-scalapb to sbt-protoc

In `project/scalapb.proto` or `project/plugins.proto`, remove the library
dependency on `sbt-scalapb`.

**Add** `sbt-protoc` plugin:

    addSbtPlugin("com.thesamet" % "sbt-protoc" % "{{site.data.version.sbt_protoc}}")

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
[ScalaPB SBT Settings]({{site.baseurl}}/sbt-settings.html).

If you are using files like `scalapb.proto` and Google's well-known proto
change the library dependency from:

    "com.trueaccord.scalapb" %% "scalapb-runtime" % "{{site.data.version.scalapb}}" % PB.protobufConfig

to:

    "com.trueaccord.scalapb" %% "scalapb-runtime" % com.trueaccord.scalapb.compiler.Version.scalapbVersion % "protobuf"


