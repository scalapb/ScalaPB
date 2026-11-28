#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
runner_dir=${CONFORMANCE_RUNNER_DIR:-/tmp/conformance-test-runner}
output_dir=${CONFORMANCE_OUTPUT_DIR:-target/conformance-results}
maximum_edition=${CONFORMANCE_MAXIMUM_EDITION:-2026}
testee_jars=(target/out/jvm/scala-*/conformance/conformance)
if [[ -n ${CONFORMANCE_JAR:-} ]]; then
  testee_jars=("$CONFORMANCE_JAR")
fi
if [[ ${#testee_jars[@]} != 1 || ! -f ${testee_jars[0]} ]]; then
  echo 'Build one conformance assembly or set CONFORMANCE_JAR to its path.' >&2
  exit 1
fi
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
# Run stable Editions fixtures; unstable requests remain explicitly skipped.
status=0
"$runner_dir/conformance_test_runner" --enforce_recommended --maximum_edition "$maximum_edition" --verbose \
  --failure_list conformance/failure_list_scalapb.txt \
  --text_format_failure_list conformance/text_format_failure_list_scalapb.txt \
  --output_dir "$output_dir" \
  "$@" "$(command -v java)" -Xrs -jar "${testee_jars[0]}" \
  2>&1 | tee "$output_dir/runner.log" || status=$?
python3 conformance/summarize.py "$output_dir/runner.log" \
  conformance/failure_list_scalapb.txt conformance/text_format_failure_list_scalapb.txt \
  > "$output_dir/summary.tsv" || status=1
cat "$output_dir/summary.tsv"
exit "$status"
