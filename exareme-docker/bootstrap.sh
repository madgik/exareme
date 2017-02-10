#!/usr/bin/env bash

#RAWUSERNAME="federation"
#RAWPASSWORD="federation"
#RAWHOST="rawdb"
#RAWPORT="54321"

if [ -z ${RAWUSERNAME} ]; then echo "RAWUSERNAME is unset"; exit;  fi
if [ -z ${RAWPASSWORD} ]; then echo "RAWPASSWORD is unset";exit;  fi
if [ -z ${RAWHOST} ]; then echo "RAWHOST is unset"; exit;  fi
if [ -z ${RAWPORT} ]; then echo "RAWPORT is unset";exit;  fi


echo `(hostname --ip-address)` > /root/exareme/etc/exareme/master
echo "" > /root/exareme/etc/exareme/workers

sed -i "/<raw_username>/c{ \"name\" : \"username\", \"desc\" : \"\", \"value\":\"`echo $RAWUSERNAME`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_password>/c{ \"name\" : \"password\", \"desc\" : \"\", \"value\":\"`echo $RAWPASSWORD`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_host>/c{ \"name\" : \"host\", \"desc\" : \"\", \"value\":\"`echo $RAWHOST`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_port>/c{ \"name\" : \"port\", \"desc\" : \"\", \"value\":\"`echo $RAWPORT`\" }" /root/mip-algorithms/properties.json


mkdir -p  /tmp/demo/db/

#/bin/bash

./bin/exareme-admin.sh --start --local

if [[ -e "/tmp/exareme/var/log/exareme-*.log" ]]; then

    tail -f /tmp/exareme/var/log/exareme-*.log
else
    sleep 2;
    tail -f /tmp/exareme/var/log/exareme-*.log
fi

