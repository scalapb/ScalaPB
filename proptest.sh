#!/usr/bin/env sh
set -e
sbt -J-Xmx5500M ++$TRAVIS_SCALA_VERSION test
