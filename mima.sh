#!/usr/bin/env sh
set -e

exit 0

SCALA_VERSION=${SCALA_VERSION:-2_12}

sbt \
grpcRuntimeJVM${SCALA_VERSION}/mimaReportBinaryIssues \
lensesJVM${SCALA_VERSION}/mimaReportBinaryIssues \
runtimeJVM${SCALA_VERSION}/mimaReportBinaryIssues \
compilerPluginJVM${SCALA_VERSION}/mimaReportBinaryIssues
