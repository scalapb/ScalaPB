#!/usr/bin/env sh
set -e
sbt publishLocal "project sbtPlugin" "+publishLocal"
cd e2e
sbt clean test

