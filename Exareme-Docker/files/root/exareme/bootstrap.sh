#!/usr/bin/env bash

EXAREME_ACTIVE_WORKERS_PATH="active_workers"
EXAREME_MASTER_PATH="master"
DATASETS="datasets"

stop_exareme () {
    if [ -f /tmp/exareme/var/run/*.pid ]; then
	    kill -9 $( cat /tmp/exareme/var/run/*.pid)
        rm /tmp/exareme/var/run/*.pid
        echo "Stopped."
    else
       echo "Already stopped, no action taken."
    fi
    exit 1
}

deleteKeysFromConsul () {
    DATASETS="datasets"
    if [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${DATASETS}/${NODE_NAME}?keys)" = "200" ]; then
        curl -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
    fi
    if [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${1}/${NODE_NAME}?keys)" = "200" ]; then
        curl -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME
    fi
}

# Setup signal handlers
trap term_handler SIGTERM SIGKILL

# SIGTERM-handler this funciton will be executed when the container receives the SIGTERM signal (when stopping)
term_handler () {

if [ "$MASTER_FLAG" != "master" ]; then   #worker
    echo "*******************************Stopping Worker**************************************"
    if [ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" = "passing" ];  then
        deleteKeysFromConsul "$EXAREME_ACTIVE_WORKERS_PATH"
    fi
    if [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" = "200" ]; then
        MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)
        MASTER_IP=$(curl -s $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/$(curl -s $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/?keys | jq -r '.[]' | sed "s/$EXAREME_MASTER_PATH\///g")?raw)
        curl $MASTER_IP:9091/remove/worker?IP=$MY_IP     #delete worker from master's registry
    fi
    stop_exareme
else                                      #master
    echo "*******************************Stopping Master**************************************"
    if [ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" = "passing" ];  then
        deleteKeysFromConsul "$EXAREME_MASTER_PATH"
    fi
    stop_exareme
fi
exit 0
}

if [ -z ${CONSULURL} ]; then echo "CONSULURL is unset"; exit; fi
if [ -z ${NODE_NAME} ]; then echo "NODE_NAME is unset";exit;  fi
if [ -z ${DOCKER_DATASETS_FOLDER} ]; then echo "DOCKER_DATASETS_FOLDER is unset"; exit; fi
if [ -z ${DOCKER_METADATA_FOLDER} ]; then echo "DOCKER_METADATA_FOLDER is unset"; exit; fi

mkdir -p  /tmp/demo/db/

while [ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" != "passing" ]; do	#wait until CONSUL is up and running
    sleep 2
done

echo '*/15  *  *  *  *    ./set-local-datasets.sh' >> /etc/crontabs/root
crond

if [ "$MASTER_FLAG" != "master" ]; then         #this is a worker
    DESC="exareme-worker"
    echo -n $NODE_NAME > /root/exareme/etc/exareme/name
    while [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" != "200" ]; do
        echo "Waiting for master node to be initialized...."
        sleep 2
    done
    ./set-local-datasets.sh
    MASTER_IP=$(curl -s $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/$(curl -s $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/?keys | jq -r '.[]' | sed "s/$EXAREME_MASTER_PATH\///g")?raw)
    MASTER_NAME=$(curl -s $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/?keys | jq -r '.[]' | sed "s/$EXAREME_MASTER_PATH\///g")
    MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)
    . ./start-worker.sh
    curl -X PUT -d @- $CONSULURL/v1/kv/$EXAREME_ACTIVE_WORKERS_PATH/$NODE_NAME <<< $MY_IP
    while [ ! -f /var/log/exareme.log ]; do
        echo "Trying to connect worker with IP "$MY_IP" and name "$NODE_NAME" to master with IP "$MASTER_IP" and name "$MASTER_NAME"."
    done
    tail -f /var/log/exareme.log | while read LOGLINE
    do
        [[ "${LOGLINE}" == *"Worker node started."* ]] && pkill -P $$ tail
        echo " Waiting to establish connection for worker with IP "$MY_IP" with master's IP "$MASTER_IP" and name "$MASTER_NAME".."
        sleep 2
        if [[ "${LOGLINE}" == *"Cannot connect to"* ]]; then
            echo "Can not establish connection with master node. Is master node running? Terminating worker node "$NODE_NAME"..."
           stop_exareme
        fi
    done
    echo -e "\nWorker with IP "$MY_IP" and name "$NODE_NAME" connected to master with IP "$MASTER_IP" and name "$MASTER_NAME"."

#this is the master
else
    DESC="exareme-master"
    echo -n $NODE_NAME > /root/exareme/etc/exareme/name
    ./set-local-datasets.sh
    MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)
    #Master re-booted
    if [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" = "200" ]; then
        if [ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_ACTIVE_WORKERS_PATH}/?keys)" = "200" ]; then  #workers connected to him
            echo "" #empty echo for if-then-else consistency
            #TODO check what if master restarts with different IP while workers are already connected to the master's registry with previous IP
        else
            ./exareme-admin.sh --start
            echo "Master node with IP "$MY_IP" and name " $NODE_NAME" trying to re-boot..."
                while [ ! -f /var/log/exareme.log ]; do
            echo "Master node with IP "$MY_IP" and name " $NODE_NAME" re-booted..."
        done
        fi
    #Master just created
    else
        ./exareme-admin.sh --start
        echo "Initializing master node with IP "$MY_IP" and name " $NODE_NAME"..."
        while [ ! -f /var/log/exareme.log ]; do
            echo "Initializing master node with IP "$MY_IP" and name " $NODE_NAME"..."
        done
    fi
    curl -X PUT -d @- $CONSULURL/v1/kv/$EXAREME_MASTER_PATH/$NODE_NAME <<< $MY_IP
fi

# Both Master and Worker should transform the csv to an sqlite db file
echo "Removing the previous datasets.db file if it still exists"
rm -f $DOCKER_METADATA_FOLDER/datasets.db
echo "Parsing the csv file in " $DOCKER_METADATA_FOLDER " to a db file. "
python ./convert-csv-dataset-to-db.py --csvFilePath $DOCKER_DATASETS_FOLDER/datasets.csv --CDEsMetadataPath $DOCKER_METADATA_FOLDER/CDEsMetadata.json --outputDBAbsPath $DOCKER_METADATA_FOLDER/datasets.db

# Creating the python log file
echo "Exareme Python Algorithms log file created." > /var/log/exaremePythonAlgorithms.log

# Running something in foreground, otherwise the container will stop
while true
do
   tail -f /var/log/exareme.log -f /var/log/exaremePythonAlgorithms.log
done
