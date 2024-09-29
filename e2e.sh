#!/usr/bin/env bash
set -e
SCALA_VERSION=${SCALA_VERSION:-2_12}
if [[ "$SCALA_VERSION" != "2_12" ]]; then
    COMPAT_TARGET=e2eScala3SourcesJVM${SCALA_VERSION};
fi

sbt clean \
    e2eJVM$SCALA_VERSION/test \
    e2eGrpcJVM$SCALA_VERSION/test \
    e2eJS${SCALA_VERSION}/test \
    $COMPAT_TARGET
