#!/usr/bin/env bash
set -e

which jekyll

# Workaround for https://github.com/sbt/sbt-ghpages/issues/46
sbt docs/makeMicrosite docs/ghpagesSynchLocal docs/publishMicrosite
