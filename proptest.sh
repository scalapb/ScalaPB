#!/usr/bin/env sh
set -e
sbt ++$TRAVIS_SCALA_VERSION test
