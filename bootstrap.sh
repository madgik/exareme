#!/usr/bin/env bash

echo `(hostname --ip-address)` > /root/exareme/etc/exareme/master
echo "" > /root/exareme/etc/exareme/workers


mkdir -p  /tmp/demo/db/
#/bin/bash
./bin/exareme-admin.sh --start --local && tail -f /tmp/exareme/var/log/exareme-*.log
