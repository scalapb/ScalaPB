#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-2_12}
sbt clean \
    e2eJVM$SCALA_VERSION/test \
    e2eGrpcJVM$SCALA_VERSION/test

if [[ "$SCALA_VERSION" != "0_27" ]]; then
sbt \
    e2eJS${SCALA_VERSION}/test
fi

