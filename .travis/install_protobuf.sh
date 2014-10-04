#!/bin/sh
set -e
wget https://protobuf.googlecode.com/files/protobuf-2.5.0.tar.gz
tar -xzvf protobuf-2.5.0.tar.gz
cd protobuf-2.5.0 && ./configure --prefix=/usr && make && sudo make install

