# Change Log
## [0.11.2] (Unreleased)
- Fix bug where generated code would not compile if a message was named Unit (#1126)
- 
## [0.11.1]
- Support Scala 3.0.0-RC2
- ScalaPB artifacts include [https://www.scala-lang.org/blog/2021/02/16/preventing-version-conflicts-with-versionscheme.html](versionscheme)

## [0.11.0]
## [0.11.0-M7]
- Add ScalaJs cross build for Scala 3.0.0-RC1

## [0.11.0-M6]
- Update to Scala 3.0.0-RC1
- Update to Scala 2.13.5
- Remove Builder class (#1033)

## [0.11.0-M5]
- GeneratedMessage now extends Product for better type inference (#984)
- Update to Scala 3.0.0-M3
- Update to Scala 2.13.4

## [0.11.0-M4]
- Upgraded to Protobuf 3.13.0
- Add java_conversions as a file-level options in scalapb.proto
- google.protobuf.Timestamp and google.protobuf.Duration now come with a new
  constructor that takes a java.time.Instant or Duration. There are are
  implicit converters between the case classes and the java types that can be
  imported from scalapb.DurationConverters and scalapb.TimestampConverters
  (#939).
- Add support for renaming `oneof` fields (#946)
- Allow sealed oneofs to be defined inside a parent message (#977)
- Support Scala 3.0.0-M1

## [0.11.0-M3]
- Supports protobuf 3.12.2
- Supports the new experimental optional fields introduced in proto3 at
  protobuf 3.12.0 (see https://github.com/protocolbuffers/protobuf/blob/v3.12.0/docs/field_presence.md). To enable, add the following to your `build.sbt`:
    Compile / PB.protocOptions += "--experimental_allow_proto3_optional"
- Supports Dotty (0.27.0-RC1). Generated code compiles in Dotty (with `-source
  3.0-migration`)
- Added auxiliary enum value options.
- Removed dependency on fastparse2 which was used for ASCII format parsing. ScalaPB now
  implements its own custom parser.

## [0.10.10](https://github.com/scalapb/ScalaPB/compare/v0.10.9..v0.10.10)
- Adds support for preprocessor plugins that can mutate ScalaPB options (see #1007)
- Adds new field transformation DSL that allows changing FieldOptions when
  user-defined rules match (see #1007).
- Add more collection-level options in scalapb.proto to support non-standard collections such
  as cats.data.NonEmptyList, NonEmptyMap and NonEmptySet (see scalapb/scalapb-validate#38)
- ScalaPBC backport from master to support NixOS.

## [0.10.9](https://github.com/scalapb/ScalaPB/compare/v0.10.8..v0.10.9)
- Add option for annotating the unknown field (#898)
- Add extension ranges for scalapb.proto messages so plugins can further
  extend ScalaPB.
- scalapb.proto is no longer shaded into compilerplugin.

## [0.10.8](https://github.com/scalapb/ScalaPB/compare/v0.10.7..v0.10.8)
- Fix a regression where calling scalapb.gen() without parameters would not
  generate grpc stubs. Workaround: pass grpc=true explictly. #879
- Add asMap to UnkownFieldSet to make it possible to extract the map (#878)
- ScalaPBC now accepts --jvm-plugin flag that loads an artifact from maven
  that is expected to have a main class which extends ProtocCodeGenerator.

## [0.10.7](https://github.com/scalapb/ScalaPB/compare/v0.10.6..v0.10.7)
- Add support for insertion points so other plugins can inject code into
  ScalaPB generated code. Currently, the only insertion point available is at
  the companion object of messages.

## [0.10.6](https://github.com/scalapb/ScalaPB/compare/v0.10.5..v0.10.6)
- Updated protoc-bridge to 0.9.0-RC2.

## [0.10.5](https://github.com/scalapb/ScalaPB/compare/v0.10.4..v0.10.5)
- Updated protoc-bridge to 0.9.0-RC1.
- Minimum version of sbt-protoc required is 0.99.33
- ScalaPB compiler plugin is now compatible with SBT 1.4.0-M1
- Plugin runs in a sandboxed classloader.

## [0.10.3](https://github.com/scalapb/ScalaPB/compare/v0.10.3..v0.10.4)
- Updated protoc-bridge to 0.8.3
- Changed scalapb.Message to extend Any, instead of a type alias to Any (see #777)

## [0.10.3](https://github.com/scalapb/ScalaPB/compare/v0.10.1..v0.10.3)
- Added StructUtils to convert between messages to Struct, see #812 (Thanks, @ittaiz)

## [0.10.2](https://github.com/scalapb/ScalaPB/compare/v0.10.1..v0.10.2)
- BREAKING CHANGE: remove unknownFields parameter from `of` (see #778)

## [0.10.1](https://github.com/scalapb/ScalaPB/compare/v0.10.0..v0.10.11)
- Add ScalaPBC support for fetching artifacts from maven.

## [0.10.0](https://github.com/scalapb/ScalaPB/compare/v0.9.4..0.10.0)
- For SBT uses, sbt-protoc >= 0.99.28 is required.

- BREAKING CHANGE: all messages are now preserving unknown fields by default.
  This results in an additional parameter to the case class. This may break
  your code if you are using pattern matching and expecting a specific number
  of parameters. This feature can be disabled by settings
  `preserve_unknown_fields` to `false` at the file or package level.

  Why is this feature important? If your service receives messages that were
  serialized by a future version of the proto, the unknown fields your service
  does not know about will be deserialized into the unknown fields. Once the
  message is serialized again, the unknown fields will be serialized too, so
  this data will not be lost. When this feature is disabled, unknown fields
  are dropped.

- Deprecated `Message[T]`. Up to 0.10.x, generated messages would extend
  `GeneratedMessage with Message[T]`. The additional `with Message[T]` added
  noise to the code and did not provide much value. In 0.10.x, `Message[T]` is
  a type-alias to `Any` for source compatibility.

- BREAKING CHANGE: in earlier version, constructor parameters for oneofs were
  always generated after all the regular fields. From this version of ScalaPB,
  oneofs are generated in the position that matches their index in the proto
  file.

- BREAKING CHANGE: Enums case objects now extend a sealed abstract base class
  instead of a sealed trait for improved performance. This breaks binary
  compatibility for the generated code. Source should remained compatible.

- Strings are now parsed using CodedInputStream.readStringRequireUtf8 instead
  of CodedInputStream.readString (following protobuf-java)

- Update to protobuf 3.11.4

- Drop Scala 2.11 and sbt 0.13.x support. Scala 2.11 is EOL and no longer able
  to compile protocol buffers generated by protoc 3.11.x in mixed Scala mode.

- Added `aux_message_options`, `aux_field_options` and `aux_enum_options`
  file-level options to enable providing ScalaPB options to protos that can't
  be edited to include ScalaPB-specific settings (#737).

- Added `bytes_type` file-level options to modify the Scala type generated for
  protobuf's `bytes` type (#746).

- Updated to Scala.js 1.0.0

- ScalaPB runtime now depends on scala-collection-compat and is warning-free
  for both 2.12 and 2.13.

- SparkSQL-scalapb: removed support for directly reading and writing Parquet file (outside Spark). It is still possible to load and save protobuf to parquet through converting them to dataframes in Spark. Reason of removal is what I perceive as low adoption due to getting no feedback. If you were relying on the ability to directly load and save protobufs to/from parquet, please let us know by filing a ticket.

## [0.9.8](https://github.com/scalapb/ScalaPB/compare/v0.9.8)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.9.7...v0.9.8)
- Fix compatibility with sbt 1.4.x (#777)

## [0.9.7](https://github.com/scalapb/ScalaPB/compare/v0.9.7)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.9.4...v0.9.7)
- Support custom types in generated RPCs (#697)
- Support enum_strip_prefix (#730)
- Improve name conflicts collision between oneofs, fields and packages (#782)

## [0.9.4](https://github.com/scalapb/ScalaPB/compare/v0.9.5)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.9.4...v0.9.5)
- Fix a couple of cases of oneof naming conflicts. (#695)

## [0.9.4](https://github.com/scalapb/ScalaPB/compare/v0.9.4)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.9.1...v0.9.4)
- Add option for generating enum values in CamelCase (#675)
- Add option to specify names for enum values (#675)
- Rename standalone code generators from `protoc-gen-scalapb` to `protoc-gen-scala`
- Native protoc-gen-scala binary for Linux is now statically linked.

## [0.9.1](https://github.com/scalapb/ScalaPB/compare/v0.9.1)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.9.0...v0.9.1)
- It is now possible to specify no_box at the message-level.
- We now release to github native binary version of ScalaPB codegen.
- Updated to Scala 2.12.10
- Added `NonEmpty` marker trait to non-empty cases of sealed oneofs. A new
  method `asNonEmpty` returns an `Option[NonEmpty]`. See #633
- Added `no_default_values_in_constructor` to inhibit generation of default
  values in case class constructors.
- Update to protobuf 3.8.0

## [0.9.0](https://github.com/scalapb/ScalaPB/compare/v0.9.0)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.8.4...v0.9.0)
- Experimental support for Scala 2.13.0. Notes:
  - ScalaPB now generates `scala.Seq` by default for repeated
    fields (previously was `scala.collection.Seq`). This ensures usage of
    immutable Seqs on Scala 2.13 without breaking compatibility for old code.
  - The generated code compiles for Scala 2.13 and older version, so deprecation warnings
    are expected when compiling for 2.13. In a future release, we may have a
    generator that generates code that compiles cleanly, but only for 2.13.
- ScalaPB code generator is published to maven as a plugin. This enabled
  Gradle and Maven protobuf plugins to download and invoke ScalaPB.
- Dropped support for Scala 2.10.
- Custom collections are now required to provide an `iterator` method that
  returns an `Iterator[A]` over their elements.
- Temporarily dropped support for Scala Native (pending on fastparse2 support)
- Bumped fastparse to 2.1.0 (which is binary-incompatible with fastparse 1)
- Add support for custom map types (#410)
- Upgrade to protobuf 3.7.1
- Removed deprecated com.trueaccord symbols
- Added `scalapb.GeneratedSealedOneof` as a base trait for all sealed oneofs.
- Added `sealed_oneof_extends` option to allow adding base classes for sealed oneofs.
- Add support for services & methods comments (scaladoc & runtime inspection)

## [0.8.4](https://github.com/scalapb/ScalaPB/compare/v0.8.4)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.8.3...v0.8.4)
- Add `scalaName` to FieldDescriptor to make the name of the Scala field
  accessible from the descriptor. This is done by updating
  `scalapb.FieldOptions` in the descriptor we serialize in the proto file
  companion object.
- Add `of` method to companion object to enable construction of case classes
  without having default values.

## [0.8.3](https://github.com/scalapb/ScalaPB/compare/v0.8.3)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.8.2...v0.8.3)
- Add `retain_source_code_info` setting for accessing source code information through
  the `location` accessor on descriptors.
- Add option to not generate lenses.

## [0.8.2](https://github.com/scalapb/ScalaPB/compare/v0.8.2)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.8.0...v0.8.2)
- Experimental support for [providing package-level options through a proto file](https://scalapb.github.io/customizations.html#package-scoped-options)

## [v0.8.0](https://github.com/scalapb/ScalaPB/compare/v0.8.0) (2018-09-23)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.7.4...v0.8.0)
- Build with protobuf 3.6.0
- ScalaPBC uses 3.6.0 by default
- Optimize cases where custom mappers are called twice during serialization
- Added file-level option to set the proto object name (#412)
- Implementation of [sealed oneofs](https://scalapb.github.io/sealed-oneofs.html)
- Added FieldMaskUtil (Thanks @xuwei-k)

## [v0.7.4](https://github.com/scalapb/ScalaPB/compare/v0.7.4) (2018-05-01)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.6.7...v0.7.4)

Note:
- Maven artifacts are now published under a new group id: com.thesamet.scalapb
- Classes were renamed from com.trueaccord.scalapb.X to scalapb.X

Changes:
- Update to protobuf-java 3.5.0
- Added file-level option to preserve unknown fields in generated classes.
- ScalaPBC can run custom ProtocGenerator if they are available on the class
  path.
- Added type member ValueType to GeneratedOneof that makes it easy to write
  type classes that deal with Oneofs.
- Add support for Scala annotations on field-level, and companion object level.
- It is now possible to specify additional traits for oneofs.
- #235: toString is now not overridden by ScalaPB and does not generate the
  default TextFormat. To get the text format, use toProtoString. See issue #235
  for how to achieve a backwards-compatible behavior.
- Added support for Scala Native.
- `singleLineToString` has been renamed to `singleLineToProtoString`. If you are
  using ScalaPBC, the new flag name is `single_line_to_proto_string`.
- Unrecognized enum and one-of case classes are marked `final`.
- `withX` methods convert `null` to `None` for options.

## [v0.6.7](https://github.com/scalapb/ScalaPB/compare/v0.6.6) (2017-11-23)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.6.0...v0.6.7)
- GeneratedMessageCompanion: add `nestedMessagesCompanions` and Add
- Added base class GeneratedFileObject that will be the base class for the
  singleton object generated for each proto file.
- Custom base classes for enums
- Add support for custom Scala annotations on messages.

## [v0.6.0](https://github.com/scalapb/ScalaPB/compare/v0.5.47) (2017-01-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.47...v0.6.0)
- Support for custom names for fields: https://scalapb.github.io/customizations.html#custom-names
  This enables users to get around name conflicts in the generated code.
- Add messageCompanionForFieldNumber and enumCompanionForFieldNumber to
  retrive companion objects of message and enum fields by the field number.
- Support for Scala protocol buffers.
- Primitive wrappers are now the default.
- `scalapb.proto`: Add no_primitive_wrappers, deprecate `primitive_wrappers`.
- Add support for custom collection types (representation of `repeated`
  in Scala can be almost any Scala collection.
- Add support for custom types for keys and values of map<>.
- Added support for extensions (Scala.js compatible!).

BREAKING CHANGE:
- Custom options now work on both JVM and Scala.js. To migrate, remove
  imports of `com.trueaccord.scalapb.Implicits` and use the new
  `scalaDescriptor` whenever `descriptor` or `javaDescriptor` have been used.

## [v0.5.47](https://github.com/scalapb/ScalaPB/compare/v0.5.35) (2017-01-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.43...v0.5.47)
- GeneratedMessageCompanion.descriptor is deprecated in favor of
  `javaDescriptor`
- Added GeneratedMessage.toByteString

## [v0.5.43](https://github.com/scalapb/ScalaPB/compare/v0.5.35) (2016-09-27)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.35...v0.5.43)
- We are switching from sbt-scalapb to sbt-protoc. Migration instructions are
  at http://scalapb.github.io/migrating.html

- Breaking change: The outer class of a GRPC service used to extend
  `ServiceCompanion`. The trait `ServiceCompanion` has been changed to a
  type-class `ServiceCompanion[A]` where `A` is some service, and is now
  used as a companion object for the service itself `A. This allows
  you to implicitly obtain the companion object given a service `T`:

      implicity[ServiceCompanion[T]].descriptor

  Fixes #154.

- Add GeneratedEnum.fromName that gives an Option[EnumValue] given a name
  string (#160)
- Required fields must be provided explicitly to constructor.
- Grpc java updated to 1.0.0

## [v0.5.35](https://github.com/scalapb/ScalaPB/compare/v0.5.35) (2016-07-31)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.34...v0.5.35)
- Update to protobuf 3.0.0
- Upgrade grpc-java to 1.0.0-pre1
- Scaladoc is automatically generated for case classes based on comments in the proto.

## [v0.5.34](https://github.com/scalapb/ScalaPB/compare/v0.5.34) (2016-07-15)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.29...v0.5.34)

- Include Scala versions of well-known types, including basic support for Any.
- Bugfix: package names with reserved words (`type`, `val`) no longer use
  backticks in the directory name.
- Fix support for additional reserved keywords.
- Bufgix: support for well-known types in conjunction with flat_package.

[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.29...HEAD)

**Closed issues:**

- proto3 & grpc [\#127](https://github.com/scalapb/ScalaPB/issues/127)
- PB.grpc does not exist [\#125](https://github.com/scalapb/ScalaPB/issues/125)
- Publish 0.5.28 [\#124](https://github.com/scalapb/ScalaPB/issues/124)

## [v0.5.29](https://github.com/scalapb/ScalaPB/compare/v0.5.29) (2016-06-06)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.28...v0.5.29)

**Closed issues:**

- Remove usages of deprecated methods from CodedOutputStream [\#123](https://github.com/scalapb/ScalaPB/issues/123)
- \[0.5.27 regression\] serializedSize does not work with java serialization [\#121](https://github.com/scalapb/ScalaPB/issues/121)

**Merged pull requests:**

- fixes \#121: java serialization breaks serializedSize [\#122](https://github.com/scalapb/ScalaPB/pull/122) ([eiennohito](https://github.com/eiennohito))

## [v0.5.28](https://github.com/scalapb/ScalaPB/compare/v0.5.28) (2016-05-31)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.27...v0.5.28)

## [v0.5.27](https://github.com/scalapb/ScalaPB/compare/v0.5.27) (2016-05-30)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.26...v0.5.27)

**Closed issues:**

- ScalaPB + Intellij [\#120](https://github.com/scalapb/ScalaPB/issues/120)
- sbt gives error: scalapb.sbt:1: error: object trueaccord is not a member of package com addSbtPlugin\("com.trueaccord.scalapb" % "sbt-scalapb" % com.trueaccord.scalapb.Version.sbtPluginVersion\) This happens after downloadin zipfile from github and running sbt in the e2e folder \(Fedora\) [\#117](https://github.com/scalapb/ScalaPB/issues/117)
- grpc should not depend on grpc-all [\#113](https://github.com/scalapb/ScalaPB/issues/113)
- protoc-jar version [\#111](https://github.com/scalapb/ScalaPB/issues/111)
- Make grpc service name visible in generated code [\#108](https://github.com/scalapb/ScalaPB/issues/108)

**Merged pull requests:**

- replace lazy vals in size generation with explicit lazy initialization [\#119](https://github.com/scalapb/ScalaPB/pull/119) ([eiennohito](https://github.com/eiennohito))
- update documentation for removing grpc-all [\#115](https://github.com/scalapb/ScalaPB/pull/115) ([eiennohito](https://github.com/eiennohito))
- depend not on grpc-all, but on more fine-grained dependencies [\#114](https://github.com/scalapb/ScalaPB/pull/114) ([eiennohito](https://github.com/eiennohito))
- grpc 0.14.0 [\#109](https://github.com/scalapb/ScalaPB/pull/109) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.26](https://github.com/scalapb/ScalaPB/compare/v0.5.26) (2016-04-28)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.25...v0.5.26)

**Closed issues:**

- Option for companion objects to extend specified classes/traits [\#105](https://github.com/scalapb/ScalaPB/issues/105)
- Won't decode not packed `repeated` if declared as `packed` in `.proto` and vice-versa [\#102](https://github.com/scalapb/ScalaPB/issues/102)
- default value unexpected behaviour [\#100](https://github.com/scalapb/ScalaPB/issues/100)
- Option to change repeated fields collection type from Seq to IndexedSeq [\#97](https://github.com/scalapb/ScalaPB/issues/97)

**Merged pull requests:**

- Add companion\_extends [\#107](https://github.com/scalapb/ScalaPB/pull/107) ([ngthanhtrung](https://github.com/ngthanhtrung))
- fix \#102 [\#106](https://github.com/scalapb/ScalaPB/pull/106) ([xuwei-k](https://github.com/xuwei-k))
- Use fully-qualified Option class name in code generator [\#101](https://github.com/scalapb/ScalaPB/pull/101) ([zackangelo](https://github.com/zackangelo))

## [v0.5.25](https://github.com/scalapb/ScalaPB/compare/v0.5.25) (2016-04-12)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.24...v0.5.25)

**Closed issues:**

- CameCase issue [\#94](https://github.com/scalapb/ScalaPB/issues/94)
- multi-lined toString [\#82](https://github.com/scalapb/ScalaPB/issues/82)
- Support for sealed traits [\#67](https://github.com/scalapb/ScalaPB/issues/67)
- Support for options is missing. [\#65](https://github.com/scalapb/ScalaPB/issues/65)
- Add support to convert messages to Json [\#62](https://github.com/scalapb/ScalaPB/issues/62)

## [v0.5.24](https://github.com/scalapb/ScalaPB/compare/v0.5.24) (2016-04-01)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.23...v0.5.24)

**Closed issues:**

- scala.js link fail on sbt-scalapb `0.5.23` [\#98](https://github.com/scalapb/ScalaPB/issues/98)
- Incompatibility between enums and Spark SQL [\#87](https://github.com/scalapb/ScalaPB/issues/87)
- excludeFilter doesn't work [\#24](https://github.com/scalapb/ScalaPB/issues/24)

## [v0.5.23](https://github.com/scalapb/ScalaPB/compare/v0.5.23) (2016-03-28)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.22...v0.5.23)

**Merged pull requests:**

- add scalacOptions for Scala.js source map [\#95](https://github.com/scalapb/ScalaPB/pull/95) ([xuwei-k](https://github.com/xuwei-k))
- change method descriptor from private to public [\#93](https://github.com/scalapb/ScalaPB/pull/93) ([matsu-chara](https://github.com/matsu-chara))

## [v0.5.22](https://github.com/scalapb/ScalaPB/compare/v0.5.22) (2016-03-16)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.21...v0.5.22)

**Closed issues:**

- scalapbc command line tool does not generate gRPC scala code. [\#91](https://github.com/scalapb/ScalaPB/issues/91)
- Issue:value enablePlugins is not a member of sbt.Project possible cause [\#89](https://github.com/scalapb/ScalaPB/issues/89)
- Protobuf 2.x support [\#88](https://github.com/scalapb/ScalaPB/issues/88)
- Compilation fails due to attempt to write two files with the same name [\#86](https://github.com/scalapb/ScalaPB/issues/86)
- Need some details for implementing grpc [\#85](https://github.com/scalapb/ScalaPB/issues/85)
- Compilation not working. [\#84](https://github.com/scalapb/ScalaPB/issues/84)
- Getting 'protoc not found' error [\#83](https://github.com/scalapb/ScalaPB/issues/83)
- support grpc-java 0.13 [\#81](https://github.com/scalapb/ScalaPB/issues/81)
- Release 0.5.20 artifact [\#78](https://github.com/scalapb/ScalaPB/issues/78)

**Merged pull requests:**

- update grpc-java 0.13.2 [\#92](https://github.com/scalapb/ScalaPB/pull/92) ([xuwei-k](https://github.com/xuwei-k))
- update grpc 0.13.1 [\#90](https://github.com/scalapb/ScalaPB/pull/90) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.21](https://github.com/scalapb/ScalaPB/compare/v0.5.21) (2016-01-19)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.20...v0.5.21)

**Closed issues:**

- Option PB.sourceDirectories is not available in 0.4.21 [\#76](https://github.com/scalapb/ScalaPB/issues/76)

**Merged pull requests:**

- fix \#76 [\#79](https://github.com/scalapb/ScalaPB/pull/79) ([xuwei-k](https://github.com/xuwei-k))
- Add implicit value for the companion object of enums [\#77](https://github.com/scalapb/ScalaPB/pull/77) ([sebastienrainville](https://github.com/sebastienrainville))

## [v0.5.20](https://github.com/scalapb/ScalaPB/compare/v0.5.20) (2016-01-11)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.21...v0.5.20)

**Closed issues:**

- Unsupported major.minor version 52.0 [\#75](https://github.com/scalapb/ScalaPB/issues/75)
- broken sbt build due to sbt-protobuf dependency [\#74](https://github.com/scalapb/ScalaPB/issues/74)
- Unnecessary long filename for InternalFields\_xxx files [\#73](https://github.com/scalapb/ScalaPB/issues/73)

## [v0.4.21](https://github.com/scalapb/ScalaPB/compare/v0.4.21) (2016-01-08)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.19...v0.4.21)

**Closed issues:**

- gRPC: Should not call onComplete after onError  [\#71](https://github.com/scalapb/ScalaPB/issues/71)

**Merged pull requests:**

- Fix double stream close, don't call onComplete after onError [\#72](https://github.com/scalapb/ScalaPB/pull/72) ([zackangelo](https://github.com/zackangelo))
- Add a Gitter chat badge to README.md [\#69](https://github.com/scalapb/ScalaPB/pull/69) ([gitter-badger](https://github.com/gitter-badger))
- update protobuf-java 3.0.0-beta-2 [\#68](https://github.com/scalapb/ScalaPB/pull/68) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.19](https://github.com/scalapb/ScalaPB/compare/v0.5.19) (2016-01-02)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.18...v0.5.19)

**Closed issues:**

- GeneratedMessageCompanion\#parseFrom always successful even for incompatible types [\#66](https://github.com/scalapb/ScalaPB/issues/66)
- Is there any plan to support GRPC? [\#44](https://github.com/scalapb/ScalaPB/issues/44)
- ScalaJs compatibility [\#31](https://github.com/scalapb/ScalaPB/issues/31)

**Merged pull requests:**

- use fully qualified name. s/Any/scala.Any [\#63](https://github.com/scalapb/ScalaPB/pull/63) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.18](https://github.com/scalapb/ScalaPB/compare/v0.5.18) (2015-12-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.17...v0.5.18)

## [v0.5.17](https://github.com/scalapb/ScalaPB/compare/v0.5.17) (2015-12-05)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.16...v0.5.17)

**Merged pull requests:**

- remove `javaConversions = true` when grpc [\#60](https://github.com/scalapb/ScalaPB/pull/60) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.16](https://github.com/scalapb/ScalaPB/compare/v0.5.16) (2015-12-01)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.15...v0.5.16)

**Closed issues:**

- e2e test fail [\#55](https://github.com/scalapb/ScalaPB/issues/55)

**Merged pull requests:**

- GeneratedEnum and GeneratedOneof extends Product [\#59](https://github.com/scalapb/ScalaPB/pull/59) ([xuwei-k](https://github.com/xuwei-k))
- fix Encoding bug [\#57](https://github.com/scalapb/ScalaPB/pull/57) ([xuwei-k](https://github.com/xuwei-k))
- optimize com.trueaccord.scalapb.Encoding [\#56](https://github.com/scalapb/ScalaPB/pull/56) ([xuwei-k](https://github.com/xuwei-k))

## [v0.5.15](https://github.com/scalapb/ScalaPB/compare/v0.5.15) (2015-11-16)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.20...v0.5.15)

**Closed issues:**

- Add an option to wrap primitives to Option\[\] [\#54](https://github.com/scalapb/ScalaPB/issues/54)
- Case class and object conflict [\#47](https://github.com/scalapb/ScalaPB/issues/47)
- Maps in proto2 files [\#43](https://github.com/scalapb/ScalaPB/issues/43)
- use printToUnicodeString instead of printToString in generated toString [\#41](https://github.com/scalapb/ScalaPB/issues/41)
- Add an option not to wrap some values in Options when generating Scala values [\#40](https://github.com/scalapb/ScalaPB/issues/40)
- Compile for Scala 2.12 M1 [\#38](https://github.com/scalapb/ScalaPB/issues/38)

**Merged pull requests:**

- target java7 [\#53](https://github.com/scalapb/ScalaPB/pull/53) ([xuwei-k](https://github.com/xuwei-k))
- Target JDK 1.7 [\#52](https://github.com/scalapb/ScalaPB/pull/52) ([plaflamme](https://github.com/plaflamme))
- support java\_multiple\_files [\#51](https://github.com/scalapb/ScalaPB/pull/51) ([xuwei-k](https://github.com/xuwei-k))
- "macro" is a reserved word since Scala 2.11 [\#50](https://github.com/scalapb/ScalaPB/pull/50) ([xuwei-k](https://github.com/xuwei-k))
- call asSymbol in fullJavaName [\#49](https://github.com/scalapb/ScalaPB/pull/49) ([xuwei-k](https://github.com/xuwei-k))
- remove unused local variables [\#48](https://github.com/scalapb/ScalaPB/pull/48) ([xuwei-k](https://github.com/xuwei-k))
- fix typo [\#46](https://github.com/scalapb/ScalaPB/pull/46) ([xuwei-k](https://github.com/xuwei-k))
- update dependencies [\#45](https://github.com/scalapb/ScalaPB/pull/45) ([xuwei-k](https://github.com/xuwei-k))
- make generated toString use printToUnicodeString [\#42](https://github.com/scalapb/ScalaPB/pull/42) ([eiennohito](https://github.com/eiennohito))

## [v0.4.20](https://github.com/scalapb/ScalaPB/compare/v0.4.20) (2015-09-05)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.14...v0.4.20)

## [v0.5.14](https://github.com/scalapb/ScalaPB/compare/v0.5.14) (2015-09-05)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.19...v0.5.14)

**Closed issues:**

- Output directory conflicts with IntelliJ [\#37](https://github.com/scalapb/ScalaPB/issues/37)
- 0.5.9 has sbt generated classes in the packaged scalapb-runtime jar [\#35](https://github.com/scalapb/ScalaPB/issues/35)
- Make it possible to run ScalaPB outside SBT [\#17](https://github.com/scalapb/ScalaPB/issues/17)
- Support for enums with unknown values [\#11](https://github.com/scalapb/ScalaPB/issues/11)

## [v0.4.19](https://github.com/scalapb/ScalaPB/compare/v0.4.19) (2015-08-23)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.18...v0.4.19)

## [v0.4.18](https://github.com/scalapb/ScalaPB/compare/v0.4.18) (2015-08-23)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.17...v0.4.18)

## [v0.4.17](https://github.com/scalapb/ScalaPB/compare/v0.4.17) (2015-08-23)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.13...v0.4.17)

## [v0.5.13](https://github.com/scalapb/ScalaPB/compare/v0.5.13) (2015-08-22)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.12...v0.5.13)

## [v0.5.12](https://github.com/scalapb/ScalaPB/compare/v0.5.12) (2015-08-22)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.11...v0.5.12)

## [v0.5.11](https://github.com/scalapb/ScalaPB/compare/v0.5.11) (2015-08-22)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.16...v0.5.11)

**Closed issues:**

- Why are option fields with default value converted to Option\[..\] ? [\#36](https://github.com/scalapb/ScalaPB/issues/36)
- ScalaPB uses javax, which is not available on Android [\#34](https://github.com/scalapb/ScalaPB/issues/34)
- ScalaPB jar includes google proto files [\#33](https://github.com/scalapb/ScalaPB/issues/33)
- In proto3, add support for file name equals message name [\#26](https://github.com/scalapb/ScalaPB/issues/26)

## [v0.4.16](https://github.com/scalapb/ScalaPB/compare/v0.4.16) (2015-08-18)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.10...v0.4.16)

## [v0.5.10](https://github.com/scalapb/ScalaPB/compare/v0.5.10) (2015-08-18)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.9...v0.5.10)

**Closed issues:**

- import does not work correctly [\#32](https://github.com/scalapb/ScalaPB/issues/32)
- Protosb descriptor isn't 2.x compatible [\#28](https://github.com/scalapb/ScalaPB/issues/28)
- javaConversions Doesnt work [\#19](https://github.com/scalapb/ScalaPB/issues/19)

## [v0.5.9](https://github.com/scalapb/ScalaPB/compare/v0.5.9) (2015-06-18)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.8...v0.5.9)

## [v0.5.8](https://github.com/scalapb/ScalaPB/compare/v0.5.8) (2015-06-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.7...v0.5.8)

## [v0.5.7](https://github.com/scalapb/ScalaPB/compare/v0.5.7) (2015-06-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.6...v0.5.7)

## [v0.5.6](https://github.com/scalapb/ScalaPB/compare/v0.5.6) (2015-06-06)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.5...v0.5.6)

## [v0.5.5](https://github.com/scalapb/ScalaPB/compare/v0.5.5) (2015-06-04)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.15...v0.5.5)

**Closed issues:**

- Support for hinted deserialization? [\#29](https://github.com/scalapb/ScalaPB/issues/29)

## [v0.4.15](https://github.com/scalapb/ScalaPB/compare/v0.4.15) (2015-05-31)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.1...v0.4.15)

## [v0.5.1](https://github.com/scalapb/ScalaPB/compare/v0.5.1) (2015-05-31)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.14...v0.5.1)

**Closed issues:**

- akka serialization and scalapb [\#27](https://github.com/scalapb/ScalaPB/issues/27)
- Make serializedSize field @transient [\#25](https://github.com/scalapb/ScalaPB/issues/25)

## [v0.4.14](https://github.com/scalapb/ScalaPB/compare/v0.4.14) (2015-05-16)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.13...v0.4.14)

## [v0.4.13](https://github.com/scalapb/ScalaPB/compare/v0.4.13) (2015-05-16)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.5.0...v0.4.13)

**Merged pull requests:**

- build: upgrade scala versions [\#23](https://github.com/scalapb/ScalaPB/pull/23) ([ahjohannessen](https://github.com/ahjohannessen))

## [v0.5.0](https://github.com/scalapb/ScalaPB/compare/v0.5.0) (2015-05-08)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.12...v0.5.0)

## [v0.4.12](https://github.com/scalapb/ScalaPB/compare/v0.4.12) (2015-05-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.11...v0.4.12)

**Closed issues:**

- ScalaPB generates invalid class names if the original proto filename contains dot [\#22](https://github.com/scalapb/ScalaPB/issues/22)

## [v0.4.11](https://github.com/scalapb/ScalaPB/compare/v0.4.11) (2015-05-07)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.10...v0.4.11)

**Closed issues:**

- doesn't respect the protoc setting of sbt-protobuf [\#21](https://github.com/scalapb/ScalaPB/issues/21)

## [v0.4.10](https://github.com/scalapb/ScalaPB/compare/v0.4.10) (2015-05-06)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.9...v0.4.10)

**Closed issues:**

- Typo on website [\#20](https://github.com/scalapb/ScalaPB/issues/20)
- oneof not working [\#18](https://github.com/scalapb/ScalaPB/issues/18)
- Windows support? [\#13](https://github.com/scalapb/ScalaPB/issues/13)

## [v0.4.9](https://github.com/scalapb/ScalaPB/compare/v0.4.9) (2015-04-05)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.8...v0.4.9)

## [v0.4.8](https://github.com/scalapb/ScalaPB/compare/v0.4.8) (2015-03-28)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.7...v0.4.8)

**Closed issues:**

- containingOneOfName field does not give the name [\#15](https://github.com/scalapb/ScalaPB/issues/15)
- Support for generation of marker types [\#14](https://github.com/scalapb/ScalaPB/issues/14)
- Allow oneofs with reserved named like 'type' [\#10](https://github.com/scalapb/ScalaPB/issues/10)
- Add support for packed enums [\#9](https://github.com/scalapb/ScalaPB/issues/9)

**Merged pull requests:**

- Fix generator to correctly provide the oneof name [\#16](https://github.com/scalapb/ScalaPB/pull/16) ([henrymai](https://github.com/henrymai))
- Fix code generator for double and float fields with \[default=nan\] [\#12](https://github.com/scalapb/ScalaPB/pull/12) ([chrischamberlin](https://github.com/chrischamberlin))

## [v0.4.7](https://github.com/scalapb/ScalaPB/compare/v0.4.7) (2015-02-09)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.6...v0.4.7)

## [v0.4.6](https://github.com/scalapb/ScalaPB/compare/v0.4.6) (2015-02-09)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.5...v0.4.6)

## [v0.4.5](https://github.com/scalapb/ScalaPB/compare/v0.4.5) (2015-02-09)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.4...v0.4.5)

## [v0.4.4](https://github.com/scalapb/ScalaPB/compare/v0.4.4) (2015-01-24)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.3...v0.4.4)

## [v0.4.3](https://github.com/scalapb/ScalaPB/compare/v0.4.3) (2015-01-24)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.2...v0.4.3)

## [v0.4.2](https://github.com/scalapb/ScalaPB/compare/v0.4.2) (2015-01-21)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.1...v0.4.2)

**Closed issues:**

- Should use declared package name [\#8](https://github.com/scalapb/ScalaPB/issues/8)
- Fields cannot be named `get` or `tag` [\#7](https://github.com/scalapb/ScalaPB/issues/7)
- protoc-gen-scala: program not found or is not executable [\#5](https://github.com/scalapb/ScalaPB/issues/5)
- Value classes for primitive single-element message? [\#4](https://github.com/scalapb/ScalaPB/issues/4)

## [v0.4.1](https://github.com/scalapb/ScalaPB/compare/v0.4.1) (2015-01-01)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.4.0...v0.4.1)

**Closed issues:**

- writeDelimitedTo missing? [\#6](https://github.com/scalapb/ScalaPB/issues/6)
- Support for Extensions? [\#3](https://github.com/scalapb/ScalaPB/issues/3)
- For oneOfs add isDefined [\#2](https://github.com/scalapb/ScalaPB/issues/2)

## [v0.4.0](https://github.com/scalapb/ScalaPB/compare/v0.4.0) (2014-11-26)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.3.4...v0.4.0)

## [v0.3.4](https://github.com/scalapb/ScalaPB/compare/v0.3.4) (2014-11-25)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.3.3...v0.3.4)

## [v0.3.3](https://github.com/scalapb/ScalaPB/compare/v0.3.3) (2014-11-24)
[Full Changelog](https://github.com/scalapb/ScalaPB/compare/v0.3.2...v0.3.3)

## [v0.3.2](https://github.com/scalapb/ScalaPB/compare/v0.3.2) (2014-11-23)
**Merged pull requests:**

- Make conversions implicit: to/fromJavaProto, to/fromJavaValue. [\#1](https://github.com/scalapb/ScalaPB/pull/1) ([imikushin](https://github.com/imikushin))



\* *This Change Log was automatically generated by [github_changelog_generator](https://github.com/skywinder/Github-Changelog-Generator)*
