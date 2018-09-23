#!/usr/bin/env bash
set -e
rvm use 2.2.8 --install --fuzzy
gem update --system
gem install sass
gem install jekyll -v 3.2.1
sbt docs/makeMicrosite docs/publishMicrosite
