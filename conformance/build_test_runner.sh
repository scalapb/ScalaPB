#!/usr/bin/env bash
set -euo pipefail

# Keep the runner and vendored schemas on the same release (see README.md).
PROTOBUF_VERSION=36.2
PROTOBUF_SHA256=7e7eaf580ec77f1a392ff1ab1013c13d3b04a6716f002f697f67f3c1113f0530
export USE_BAZEL_VERSION=8.6.0
runner_dir=${CONFORMANCE_RUNNER_DIR:-/tmp/conformance-test-runner}
work_dir=$(mktemp -d)
trap 'rm -rf "$work_dir"' EXIT
curl --fail --location --retry 3 \
  "https://github.com/protocolbuffers/protobuf/archive/refs/tags/v${PROTOBUF_VERSION}.tar.gz" \
  -o "$work_dir/protobuf.tar.gz"
echo "$PROTOBUF_SHA256  $work_dir/protobuf.tar.gz" | sha256sum --check
tar -xzf "$work_dir/protobuf.tar.gz" -C "$work_dir"
cd "$work_dir/protobuf-${PROTOBUF_VERSION}"
bazel --batch build --jobs="${CONFORMANCE_BUILD_JOBS:-2}" //conformance:conformance_test_runner
mkdir -p "$runner_dir"
cp bazel-bin/conformance/conformance_test_runner "$runner_dir/"
printf '%s\n' "$PROTOBUF_VERSION" > "$runner_dir/version"
