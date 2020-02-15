#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-${TRAVIS_SCALA_VERSION:-2.12.10}}
sbt ++$SCALA_VERSION e2e/clean e2eNoJava/clean e2e/test e2eNoJava/test
