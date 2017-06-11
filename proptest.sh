#!/usr/bin/env sh
set -e
sbt -J-Xmx4500M ++$TRAVIS_SCALA_VERSION test
