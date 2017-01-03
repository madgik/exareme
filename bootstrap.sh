#!/usr/bin/env bash

echo `(hostname --ip-address)` > /root/exareme/etc/exareme/master
echo "" > /root/exareme/etc/exareme/workers

rm /root/exareme/etc/exareme/gateway.properties

mv /root/exareme/etc/exareme/gateway_docker.properties /root/exareme/etc/exareme/gateway.properties

rm /root/exareme/lib/algorithms-dev/properties.json

mv /root/exareme/lib/algorithms-dev/properties_docker.json /root/exareme/lib/algorithms-dev/properties.json

mkdir -p  /tmp/demo/db/
#/bin/bash


./bin/exareme-admin.sh --start --local

if [[ -e "/tmp/exareme/var/log/exareme-*.log" ]]; then

    tail -f /tmp/exareme/var/log/exareme-*.log
else
    sleep 2;
    tail -f /tmp/exareme/var/log/exareme-*.log
fi


