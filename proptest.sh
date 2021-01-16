#!/usr/bin/env sh
set -e
sbt ++$SCALA_VERSION test
