#!/usr/bin/env bash

mkdir -p  /tmp/demo/db/
./bin/exareme-admin.sh --start --local && tail -f /tmp/exareme/var/log/exareme-*.log
