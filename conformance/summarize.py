#!/usr/bin/env python3
"""Summarize verbose upstream conformance results by fixture family and output format."""
import collections
import re
import sys
from pathlib import Path

log = Path(sys.argv[1]).read_text()
expected = {
    line.split('#', 1)[0].strip()
    for filename in sys.argv[2:]
    for line in Path(filename).read_text().splitlines()
    if line.strip() and not line.lstrip().startswith('#')
}
unexpected = set(re.findall(r'ERROR, test=([^:]+):', log))
counts = collections.defaultdict(collections.Counter)
for line in log.splitlines():
    match = re.match(r'conformance test: name=([^,]+), request=(.*)', line)
    if not match:
        continue
    name, request = match.groups()
    family = name.split('.')[1]
    output = re.search(r'requested_output_format: (\w+)', request)
    output = output.group(1) if output else 'UNSPECIFIED'
    response = request.split('response=', 1)[-1]
    status = ('skipped' if re.search(r'\bskipped:', response) else
              'unexpected' if name in unexpected else
              'expected_failure' if name in expected else 'success')
    counts[(family, output)][status] += 1
if not counts:
    raise SystemExit('No test records found; run the upstream runner with --verbose.')
print('Family\tOutput\tSuccess\tExpected failure\tUnexpected failure\tSkipped')
for (family, output), values in sorted(counts.items()):
    print('\t'.join(map(str, (family, output, values['success'],
                              values['expected_failure'], values['unexpected'], values['skipped']))))
