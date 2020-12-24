#!/usr/bin/env sh
set -e
exit 0
SCALA_VERSION=${SCALA_VERSION:-2.13.1}

sbt ++$SCALA_VERSION \
grpcRuntime/mimaReportBinaryIssues \
lensesJVM/mimaReportBinaryIssues \
runtimeJVM/mimaReportBinaryIssues
