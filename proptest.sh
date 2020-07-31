#!/usr/bin/env bash
set -e
SCALA_VERSION=${SCALA_VERSION:-2_12}

sbt \
    lensesJVM${SCALA_VERSION}/test \
    compilerPluginJVM${SCALA_VERSION}/test \
    runtimeJVM${SCALA_VERSION}/test \
    grpcRuntimeJVM${SCALA_VERSION}/test

if [[ $SCALA_VERSION = 2* ]]; then
    sbt \
        lensesJS${SCALA_VERSION}/test \
        runtimeJS${SCALA_VERSION}/test
fi

if [[ $SCALA_VERSION = 2_12 ]]; then
    sbt \
        proptestJVM${SCALA_VERSION}/test
fi
