#!/usr/bin/env bash
# Test that all generated code is checked in.
set -e

echo "java -jar ${HOME}/.ivy2/cache/com.github.os72/protoc-jar/jars/protoc-jar-3.5.0.jar \$@" > protoc
chmod +x protoc
PATH=${PATH}:${PWD} ./make_plugin_proto.sh
rm protoc
git diff --exit-code || { \
    echo "Generated code changed. Please run make_plugin_proto.sh."; exit 1; }

