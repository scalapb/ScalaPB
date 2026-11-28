#!/usr/bin/env bash
set -euo pipefail
repo_root=$(cd "$(dirname "$0")/../.." && pwd)
report_dir=${CONFORMANCE_OUTPUT_DIR:-"$repo_root/target/released-conformance"}
mkdir -p "$report_dir"
report_dir=$(cd "$report_dir" && pwd)
scalapb_version=${SCALAPB_RELEASE_VERSION:-1.0.0-alpha.8}
json_version=${JSON4S_RELEASE_VERSION:-1.0.0-alpha.4}

# Never satisfy a release check from locally published candidate artifacts.
cat > "$report_dir/repositories" <<'REPOS'
[repositories]
central: https://repo.maven.apache.org/maven2
REPOS
export COURSIER_CACHE="$report_dir/coursier-cache"
(
  cd "$repo_root/conformance/released"
  sbt -Dsbt.override.build.repos=true \
    "-Dsbt.global.base=$report_dir/sbt-global" \
    "-Dsbt.ivy.home=$report_dir/ivy" \
    "-Dsbt.repository.config=$report_dir/repositories" \
    "-Dscalapb.version=$scalapb_version" \
    "-Dscalapb.json4s.version=$json_version" \
    'clean; test; assembly; dependencyTree'
) 2>&1 | tee "$report_dir/build.log"
CONFORMANCE_JAR="$repo_root/conformance/released/target/scala-2.13/conformance.jar" \
  CONFORMANCE_OUTPUT_DIR="$report_dir" "$repo_root/conformance/run.sh"
