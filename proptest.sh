#!/usr/bin/env sh
set -e
sbt ++2.12.8 compilerPlugin/publishLocal \
    ++$TRAVIS_SCALA_VERSION \
    lensesJVM/publishLocal runtimeJVM/publishLocal grpcRuntime/publishLocal test
