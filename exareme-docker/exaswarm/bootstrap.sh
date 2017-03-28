#!/usr/bin/env bash

#RAWUSERNAME="federation"
#RAWPASSWORD="federation"
#RAWHOST="raw-ui"
#RAWPORT="5555"
#RAWENDPOINT="query-start"

if [ -z ${RAWUSERNAME} ]; then echo "RAWUSERNAME is unset"; exit;  fi
if [ -z ${RAWPASSWORD} ]; then echo "RAWPASSWORD is unset";exit;  fi
if [ -z ${RAWHOST} ]; then echo "RAWHOST is unset";exit;  fi
if [ -z ${RAWPORT} ]; then echo "RAWPORT is unset";exit;  fi
if [ -z ${RAWENDPOINT} ]; then echo "RAWENDPOINT is unset";exit;  fi
if [ -z ${RAWRESULTS} ]; then echo "RAWRESULTS is unset"; exit;  fi
if [ -z ${RAWDATAKEY} ]; then echo "RAWDATAKEY is unset";exit;  fi
if [ -z ${CONSULURL} ]; then echo "CONSULURL is unset"; exit; fi

#echo `(hostname --ip-address)` > /root/exareme/etc/exareme/master
#echo "172.17.0.3" > /root/exareme/etc/exareme/workers
#echo "172.17.0.2" > /root/exareme/etc/exareme/master

EXAREME_WORKERS_PATH="available_workers"
EXAREME_ACTIVE_WORKERS_PATH="active_workers"

sed -i "/<raw_username>/c{ \"name\" : \"username\", \"desc\" : \"\", \"value\":\"`echo $RAWUSERNAME`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_password>/c{ \"name\" : \"password\", \"desc\" : \"\", \"value\":\"`echo $RAWPASSWORD`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_host>/c{ \"name\" : \"host\", \"desc\" : \"\", \"value\":\"`echo $RAWHOST`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_port>/c{ \"name\" : \"port\", \"desc\" : \"\", \"value\":\"`echo $RAWPORT`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_endpoint>/c{ \"name\" : \"api\", \"desc\" : \"\", \"value\":\"`echo $RAWENDPOINT`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_resultsperpage>/c{ \"name\" : \"resultsPerPage\", \"desc\" : \"\", \"value\":\"`echo $RAWRESULTS`\" }," /root/mip-algorithms/properties.json
sed -i "/<raw_datakey>/c{ \"name\" : \"datakey\", \"desc\" : \"\", \"value\":\"`echo $RAWDATAKEY`\" }" /root/mip-algorithms/properties.json



mkdir -p  /tmp/demo/db/

service ssh restart 

if [[ "$MASTER_FLAG" != "master" ]]; then #this is a worker
    sleep 2
    MY_OLIP=$(/sbin/ifconfig $1 | grep "inet " | awk -F: '{print $2}' | awk '{print $1;}' | head -n 1)
    curl -X PUT -d @- $CONSULURL/v1/kv/$EXAREME_WORKERS_PATH/$MY_OLIP <<< $(hostname)
    while [[ ! -f "/tmp/exareme/var/log/exareme-*.log" ]]; do
        sleep 2 
    done 
else #this is the master
    /sbin/ifconfig $1 | grep "inet " | awk -F: '{print $2}' | awk '{print $1;}' | head -n 1 > etc/exareme/master
    WORKERS_UP=0
    while [[ $WORKERS_UP != $EXA_WORKERS_WAIT ]]; do
	    sleep 2
	    curl -s $CONSULURL/v1/kv/$EXAREME_WORKERS_PATH/?keys | jq -r '.[]' | sed "s/$EXAREME_WORKERS_PATH\///g"  \
	    | head -n $EXA_WORKERS_WAIT > etc/exareme/workers
        	WORKERS_UP=`cat etc/exareme/workers | wc -l`
            echo "Waiting for " $((EXA_WORKERS_WAIT-WORKERS_UP)) " more exareme workers..."
    done
    #curl -X DELETE  $CONSULURL/v1/kv/$EXAREME_WORKERS_PATH/?recurse
    for i in `cat etc/exareme/workers` ; do 
	    ssh -oStrictHostKeyChecking=no $i date
        curl -X PUT -d @- $CONSULURL/v1/kv/$EXAREME_ACTIVE_WORKERS_PATH/$(curl -s $CONSULURL/v1/kv/$EXAREME_WORKERS_PATH/$i?raw) <<< $i
	    curl -X DELETE $CONSULURL/v1/kv/$EXAREME_WORKERS_PATH/$i
    done
    ./bin/exareme-admin.sh --update
    sleep 3
    ./bin/exareme-admin.sh --start

fi

tail -f /tmp/exareme/var/log/exareme-*.log


