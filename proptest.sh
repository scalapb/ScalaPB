#!/usr/bin/env bash
set -e
SCALA_VERSION=${SCALA_VERSION:-2_12}

sbt \
    lensesJVM${SCALA_VERSION}/test \
    compilerPluginJVM${SCALA_VERSION}/test \
    runtimeJVM${SCALA_VERSION}/test \
    grpcRuntimeJVM${SCALA_VERSION}/test \
    proptestJVM${SCALA_VERSION}/test

if [[ $SCALA_VERSION = 2* ]]; then
    sbt \
        lensesJS${SCALA_VERSION}/test \
        runtimeJS${SCALA_VERSION}/test \
        lensesNative${SCALA_VERSION}/test \
        runtimeNative${SCALA_VERSION}/test
fi
