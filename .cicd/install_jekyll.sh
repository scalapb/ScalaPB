#!/usr/bin/env bash
set -e
cd .cicd/jekyll
gem install bundler:2.1.4
bundle config path vendor/bundle
bundle install --jobs 4 --retry 3 --binstubs=bin
echo "::add-path::$PWD/bin"
