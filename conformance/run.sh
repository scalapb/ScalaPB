#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
runner_dir=${CONFORMANCE_RUNNER_DIR:-/tmp/conformance-test-runner}
output_dir=${CONFORMANCE_OUTPUT_DIR:-target/conformance-results}
if [[ $(cat "$runner_dir/version") != 36.2 ]]; then
  echo 'Rebuild the protobuf v36.2 runner with conformance/build_test_runner.sh' >&2
  exit 1
fi
mkdir -p "$output_dir"
# Remove only reports owned by the runner, so a successful rerun has no stale failures.
for report in unmatched expected_failure_messages succeeding_tests exceeded_max_matches \
  unexpected_failure_messages failing_tests; do
  rm -f "$output_dir/$report.txt"
done
# A timed-out JVM receives SIGQUIT; avoid writing a potentially large core dump.
ulimit -c 0
# Upstream defaults to legacy syntax; opt in so Editions skips remain visible.
"$runner_dir/conformance_test_runner" --enforce_recommended --maximum_edition 2023 \
  --failure_list conformance/failure_list_scalapb.txt \
  --text_format_failure_list conformance/text_format_failure_list_scalapb.txt \
  --output_dir "$output_dir" \
  "$@" "$(command -v java)" -Xrs -jar target/out/jvm/scala-*/conformance/conformance \
  2>&1 | tee "$output_dir/runner.log"
