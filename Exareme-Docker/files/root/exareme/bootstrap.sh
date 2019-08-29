#!/usr/bin/env bash

if [[ -z "${EXAREME_ACTIVE_WORKERS_PATH}" ]]; then
  echo "Env. variable 'active_workers' not initialized in docker-compose.yaml files. Exiting..."
  exit 1
else
  EXAREME_ACTIVE_WORKERS_PATH=${EXAREME_ACTIVE_WORKERS_PATH}
fi
if [[ -z "${EXAREME_MASTER_PATH}" ]]; then
  echo "Env. variable 'master' not initialized in docker-compose.yaml files. Exiting..."
  exit 1

else
  EXAREME_MASTER_PATH=${EXAREME_MASTER_PATH}
fi
if [[ -z "${DATASETS}" ]]; then
  echo "Env. variable 'datasets' not initialized in docker-compose.yaml files. Exiting..."
  exit 1
else
  DATASETS=${DATASETS}
fi
if [ -z ${CONSULURL} ]; then echo "CONSULURL is unset"; exit; fi
if [ -z ${NODE_NAME} ]; then echo "NODE_NAME is unset";exit;  fi
if [ -z ${DOCKER_DATA_FOLDER} ]; then echo "DOCKER_DATA_FOLDER is unset"; exit; fi
if [ -z ${DOCKER_METADATA_FOLDER} ]; then echo "DOCKER_METADATA_FOLDER is unset"; exit; fi

#Stop Exareme service
stop_exareme () {
    if [[ -f /tmp/exareme/var/run/*.pid ]]; then
	    kill -9 $( cat /tmp/exareme/var/run/*.pid)
        rm /tmp/exareme/var/run/*.pid
        echo "Stopped."
        exit 0
    else
       echo "Already stopped, no action taken."
    fi
}

#Clean ups in Consul [key-value store]
deleteKeysFromConsul () {
    if [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${DATASETS}/${NODE_NAME}?keys)" = "200" ]]; then
        curl -s -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
    fi
    if [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${1}/${NODE_NAME}?keys)" = "200" ]]; then
        curl -s -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME
    fi
}

#CSVs to DB
transformCsvToDB () {
    # Both Master and Worker should transform the csv to an sqlite db file
    echo "Removing the previous datasets.db file if it still exists"
    rm -f ${DOCKER_METADATA_FOLDER}/datasets.db
    echo "Parsing the csv file in " ${DOCKER_METADATA_FOLDER} " to a db file. "
    python ./convert-csv-dataset-to-db.py --csvFilePath ${DOCKER_DATA_FOLDER}/datasets.csv --CDEsMetadataPath ${DOCKER_METADATA_FOLDER}/CDEsMetadata.json --outputDBAbsPath $DOCKER_METADATA_FOLDER/datasets.db
	#Get the status code from previous command
    py_script=$?
	#If status code != 0 an error has occurred
    if [[ ${py_script} -ne 0 ]]; then
         echo "Script: \"convert-csv-dataset-to-db.py\" exited with error." >&2
         exit 1
    fi
}

# Setup signal handlers
trap term_handler SIGTERM SIGKILL

#This funciton will be executed when the container receives the SIGTERM signal (when stopping)
term_handler () {

if [[ "${MASTER_FLAG}" != "master" ]]; then   #worker
    echo "*******************************Stopping Worker**************************************"
    if [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
        deleteKeysFromConsul "$EXAREME_ACTIVE_WORKERS_PATH"
    fi
    if [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" = "200" ]]; then
        MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)
        MASTER_IP=$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${EXAREME_MASTER_PATH}\///g")?raw)
        #Delete worker from master's registry
        curl -s ${MASTER_IP}:9091/remove/worker?IP=${MY_IP}        #TODO check if that was done?
    fi
    stop_exareme
else                                      #master
    echo "*******************************Stopping Master**************************************"
    if [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" = "passing" ]];  then
        deleteKeysFromConsul "$EXAREME_MASTER_PATH"
    fi
    stop_exareme
fi
exit 0
}

mkdir -p  /tmp/demo/db/

#This is the Worker
if [[ "${MASTER_FLAG}" != "master" ]]; then

    DESC="exareme-worker"
    MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)

    #Try accessing Consul[key-value store]
    echo -e "\nWorker node["${NODE_NAME}","${MY_IP}"] trying to connect with Consul[key-value store]"
    n=0
    #Wait until Consul [key-value store] is up and running
    while [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" != "passing" ]]; do
        echo -e "\nWorker node["${NODE_NAME}","${MY_IP}"] trying to connect with Consul[key-value store]"
        sleep 2
        n=$((${n} + 1))
        #After 4 attempts-Show error
        if [[ ${n} -ge 5 ]]; then
            echo -e "\nConsul[key-value store] may not be initialized or Worker node["${NODE_NAME}","${MY_IP}"] can not contact Consul[key-value store]"
            exit 1  #Simple exit 1. Exareme is not up yet
        fi
    done

    #Try retrieve Master's IP from Consul[key-value store]
    echo -e "Retrieving Master's info from Consul[key-value store]"
    n=0
    while [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" != "200" ]]; do
        sleep 5
        n=$((${n} + 1))
        echo -e "Retrieving Master's info from Consul[key-value store]"
        if [[ ${n} -ge 30 ]]; then
            echo "Is Master node initialized? Check Master's logs. Terminating Worker node["${NODE_NAME}","${MY_IP}"]"
            exit 1
        fi
    done

    #Get Master's IP/Name from Consul[key-value store]
    MASTER_IP=$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${EXAREME_MASTER_PATH}\///g")?raw)
    MASTER_NAME=$(curl -s ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${EXAREME_MASTER_PATH}\///g")

    #CSVs to DB
    transformCsvToDB

    . ./start-worker.sh
    echo "Worker node["${MY_IP}","${NODE_NAME}]" trying to connect with Master node["${MASTER_IP}","${MASTER_NAME}"]"
    while [[ ! -f /var/log/exareme.log ]]; do
        echo "Worker node["${MY_IP}","${NODE_NAME}]" trying to connect with Master node["${MASTER_IP}","${MASTER_NAME}"]"
        sleep 1
    done
    echo "Waiting to establish connection for Worker node["${MY_IP}","${NODE_NAME}"] with Master node["${MASTER_IP}","${MASTER_NAME}"]"
    tail -f /var/log/exareme.log | while read LOGLINE
    do
        [[ "${LOGLINE}" == *"Worker node started."* ]] && pkill -P $$ tail
        echo "Waiting to establish connection for Worker node["${MY_IP}","${NODE_NAME}"] with Master node["${MASTER_IP}","${MASTER_NAME}"]"
        sleep 1

        #Java's exception in StartWorker.java
        if [[ "${LOGLINE}" == *"java.rmi.RemoteException"* ]]; then
            exit 1  #Simple exit 1. Exareme is not up yet
        fi
    done

    #Health check for Worker. LIST_DATASET algorithm execution
    echo "Health check for Worker node["${MY_IP}","${NODE_NAME}"]"
    check="$(curl -s ${MASTER_IP}:9092/check/worker?IP_MASTER=${MASTER_IP}?IP_WORKER=${MY_IP})"

    #error: Something went wrong could happen (it could be madis..)
    if [[ $( echo ${check} | jq '.error') != null ]]; then
        echo ${check} | jq '.error'
        echo -e  "\nExiting.."
        exit 1
    fi

    getNames="$( echo ${check} | jq '.active_nodes')"

    #Retrieve result as json. If $NODE_NAME exists in result, the algorithm run in the specific node
    if [[ $getNames = *${NODE_NAME}* ]]; then
        echo -e "\nWorker node["${MY_IP}","${NODE_NAME}"] connected to Master node["${MASTER_IP}","${MASTER_NAME}"]"
        curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${EXAREME_ACTIVE_WORKERS_PATH}/${NODE_NAME} <<< ${MY_IP}
    else
        echo "Worker node["${MY_IP}","${NODE_NAME}]" seems that is not connected with the Master.Exiting..."
        exit 1
    fi


#This is the Master
else
    DESC="exareme-master"
    MY_IP=$(/sbin/ifconfig | grep "inet " | awk -F: '{print $2}' | grep '10.20' | awk '{print $1;}' | head -n 1)

    echo -e "\nMaster node["${NODE_NAME}","${MY_IP}"] trying to connect with Consul[key-value store]"

    #Try accessing Consul[key-value store]
    echo -e "\nMaster node["${NODE_NAME}","${MY_IP}"] trying to connect with Consul[key-value store]"
    n=0
    #Wait until Consul [key-value store] is up and running
    while [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" != "passing" ]]; do
        echo -e "\nMaster node["${NODE_NAME}","${MY_IP}"] trying to connect with Consul[key-value store]"
        n=$((${n} + 1))
        sleep 1
        #After 30 attempts-Show error
        if [[ ${n} -ge 30 ]]; then
            echo -e "\nConsul[key-value store] may not be initialized or Master node["${NODE_NAME}","${MY_IP}"] can not contact Consul[key-value store]"
            exit 1  #Simple exit 1. Exareme is not up yet
        fi
    done

    #CSVs to DB
    transformCsvToDB

    #Master re-booted
    if [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/?keys)" = "200" ]]; then
        #Workers connected to Master node
        if [[ "$(curl -s -o  /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${EXAREME_ACTIVE_WORKERS_PATH}/?keys)" = "200" ]]; then
            #Empty echo for if-then-else consistency
            echo ""
            #TODO check what if master restarts with different IP while workers are already connected to the master's registry with previous IP
        else
            ./exareme-admin.sh --start
            echo "Master node["${MY_IP}","$NODE_NAME"] trying to re-boot..."
            while [[ ! -f /var/log/exareme.log ]]; do
                echo "Master node["$MY_IP"," $NODE_NAME"] re-booted..."
            done
        fi

    #Master node just created
    else
        ./exareme-admin.sh --start
        echo "Initializing Master node["${MY_IP}","${NODE_NAME}"]"

        while [[ ! -f /var/log/exareme.log ]]; do
            echo "Initializing Master node["${MY_IP}","${NODE_NAME}"]"
        done
        echo "Initializing Master node["${MY_IP}","${NODE_NAME}"]"
        tail -f /var/log/exareme.log | while read LOGLINE
        do
            [[ "${LOGLINE}" == *"Master node started."* ]] && pkill -P $$ tail
            echo "Initializing Master node["${MY_IP}","${NODE_NAME}"]"

            #Java's exception in StartMaster.java
            if [[ "${LOGLINE}" == *"java.rmi.RemoteException"* ]]; then
                exit 1  #Simple exit 1. Exareme is not up yet
            fi
        done
	

        #Health check for Master. LIST_DATASET algorithm execution
        echo "Health check for Master node["${MY_IP}","${NODE_NAME}"]"
        check="$(curl -s ${MY_IP}:9092/check/worker?IP_MASTER=${MY_IP}?IP_WORKER=${MY_IP})"     #Master has a Worker instance. So in this case IP_MASTER / IP_WORKER is the same

        #error: Something went wrong could happen (it could be madis)
        if [[ $( echo ${check} | jq '.error') != null ]]; then
             echo ${check} | jq '.error'
             echo "Exiting.."
             exit 1
        fi

        getNames="$( echo ${check} | jq '.active_nodes')"
        #Retrieve result as json. If $NODE_NAME exists in result, the algorithm run in the specific node
        if [[ $getNames = *${NODE_NAME}* ]]; then
            echo -e "\nMaster node["${MY_IP}","${NODE_NAME}"] initialized"
            curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${EXAREME_MASTER_PATH}/${NODE_NAME} <<< ${MY_IP}
        else
            echo "Master node["${MY_IP}","${NODE_NAME}]" seems that could not be initialized.Exiting..."
            exit 1
        fi
    fi
fi

#Both Worker(s)/Master will execute the command. At this point Consul [Key-value store] is up, running and everyone can access it
./set-local-datasets.sh
echo '*/15  *  *  *  *    ./set-local-datasets.sh' >> /etc/crontabs/root
crond

# Creating the python log file
echo "Exareme Python Algorithms log file created." > /var/log/exaremePythonAlgorithms.log

# Running something in foreground, otherwise the container will stop
while true
do
   tail -f /var/log/exareme.log -f /var/log/exaremePythonAlgorithms.log
done
