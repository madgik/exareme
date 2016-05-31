#!/usr/bin/env bash


./bin/exareme-admin.sh --start --local && tail -f /tmp/exareme/var/log/exareme-*.log
