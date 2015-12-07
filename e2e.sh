#!/usr/bin/env sh
set -e
sbt ++2.10.6 publishLocal createVersionFile
cd e2e
sbt clean test

