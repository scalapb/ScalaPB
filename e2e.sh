#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.11.11}}

sbt ++2.10.6 compilerPlugin/publishLocal createVersionFile \
    ++$SCALA_VERSION runtimeJVM/publishLocal grpcRuntime/publishLocal
cd e2e
sbt ++$SCALA_VERSION clean test

