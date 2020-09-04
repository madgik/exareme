#!/usr/bin/env bash

#Init environmental variables

export DOCKER_DATA_FOLDER="/root/exareme/data/"
export DOCKER_METADATA_FOLDER="/root/exareme/data/"

export CONSUL_DATA_PATH="data"
export CONSUL_MASTER_PATH="master"
export CONSUL_ACTIVE_WORKERS_PATH="active_workers"

if [[ -z ${CONSULURL} ]]; then
  echo "CONSULURL is unset. Check docker-compose file."
  exit
fi
if [[ -z ${NODE_NAME} ]]; then
  echo "NODE_NAME is unset. Check docker-compose file."
  exit
fi
if [[ -z ${FEDERATION_ROLE} ]]; then
  echo "FEDERATION_ROLE is unset. Check docker-compose file."
  exit
fi
if [[ -z ${ENVIRONMENT_TYPE} ]]; then
  echo "ENVIRONMENT_TYPE is unset. Check docker-compose file."
  exit
fi

timestamp() {
  date +%F' '%T
}

# Wait until Consul [key-value store] is up and running
waitForConsulToStart() {
  attempts=0
  while [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" != "passing" ]]; do
    echo -e "\n$(timestamp) Trying to connect with Consul [key-value store]... "

    # Exit after 30 attempts
    if [[ ${attempts} -ge 30 ]]; then
      echo -e "\n$(timestamp) Consul[key-value store] may not be initialized or Node with IP: ${NODE_IP} and name: ${NODE_NAME} can not contact it."
      exit 1
    fi

    attempts=$((${attempts} + 1))
    sleep 2
  done
  echo -e "\n$(timestamp) Node connected to the consul."
}

# Get Master Node IP from Consul
getMasterIPFromConsul() {
  attempts=0
  while [[ "$(curl -s -o /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys)" != "200" ]]; do
    echo -e "$(timestamp) Retrieving Master's info from Consul[key-value store]..."

    if [[ ${attempts} -ge 30 ]]; then
      echo "$(timestamp) Is Master node initialized? Check Master's logs. Terminating Worker node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}."
      exit 1
    fi

    attempts=$((${attempts} + 1))
    sleep 5
  done

  MASTER_IP=$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${CONSUL_MASTER_PATH}\///g")?raw)

  echo -e "\n$(timestamp) Fetched master node's IP ${MASTER_IP}."

}

# Clean ups in Consul [key-value store]
deleteKeysFromConsul() {
  if [[ "$(curl -s -o /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${DATA_PATH}/${NODE_NAME}?keys)" == "200" ]]; then
    curl -s -X DELETE $CONSULURL/v1/kv/$DATASETS/$NODE_NAME
  fi
  if [[ "$(curl -s -o /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${1}/${NODE_NAME}?keys)" == "200" ]]; then
    curl -s -X DELETE $CONSULURL/v1/kv/$1/$NODE_NAME
  fi
}

# Convert CSVs to DB
convertCSVsToDB() {
  # Both Master and Worker should transform the csvs to sqlite db files
  NODE_TYPE=${1}

  # Removing all previous .db files from the DOCKER_DATA_FOLDER
  echo "$(timestamp) Deleting previous db files. "
  rm -rf ${DOCKER_DATA_FOLDER}/**/*.db

  echo "$(timestamp) Parsing the csv files in " ${DOCKER_DATA_FOLDER} " to db files. "
  python ./convert-csv-dataset-to-db.py -f ${DOCKER_DATA_FOLDER} -t ${NODE_TYPE}
  #Get the status code from previous command
  py_script=$?
  #If status code != 0 an error has occurred
  if [[ ${py_script} -ne 0 ]]; then
    echo "$(timestamp) Script: \"convert-csv-dataset-to-db.py\" exited with error." >&2
    exit 1
  fi
}

# Wait for exareme to be up and running
waitForExaremeMasterToStart() {

  attempts=0
  while [[ $(curl -s -o /dev/null -w "%{http_code}" ${NODE_IP}:9092/check/worker?NODE_IP=${NODE_IP}) == "000" ]]; do
    # Exit after 10 attempts
    if [[ ${attempts} -ge 10 ]]; then
      echo -e "\n$(timestamp) Exareme could not be initialized. Exiting."
      exit 1
    fi

    echo "$(timestamp) Waiting for exareme to be initialized..."
    attempts=$((${attempts} + 1))
    sleep 10
  done

  echo "$(timestamp) Exareme initialized."
}

# Wait for exareme to be up and running
waitForExaremeWorkerToStart() {

  while [[ ! -f /var/log/exareme.log ]]; do
    echo "$(timestamp) Waiting for exareme to be initialized..."
    sleep 1
  done

  attempts=0
  tail -f /var/log/exareme.log | while read LOGLINE; do
    [[ "${LOGLINE}" == *"Worker node started."* ]] && pkill -P $$ tail
    echo "$(timestamp) Waiting for exareme to be initialized..."
    sleep 1

    #Java's exception in StartWorker.java
    if [[ "${LOGLINE}" == *"java.rmi.RemoteException"* ]]; then
      echo -e "\n$(timestamp) Exareme could not be initialized. Exiting."
      exit 1
    fi

    # Exit after 30 attempts
    if [[ ${attempts} -ge 30 ]]; then
      echo -e "\n$(timestamp) Exareme takes too long to start. Exiting."
      exit 1
    fi
    attempts=$((${attempts} + 1))
  done
}

# Health check for exareme node
exaremeNodeHealthCheck() {
  if [[ ${ENVIRONMENT_TYPE} != "PROD" ]]; then
    return 0
  fi

  echo "$(timestamp) HEALTH CHECK for node with IP ${NODE_IP} and name ${NODE_NAME} ."

  # Ping exareme to see if node is available.
  check=$(curl -s ${MASTER_IP}:9092/check/worker?NODE_IP=${NODE_IP})

  if [[ -z ${check} ]]; then
    return 1
  fi

  # Check if what curl returned is JSON
  echo ${check} | jq empty
  check_code=$?
  if [[ ${check_code} -ne 0 ]]; then
    return 1
  fi

  # Retrieve result as json. If $NODE_NAME exists in result, the algorithm run in the specific node
  getNames="$(echo ${check} | jq '.active_nodes')"
  if ! [[ ${getNames} == *${NODE_NAME}* ]]; then
    return 1
  fi

  return 0
}

# Periodic check for exareme's health.
# If it fails shutdown the container
periodicExaremeNodeHealthCheck() {
  # Make a health check every 5 minutes.
  while true; do
    sleep 10	# TODO Increase

    # If health check fails then try again until it succeeds or close the container.
    if ! exaremeNodeHealthCheck; then
      attempts=0
      while ! exaremeNodeHealthCheck; do
        if [[ ${attempts} -ge 10 ]]; then
          echo -e "\n$(timestamp) HEALTH CHECK FAILED. Closing the container."
          pkill -f 1 # Closing main bootstrap.sh process to stop the container.
        fi
        echo "$(timestamp) HEALTH CHECK failed. Trying again..."
        attempts=$((${attempts} + 1))
        sleep 5
      done
    fi
    echo "$(timestamp) HEALTH CHECK successful. MASTER_IP: $MASTER_IP and NODE_IP: $NODE_IP"
  done
}

# Verify that exareme master IP on consul is the same.
# If not master has restarted, so workers need to restart as well.
checkExaremeMasterState() {
  while true; do
    PREVIOUS_MASTER_IP=$MASTER_IP
    getMasterIPFromConsul

    if [[ $PREVIOUS_MASTER_IP != $MASTER_IP ]]; then
      echo "$(timestamp) Master restarted, shutting down worker."
      pkill -f 1 # Closing main bootstrap.sh process
    fi

    sleep 30
  done
}

# Periodic deletion of temp files
startTempFilesDeletionTask() {
  echo '0 *  *  *  * if [ $FEDERATION_ROLE = "master" ]; then \
cd /tmp/demo/db/ \
&& find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +\
&& cd /tmp/demo/algorithms-generation/ \
&& find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +;\
else \
cd /tmp/demo/db/ \
&& find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +\
&& find . -type f -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -delete; \
fi' >>/etc/crontabs/root
  crond
}

mkdir -p /tmp/demo/db/

NODE_IP=$(/sbin/ifconfig eth0 | grep "inet" | awk -F: '{print $2}' | cut -d ' ' -f 1)

# Start Exareme and MadisServer
echo "Starting Madis Server..."
python /root/madisServer/MadisServer.py &
echo "Madis Server started."

waitForConsulToStart

# Running bootstrap on a master node
if [[ "${FEDERATION_ROLE}" == "master" ]]; then
  MASTER_IP=$NODE_IP

  echo "$(timestamp) Starting Exareme on master node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"
  ./exareme-admin.sh --start

  waitForExaremeMasterToStart

  # Updating consul with node IP
  echo -e "\n$(timestamp) Updating consul with master node IP."
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/${NODE_NAME} <<<${NODE_IP}

  if ! exaremeNodeHealthCheck; then
    echo "$(timestamp) HEALTH CHECK algorithm failed. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting..."
    exit 1
  fi

  periodicExaremeNodeHealthCheck &

  # Prepare datasets from CSVs to SQLite db files
  convertCSVsToDB "master"

else ##### Running bootstrap on a worker node #####

  getMasterIPFromConsul

  echo "$(timestamp) Starting Exareme on worker node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"
  . ./start-worker.sh

  waitForExaremeWorkerToStart

  # Updating consul with node IP
  echo -e "\n$(timestamp) Updating consul with worker node IP."
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_ACTIVE_WORKERS_PATH}/${NODE_NAME} <<<${NODE_IP}

  if ! exaremeNodeHealthCheck; then
    echo "$(timestamp) HEALTH CHECK algorithm failed. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting..."
    exit 1
  fi

  periodicExaremeNodeHealthCheck &

  # Prepare datasets from CSVs to SQLite db files
  convertCSVsToDB "worker"

fi

# Updating consul with node's datasets.
echo "$(timestamp) Updating consul with node's datasets."
./set-local-datasets.sh

startTempFilesDeletionTask

# Creating the python log file
echo "$(timestamp) Exareme Python Algorithms log file created." >/var/log/exaremePythonAlgorithms.log

# Printing logs of Exareme, madis server and python algorithms.
tail -fn +1 /var/log/exareme.log -fn +1 /var/log/exaremePythonAlgorithms.log -fn +1 /var/log/MadisServer.log
