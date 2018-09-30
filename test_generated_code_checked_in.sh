#!/usr/bin/env bash
# Test that all generated code is checked in.
set -e

./make_plugin_proto.sh

git diff --exit-code || { \
    echo "Generated code changed. Please run make_plugin_proto.sh."; exit 1; }

