#!/usr/bin/env sh
set -e
SCALA_VERSION=${SCALA_VERSION:-2.13.1}

sbt ++$SCALA_VERSION \
grpcRuntime/mimaReportBinaryIssues \
lensesJVM/mimaReportBinaryIssues \
runtimeJVM/mimaReportBinaryIssues \
compilerPlugin/mimaReportBinaryIssues
