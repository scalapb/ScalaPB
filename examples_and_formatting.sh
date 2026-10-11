#!/usr/bin/env bash
set -e
./test_generated_code_checked_in.sh
for d in examples/*; do
  (cd "$d" && sbt "test; scalafmtCheckAll; scalafmtSbtCheck")
done
(cd benchmarks && sbt "scalafmtCheckAll; scalafmtSbtCheck")
sbt -J-Xmx4500M "scalafmtCheckAll; scalafmtSbtCheck"
