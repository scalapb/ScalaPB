# Protobuf conformance

The runner and fixtures target [protobuf v36.2](https://github.com/protocolbuffers/protobuf/releases/tag/v36.2)
(stable release published 2026-09-17). The runner uses upstream's Bazel 8.6.0.
This does not upgrade ScalaPB's published protobuf runtime dependency.

```sh
conformance/build_test_runner.sh
sbt "conformanceJVM2_13/test; conformanceJVM2_13/assembly"
conformance/run.sh
```

The build verifies the release archive's SHA-256, uses a private temporary
source directory, and defaults to two build jobs. `CONFORMANCE_BUILD_JOBS` and
`CONFORMANCE_RUNNER_DIR` override those defaults. `CONFORMANCE_OUTPUT_DIR`
selects the report directory (default `target/conformance-results`). CI caches
the versioned runner and uploads reports even on failure. Bazelisk, a C++17
compiler, curl, tar, and sha256sum are required.

## Fixture provenance and prerequisites

`src/main/protobuf/conformance.proto` comes from upstream `conformance/`;
`test_messages_proto{2,3}.proto` come from `src/google/protobuf/`.
The protocol now uses `FailureSet.test` / `TestStatus`, including failure
messages. The testee returns an empty failure set; all expected failures remain
reviewable in the checked-in lists.

`scala_proto2_options.proto` enables `proto2_closed_enums` at package scope for
the proto2 fixtures without modifying their upstream schemas. Binary parsing
therefore retains unknown enum numbers in unknown fields, including rejected map
entries, and preserves oneof selection. Proto3 enums remain open. This option
does not change the separate JSON/text enum parsing paths or extension accessors.

The proto2 fixture includes the upstream `GroupField` extension. Known and unknown
groups are supported in binary and text format. The text type registry includes
the extension payload companion so bracketed extension names can be resolved.

`upstream/editions/` preserves and compiles the unmodified upstream 2023
schema and proto2/proto3 migration fixtures. Dispatch and Any registries use
all three actual generated companions. Only the unstable edition is skipped.
The runner uses `--maximum_edition 2026`; Editions 2024 and 2026 have separate
end-to-end fixtures. `summary.tsv` reports outcomes by family and output format.

The release pair is ScalaPB `1.0.0-alpha.8` and scalapb-json4s
`1.0.0-alpha.4`, including both Java-parity fixes and Editions feature handling.
See `released/README.md` for artifact-only verification and local bootstrapping.

## Regression tests and triage

`ConformanceScalaSpec` exercises protocol dispatch and compares parsed Java
messages with ScalaPB round trips (duplicate scalars, unknown fields, mixed
packed/unpacked fields, malformed input). A closed-enum smoke test verifies that
the proto2 fixtures enable the option and dispatch successfully. Detailed enum
semantics and Java interoperability are covered by `ClosedEnumsSpec` and
`ClosedEnumsJavaSpec` in e2e.

The Java oracle is generated from the same fixtures with the project's pinned
protoc/runtime. It compares semantic
messages, not serialization order. These tests do not assert that the project's
Java runtime is v36.2; the independent C++ runner supplies the v36.2 oracle.

`PortableWireRegressionSpec` lives in runtime's shared test sources and runs
unchanged on JVM, JS, and Native. Small regressions that do not require Java or
JSON should go there; add Java comparisons to the conformance suite where useful.

```sh
sbt 'runtimeJVM2_13/testOnly scalapb.PortableWireRegressionSpec'
sbt 'runtimeJS2_13/testOnly scalapb.PortableWireRegressionSpec'
sbt 'runtimeNative2_13/testOnly scalapb.PortableWireRegressionSpec'
```

Keep exact test names in failure lists, grouped by observed limitation. Never
copy all failing tests into the lists without reviewing their diagnostics.
The runner fails on unexpected failures and stale expected failures, so fixes
in the groups, enums, UTF-8, JSON/text, and other parser work must remove their
now-passing entries. Framing and recursion-limit changes belong to their own
workstreams; this PR does not change their policies.

The remaining binary/JSON limitations are oversized tags (six cases) and
mixed-wire unknown-field ordering (two cases). The tag cases reach the project's
Java `CodedInputStream.readTag`; ordering is lost when fields of different wire
types share a field number. Text format ignores reserved names, including nested message and list values.
Certain expanded Any type-URL prefixes remain unsupported (four legacy cases).

## Java parity

The same v36.2 runner was also run against upstream's unmodified
`//conformance:conformance_java`, built from the verified v36.2 archive, with its
own upstream failure lists and `--maximum_edition 2023 --enforce_recommended`.
Comparison is by exact test name and actual runner outcome, not just membership
in Java's failure list. Of the original 148 ScalaPB failures, Java passed 110.
All 110 now pass in ScalaPB, together with six cases that also failed in Java:

| Fixed family | Enabled cases | Previously passed by Java |
| --- | ---: | ---: |
| Text field/list separators | 56 | 56 |
| Text open-enum numbers | 2 | 2 |
| JSON ignored enum collections | 8 | 8 |
| JSON well-known types | 5 | 5 |
| JSON duplicate names and aliases | 6 | 4 |
| Text float syntax and negative zero | 16 | 14 |
| Groups, extensions and reflection | 19 | 17 |
| MessageSet | 4 | 4 |
| **Total** | **116** | **110** |

All 32 remaining expected failures also fail in that official Java implementation.
This is parity for the executed proto2/proto3 suites; the explicitly skipped
Editions cases remain outside that claim. ScalaPB already passed 81 cases Java
failed before these fixes, and continues to pass those cases. The six additional
shared failures fixed here bring that count to 87.

JSON fixes live in scalapb-json4s; its conformance dependency is selected in
`project/Dependencies.scala`. The separate UTF-8 work updates
`protobuf-runtime-scala` for JS/Native; the C++ runner exercises JVM only.

## Pre-Editions baseline

After the Java-parity fixes, the full v36.2 run
(Scala 2.13/JVM) produces:

| Suite | Successes | Editions skips | Expected failures | Unexpected failures |
| --- | ---: | ---: | ---: | ---: |
| Binary/JSON | 2809 | 2832 | 8 | 0 |
| Text | 421 | 464 | 24 | 0 |

All 22 conformance unit tests pass, including Java comparisons. The original
refresh also validated three shared wire regressions on JVM, JavaScript and Native.
Expected failures are limitations, not passing conformance cases; Editions skips
are not Editions implementation coverage.

## Editions integration (2023, 2024, 2026)

The original integrated v36.2 baseline executes the three stable Editions families
(counts below precede subsequent fixes in the checked-in failure lists):

| Suite | Successes | Skips | Expected failures | Unexpected failures |
| --- | ---: | ---: | ---: | ---: |
| Binary/JSON | 5629 | 4 unstable | 16 | 0 |
| Text | 857 | 0 | 52 | 0 |

All 24 conformance unit tests pass. Exact migration failures match the legacy
oversized-tag, unknown-field-ordering, reserved-name and Any-URL limitations.
Four additional text failures use scalar extensions, for which the text registry
has no codec; message-valued extensions are supported, including DELIMITED
encoding and migrated group names. These four cases pass in upstream Java.

The fixture registry validates every occurrence of known string extensions with
VERIFY, including an invalid occurrence followed by a valid duplicate. General
ScalaPB binary parsing retains extensions as unknown fields; generated string
extension accessors now enforce the schema when interpreting a value. A bare
parse without interpreting/registering extensions does not validate their values.

The 2024 and 2026 tests compile actual edition schemas and compare Java/Scala
conversions, defaults and wire representations. Compiler tests cover naming,
visibility, option imports, structural limits and supported compatibility
settings. Shared descriptor tests run on JVM, JS and Native; end-to-end fixtures
run on JVM (Scala 2.12, 2.13 and 3) and JS. JSON tests and MiMa pass on all three
Scala versions using the combined json4s implementation.

`summary.tsv` groups the measured runner outcomes by family and output format.
Expected failures represent unresolved behavior, not passing cases. The earlier
Java-parity table above describes its original proto2/proto3 scope, not an
unqualified parity claim for these newly enabled Editions cases.

The core MiMa baseline still targets 0.11.0: it reports 178 runtime and 155
compiler compatibility diagnostics. Comparing with the conformance base's
compiled classes produces exactly the same diagnostics; this Editions series
adds none to that baseline. The JSON matrix's own MiMa checks pass.
Documentation passes mdoc and the production Docusaurus build.

## Reserved text field names

Reserved names are discarded after syntax parsing; unrelated unknown names and
malformed values remain errors. All 40 reserved-name cases (20 legacy and 20
Editions) pass. With this fix alone, the text suite reports 897 successes, 12
expected failures and no unexpected failures.
