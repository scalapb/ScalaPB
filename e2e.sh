#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.11.11}}

# E2E_SHADED signals we will work with the shaded version of the
# compilerplugin.
if [ "$E2E_SHADED" = "1" ]; then
  mv e2e/project/scalapb.sbt.shaded e2e/project/scalapb.sbt
fi

sbt ++2.12.6 compilerPlugin/publishLocal compilerPluginShaded/publishLocal createVersionFile \
    ++$SCALA_VERSION lensesJVM/publishLocal runtimeJVM/publishLocal grpcRuntime/publishLocal
cd e2e
sbt ++$SCALA_VERSION noJava/clean clean noJava/test test

