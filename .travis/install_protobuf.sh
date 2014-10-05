#!/bin/sh
set -e
wget https://protobuf.googlecode.com/svn/rc/protobuf-2.6.0.tar.gz
tar -xzvf protobuf-2.6.0.tar.gz
cd protobuf-2.6.0 && ./configure --prefix=/usr && make && sudo make install

