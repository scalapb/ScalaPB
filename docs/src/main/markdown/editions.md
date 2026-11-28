---
title: "Protobuf Editions"
---

Protobuf Editions let you select schema behavior through feature settings, such
as whether a field tracks presence or a repeated numeric field uses packed
encoding. Each edition supplies defaults that you can override where needed.

ScalaPB supports Editions **2023, 2024 and 2026**. This guide shows how to use
Editions, how the settings affect generated Scala code, and which limitations
to keep in mind.

## Getting started

Declare an edition at the top of your `.proto` file in place of a `syntax`
declaration:

```protobuf
edition = "2023";

message Person {
  string name = 1;
  int32 age = 2;
}
```

With the default ScalaPB options, both fields have explicit presence and are
represented as `Option[String]` and `Option[Int]`. This lets you distinguish an
unset `age` from an explicitly set value of `0`.

Use ScalaPB `1.0.0-alpha.8` for both the generator and runtime. If you use
[JSON support](json.md), pair it with scalapb-json4s `1.0.0-alpha.4`.
Upgrade the generator and runtime together, then regenerate your Scala sources.
See [installation](installation.md) for build configuration.

### Choosing an edition and protoc version

| Edition | Minimum upstream protoc | Changes introduced by the edition |
| --- | --- | --- |
| 2023 | 27.0 | Feature settings for presence, enums, encoding and UTF-8 validation. |
| 2024 | 32.0 | Option imports, naming and visibility rules, and new Java naming options. |
| 2026 | 36.0 | Stricter visibility, naming and structure limits, with compatibility overrides. |

ScalaPB's toolchain uses protobuf **36.2**, including protoc, protobuf-java and
its bundled descriptors. The minimum versions in the table indicate when
upstream protoc added each edition's syntax. Generated Java code still requires
a compatible protobuf-java runtime; see the upstream
[version support guidance](https://protobuf.dev/support/version-support/).
Unknown editions and `UNSTABLE` are rejected.

When migrating an existing schema, review its feature settings before
regenerating: replacing `syntax` with `edition` can change defaults such as field
presence and enum openness. The upstream guide to
[preserving proto2 or proto3 behavior](https://protobuf.dev/editions/features/#preserving-proto2-or-proto3-behavior-in-edition-2023)
describes the corresponding overrides.

## Configuring features

Feature settings apply to the scope where you declare them and are inherited by
nested declarations. A setting on a field overrides the setting inherited from
its file or containing message, provided the feature supports that scope.

For example, make scalar presence implicit throughout a file, then enable
explicit presence for one field:

```protobuf
edition = "2023";

option features.field_presence = IMPLICIT;

message Person {
  string name = 1;
  int32 age = 2 [features.field_presence = EXPLICIT];
}
```

With the default ScalaPB options, `name` is a `String` with default value `""`,
while `age` is an `Option[Int]` with default value `None`. An unset `name` and an
explicitly set empty string are indistinguishable; `age` retains that distinction.

The main runtime features behave as follows:

| Feature | Effect in ScalaPB |
| --- | --- |
| `field_presence` | `EXPLICIT` tracks whether a field is set. `IMPLICIT` omits default values from serialization. `LEGACY_REQUIRED` requires the field to be present when parsing. |
| `enum_type` | Open enums retain unknown numbers as `Unrecognized` values. Unknown values of closed enums are stored in unknown fields during binary parsing. JSON and text parsing also respect enum openness. |
| `repeated_field_encoding` | `PACKED` and `EXPANDED` choose how repeated packable fields are serialized. Parsers accept both encodings. |
| `utf8_validation` | `VERIFY` validates strings; `NONE` permits malformed UTF-8, subject to Java-compatible overrides and map validation described below. |
| `message_encoding` | `LENGTH_PREFIXED` and `DELIMITED` support nested, repeated, oneof and extension messages. Maps always remain length-prefixed. |
| `json_format` | `ALLOW` uses ProtoJSON. `LEGACY_BEST_EFFORT` allows best-effort mapping for schemas with ambiguous JSON names. |

Enum openness is determined by the enum's definition, including when it is
imported from another file. Text format preserves legacy group capitalization
for group-like fields; other `DELIMITED` fields use their field names.

See the upstream [feature settings reference](https://protobuf.dev/editions/features/)
for all values, defaults and permitted scopes. To inspect effective settings at
runtime, see [resolved descriptor features](generic.md#editions-and-resolved-descriptor-features).

### UTF-8 validation

Editions schemas follow `features.utf8_validation` automatically; you do not need
the `use_schema_utf8_validation` option used by proto2 and proto3 schemas.
ScalaPB also applies Java-specific `VERIFY` overrides and always validates string
map keys and values.

For how nonvalidating fields handle malformed bytes, including eager and lazy
string fields, see [binary string UTF-8 validation](customizations.md#binary-string-utf-8-validation).

## Naming, visibility and Java interoperability

The `enforce_naming_style`, `default_symbol_visibility` and `enforce_proto_limits`
features are enforced by protoc. Visibility determines which declarations other
`.proto` files can import; generated Scala classes remain public.

These features have source retention, so protoc strips explicit overrides from
runtime descriptors. You can inspect their edition defaults at runtime, but
cannot recover those overrides. Runtime features such as presence and enum
openness retain their overrides.

Edition 2024 changes the default Java outer class name and allows different
`nest_in_file_class` policies for types within one file. ScalaPB uses protobuf's
Java naming APIs for conversions and descriptor references, including imported
types and `large_enum`. Java-specific options generally affect Java
interoperability rather than the generated Scala API.

## Limitations and conformance

ScalaPB tests stable Editions with binary, JSON and text conformance suites, plus
actual Edition 2024 and 2026 schemas in end-to-end tests. Descriptor semantics
are shared across JVM, Scala.js and Scala Native. Some upstream conformance cases
remain unsupported:

- Text extensions declared outside the message's file require registration of
  their declaration file (or a message companion from that file). Message-valued
  extensions also require their payload companions. See
  [extensions in text format](generated-code.md#extensions-in-text-format).
- Binary extensions remain in unknown fields until interpreted. String extension
  accessors enforce the schema's UTF-8 policy when reading a value; parsing a
  message alone does not validate every extension occurrence.
- Required submessages are validated per occurrence when parsing repeated
  occurrences of a singular message field, before all occurrences are merged.

The [conformance documentation](https://github.com/scalapb/ScalaPB/blob/master/conformance/README.md#editions-integration-2023-2024-2026)
records test coverage and remaining failures. It also explains the four skipped
unstable cases and how to inspect results by test family and output format.
