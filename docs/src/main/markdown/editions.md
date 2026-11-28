---
title: "Protobuf Editions"
---

ScalaPB supports stable Editions 2023, 2024 and 2026. Unknown and `UNSTABLE`
editions are rejected. Regenerate your Scala sources when adopting Editions.

## Toolchain and coverage

| Edition | Minimum upstream protoc | ScalaPB coverage |
| --- | --- | --- |
| 2023 | 27.0 | Core feature semantics, groups/DELIMITED, migrated proto2/proto3 schemas, binary/JSON/text conformance |
| 2024 | 32.0 | Edition-indexed defaults, option imports, naming/visibility rules, mixed Java nesting and large enums |
| 2026 | 36.0 | Strict visibility, naming/collision rules, structure limits, removed options, compatibility overrides |

This implementation aligns its compiler, protoc, protobuf-java and checked-in
protobuf descriptors on **v36.2**. The minimum versions above describe the
upstream schema syntax, not permission to run generated Java code with an older
protobuf-java runtime. JVM, Scala.js and Scala Native share descriptor semantics;
Scala 2.12, 2.13 and 3 are covered by the corresponding build matrices.

JSON support requires the matching Editions-aware scalapb-json4s implementation.
Use ScalaPB `1.0.0-alpha.8` together with scalapb-json4s `1.0.0-alpha.4`.
Upgrade the generator and runtime together and regenerate sources.

## Feature semantics

| Feature | Behavior |
| --- | --- |
| Field presence | EXPLICIT produces presence-aware fields; IMPLICIT omits default values; LEGACY_REQUIRED checks required fields |
| Enum type | Open enums retain unknown numbers; closed binary enum values remain in unknown fields; JSON/text consult resolved openness |
| Repeated encoding | PACKED and EXPANDED control serialization; parsers accept both valid encodings |
| UTF-8 | Editions apply schema VERIFY/NONE without a legacy opt-in; ScalaPB retains Java-compatible validation for map strings and Java-specific VERIFY overrides |
| Message encoding | LENGTH_PREFIXED and DELIMITED support nested, repeated, oneof and extension messages; maps remain length-prefixed |
| JSON format | ALLOW uses ProtoJSON; LEGACY_BEST_EFFORT retains best-effort mapping for schemas with ambiguous JSON names |

Feature settings inherit lexically and can be overridden at their supported
scopes. Enum openness comes from the defining enum, including imported types.
Group wire parsing is supplied by the separate conformance workstream. Text format preserves legacy group capitalization only for
group-like fields; other DELIMITED fields retain their field names.

The `enforce_naming_style`, `default_symbol_visibility` and `enforce_proto_limits`
features are enforced by **protoc**. Visibility controls imports between proto
files; it does not make generated Scala classes private. These features have
source retention: runtime descriptors expose defaults, but cannot reconstruct
explicit overrides stripped by protoc. Runtime feature overrides such as presence
and enum openness remain available.

Edition 2024 changes the default Java outer class name and allows different
`nest_in_file_class` policies for types in one file. ScalaPB uses protobuf's Java
naming APIs and tests conversions and descriptor references with imported types
and `large_enum`. Language-specific Java options do not generally redefine the
Scala API; Java compatibility paths use the corresponding Java accessors.

## Limits and conformance

Stable Editions fixtures execute through their own companions, rather than being
mapped to legacy proto2/proto3 types. Four unstable cases in the v36.2 runner remain
explicitly skipped. The runner's 2024/2026 switches do not supply dedicated new
fixtures, so those editions have actual `.proto` end-to-end tests in addition to
the shared semantic tests.

Editions support does **not** mean every upstream conformance case passes.
Reviewed exact failure lists record remaining limitations. Message-valued text
extensions require explicit registration of their payload companions. Scalar text extensions currently lack codecs. Binary extensions remain unknown
fields until interpreted; string accessors then enforce the schema UTF-8 policy.
The conformance testee explicitly validates all occurrences of its registered
string extensions. Required submessages retain ScalaPB's existing
per-occurrence validation behavior when merging repeated singular occurrences.
See the repository's `conformance/README.md`, failure lists and generated
`summary.tsv` for measured successes, expected failures and skips by family and
output format. Java testee failures are tracked separately from ScalaPB failures.

Upstream references: [version support](https://protobuf.dev/support/version-support/),
[feature settings](https://protobuf.dev/editions/features/), and
[implementing Editions](https://protobuf.dev/editions/implementation/).
