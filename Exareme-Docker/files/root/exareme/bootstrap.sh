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
exaremeHealthCheck() {
  if [[ ${ENVIRONMENT_TYPE} != "PROD" ]]; then
    return 0
  fi

  echo "$(timestamp) Health check for Master node  with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"

  # Ping exareme to see if node is available.
  if [[ "${FEDERATION_ROLE}" == "master" ]]; then
    check=$(curl -s ${NODE_IP}:9092/check/worker?NODE_IP=${NODE_IP})
  else
    check=$(curl -s ${MASTER_IP}:9092/check/worker?NODE_IP=${NODE_IP})
  fi

  if [[ -z ${check} ]]; then
    echo "$(timestamp) HEALTH_CHECK algorithm did not return anything. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting.."
    exit 1
  fi

  # Check if what curl returned is JSON
  echo ${check} | jq empty
  check_code=$?
  if [[ ${check_code} -ne 0 ]]; then
    echo "$(timestamp) An error has occurred: " ${check} " Exiting... "
    exit 1
  fi

  # Retrieve result as json. If $NODE_NAME exists in result, the algorithm run in the specific node
  getNames="$(echo ${check} | jq '.active_nodes')"
  if ! [[ ${getNames} == *${NODE_NAME}* ]]; then
    echo "$(timestamp) Master node  with IP: ${NODE_IP} and nodeName: ${NODE_NAME} could not be initialized. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting..."
    exit 1
  fi
}

# Stop Exareme service
stop_exareme() {
  kill -9 "$(cat /tmp/exareme/var/run/*.pid)"
  rm /tmp/exareme/var/run/*.pid
  echo "Stopped."
  exit 0
}

# Setup signal handlers
trap term_handler SIGTERM

# This function will be executed when the container receives the SIGTERM signal (when stopping)
term_handler() {

  if [[ "${FEDERATION_ROLE}" != "master" ]]; then #worker
    echo "*******************************Stopping Worker**************************************"
    if [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" == "passing" ]]; then
      deleteKeysFromConsul "$CONSUL_ACTIVE_WORKERS_PATH"
    fi
    if [[ "$(curl -s -o /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys)" == "200" ]]; then
      NODE_IP=$(/sbin/ifconfig eth0 | grep "inet" | awk -F: '{print $2}' | cut -d ' ' -f 1)
      MASTER_IP=$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${CONSUL_MASTER_PATH}\///g")?raw)
      # Delete worker from master's registry
      curl -s ${MASTER_IP}:9091/remove/worker?IP=${NODE_IP}
    fi
    stop_exareme
  else #master
    echo "*******************************Stopping Master**************************************"
    if [[ "$(curl -s ${CONSULURL}/v1/health/state/passing | jq -r '.[].Status')" == "passing" ]]; then
      deleteKeysFromConsul "$CONSUL_MASTER_PATH"
    fi
    stop_exareme
  fi
  exit 0
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

  echo "$(timestamp) Starting Exareme on master node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"
  ./exareme-admin.sh --start

  waitForExaremeMasterToStart

  # Updating consul with node IP
  echo -e "\n$(timestamp) Updating consul with master node IP."
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/${NODE_NAME} <<<${NODE_IP}

  exaremeHealthCheck

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

  exaremeHealthCheck

  # Prepare datasets from CSVs to SQLite db files
  convertCSVsToDB "worker"

fi

# Updating consul with node's datasets.
echo "$(timestamp) Updating consul with node's datasets."
./set-local-datasets.sh

startTempFilesDeletionTask

# Creating the python log file
echo "$(timestamp) Exareme Python Algorithms log file created." >/var/log/exaremePythonAlgorithms.log

# Running something in foreground, otherwise the container will stop
while true; do
  tail -fn +1 /var/log/exareme.log -fn +1 /var/log/exaremePythonAlgorithms.log -fn +1 /var/log/MadisServer.log
done
