#!/usr/bin/env bash
set -e
cd /tmp
export PROTOBUF_VERSION=3.19.6
export USE_BAZEL_VERSION=3.7.2
curl -L https://github.com/protocolbuffers/protobuf/archive/refs/tags/v${PROTOBUF_VERSION}.zip -o /tmp/protobuf.zip
unzip /tmp/protobuf.zip
cd protobuf-${PROTOBUF_VERSION}
bazel build conformance_test_runner
mkdir -p /tmp/conformance-test-runner
cp bazel-bin/conformance_test_runner /tmp/conformance-test-runner
