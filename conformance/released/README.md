# Release-pair verification

This standalone build generates and compiles the conformance testee using artifact
coordinates only. It does not depend on the parent ScalaPB runtime/compiler projects.
Defaults are ScalaPB 1.0.0-alpha.7 and scalapb-json4s 1.0.0-alpha.4.

From this directory:

```sh
sbt 'test; assembly'
cd ../..
CONFORMANCE_JAR="$PWD/conformance/released/target/scala-2.13/conformance.jar" \
  CONFORMANCE_OUTPUT_DIR=target/released-conformance conformance/run.sh
```

The runner enables recommended tests and the maximum supported stable edition,
2026. The pinned v36.2 runner supplies actual 2023 and migration fixtures; higher
edition switches do not add 2024/2026 fixtures. This build also compiles and runs
the actual Edition 2024/2026 e2e-json fixtures against the artifact pair, alongside
the lazy-string JSON tests. The parent build has additional Java-interoperability
suites. Expected failures and unstable skips remain explicit.

Before publication, bootstrap the pair by locally publishing ScalaPB lenses and
runtime for 2.12/2.13/3 and compilerplugin for 2.12, with
`set every version := "1.0.0-alpha.7"`. Then run json4s's `+test`,
`+mimaReportBinaryIssues` and `+publishLocal` with version `1.0.0-alpha.4`.
Neither operation publishes remotely. Version overrides are `-Dscalapb.version`
and `-Dscalapb.json4s.version`; the latter also controls the parent integration build.

After publication, run the standalone build in a clean checkout/cache with a
repository configuration containing only Maven Central (no local Ivy or snapshots):

```ini
[repositories]
central: https://repo.maven.apache.org/maven2
```

Pass `-Dsbt.override.build.repos=true -Dsbt.repository.config=/path/to/repositories`
to sbt and run `clean; test; assembly`. Use an empty Coursier cache for this final
verification. This distinguishes the downloaded release from locally built
candidates with the same version. Retain the resolved dependency tree, runner log
and summary.tsv as release evidence.

`./conformance/released/verify.sh` automates the Central-only check, records build
and dependency-tree output, and runs both conformance suites. Its defaults are the
release pair above; override `SCALAPB_RELEASE_VERSION` and `JSON4S_RELEASE_VERSION`
for subsequent releases. Build/cache artifacts are kept under the report directory.
