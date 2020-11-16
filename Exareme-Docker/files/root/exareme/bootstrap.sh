#!/usr/bin/env bash

#Init environmental variables

export DOCKER_DATA_FOLDER="/root/exareme/data/"
export DOCKER_METADATA_FOLDER="/root/exareme/data/"

export CONSUL_DATA_PATH="data"
export CONSUL_MASTER_PATH="master"
export CONSUL_ACTIVE_WORKERS_PATH="active_workers"

CONSUL_CONNECTION_MAX_ATTEMPTS=20
CONSUL_WAIT_FOR_MASTER_IP_MAX_ATTEMPTS=20
EXAREME_NODE_STARTUP_HEALTH_CHECK_MAX_ATTEMPTS=10
EXAREME_NODE_HEALTH_CHECK_TIMEOUT=30
PERIODIC_EXAREME_NODES_HEALTH_CHECK_MAX_RETRIES=10
PERIODIC_EXAREME_NODES_HEALTH_CHECK_INTERVAL=120
PERIODIC_TEMP_FILES_REMOVAL=300

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
if [[ -z ${CONVERT_CSVS} ]]; then
  echo "CONVERT_CSVS is unset. Check docker-compose file."
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
    if [[ $attempts -ge $CONSUL_CONNECTION_MAX_ATTEMPTS ]]; then
      echo -e "\n$(timestamp) Consul[key-value store] may not be initialized or Node with IP: ${NODE_IP} and name: ${NODE_NAME} can not contact it."
      exit 1
    fi

    attempts=$(($attempts + 1))
    sleep 5
  done
  echo -e "\n$(timestamp) Node connected to the consul."
}

# Get Master Node IP from Consul
getMasterIPFromConsul() {
  attempts=0
  while [[ "$(curl -s -o /dev/null -i -w "%{http_code}\n" ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys)" != "200" ]]; do
    echo -e "$(timestamp) Retrieving Master's info from Consul[key-value store]..."

    if [[ $attempts -ge $CONSUL_WAIT_FOR_MASTER_IP_MAX_ATTEMPTS ]]; then
      echo "$(timestamp) Is Master node initialized? Check Master's logs. Terminating Worker node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}."
      return 1
    fi

    attempts=$(($attempts + 1))
    sleep 5
  done

  MASTER_IP=$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/$(curl -s ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/?keys | jq -r '.[]' | sed "s/${CONSUL_MASTER_PATH}\///g")?raw)

  echo -e "\n$(timestamp) Fetched master node's IP ${MASTER_IP}"
  return 0
}

# Convert CSVs to DB
convertCSVsToDB() {
  
  # Skip convertion if flag is false
  if [[ ${CONVERT_CSVS} == "FALSE" ]]; then
    echo "$(timestamp) CSV convertion turned off. "
	return 0
  fi

  # Removing all previous .db files from the DOCKER_DATA_FOLDER
  echo "$(timestamp) Deleting previous db files. "
  rm -rf ${DOCKER_DATA_FOLDER}/**/*.db

  echo "$(timestamp) Parsing the csv files in " ${DOCKER_DATA_FOLDER} " to db files. "
  python3 ./convert-csv-dataset-to-db.py -f ${DOCKER_DATA_FOLDER}
  #Get the status code from previous command
  py_script=$?
  #If status code != 0 an error has occurred
  if [[ ${py_script} -ne 0 ]]; then
    echo "$(timestamp) Script: \"convert-csv-dataset-to-db.py\" exited with error." >&2
    exit 1
  fi
}

# Health check for exareme nodes.
# Health check from MASTER checks all nodes.
# Health check from WORKERS checks only that specific node.
exaremeNodesHealthCheck() {
  if [[ ${ENVIRONMENT_TYPE} != "PROD" ]]; then
    return 0
  fi

  echo "$(timestamp) HEALTH CHECK for node with IP ${NODE_IP} and name ${NODE_NAME} ."

  if [[ "${FEDERATION_ROLE}" == "master" ]]; then
    check=$(curl -s -X POST --max-time ${EXAREME_NODE_HEALTH_CHECK_TIMEOUT} ${NODE_IP}:9090/mining/query/HEALTH_CHECK)
  else
    check=$(curl -s --max-time ${EXAREME_NODE_HEALTH_CHECK_TIMEOUT} ${MASTER_IP}:9092/check/worker?NODE_IP=${NODE_IP})
  fi

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

# Exareme health check on startup
startupExaremeNodesHealthCheck() {
  # If health check fails then try again until it succeeds or close the container.
  if ! exaremeNodesHealthCheck; then
    attempts=0
    while ! exaremeNodesHealthCheck; do
      if [[ $attempts -ge $EXAREME_NODE_STARTUP_HEALTH_CHECK_MAX_ATTEMPTS ]]; then
        echo -e "\n$(timestamp) HEALTH CHECK FAILED. Closing the container."
        return 1 # Exiting
      fi
      echo "$(timestamp) HEALTH CHECK failed. Trying again..."
      attempts=$(($attempts + 1))
      sleep 5
    done
  fi
  echo "$(timestamp) HEALTH CHECK successful on NODE_IP: $NODE_IP"
  return 0
}

# Periodic check for exareme's health.
# If it fails shutdown the container
periodicExaremeNodesHealthCheck() {
  # Make a health check every 5 minutes.
  while true; do
    sleep $PERIODIC_EXAREME_NODES_HEALTH_CHECK_INTERVAL

    # If consul doesn't have master node's IP it means that it restarted. The nodes should restart.
    if ! getMasterIPFromConsul; then
      pkill -f 1 # Closing main bootstrap.sh process to stop the container.
    fi

    # If health check fails then try again until it succeeds or close the container.
    if ! exaremeNodesHealthCheck; then
      attempts=0
      while ! exaremeNodesHealthCheck; do
        if [[ $attempts -ge $PERIODIC_EXAREME_NODES_HEALTH_CHECK_MAX_RETRIES ]]; then
          echo -e "\n$(timestamp) HEALTH CHECK FAILED. Closing the container."
          pkill -f 1 # Closing main bootstrap.sh process to stop the container.
        fi
        echo "$(timestamp) HEALTH CHECK failed. Trying again..."
        attempts=$(($attempts + 1))
        sleep 5
      done
    fi
    echo "$(timestamp) HEALTH CHECK successful on NODE_IP: $NODE_IP"
  done
}

# Periodic deletion of temp files
startTempFilesDeletionTask() {
  while true; do
    sleep $PERIODIC_TEMP_FILES_REMOVAL
    if [ $FEDERATION_ROLE = "master" ]; then
      cd /tmp/demo/db/;
      find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +;
      cd /tmp/demo/algorithms-generation/;
      find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +;
    else
      cd /tmp/demo/db/;
      find . -type d -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -exec rm -rf {} +;
      find . -type f -path "./*" -mmin +$TEMP_FILES_CLEANUP_TIME -delete;
    fi
  done
}

mkdir -p /tmp/demo/db/

# Getting the IP and removing white spaces
NODE_IP=$(hostname -i | sed 's/ *$//g')

# Start Exareme and MadisServer
echo "Starting Madis Server..."
python /root/madisServer/MadisServer.py &
echo "Madis Server started."

waitForConsulToStart

# Running bootstrap on a master node
if [[ "${FEDERATION_ROLE}" == "master" ]]; then

  echo "$(timestamp) Starting Exareme on master node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"
  ./exareme-admin.sh --start

  # Updating consul with node IP
  echo -e "\n$(timestamp) Updating consul with master node IP."
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_MASTER_PATH}/${NODE_NAME} <<<${NODE_IP}
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_ACTIVE_WORKERS_PATH}/${NODE_NAME} <<<${NODE_IP}

  if ! startupExaremeNodesHealthCheck; then
    echo "$(timestamp) HEALTH CHECK algorithm failed. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting..."
    exit 1
  fi

  periodicExaremeNodesHealthCheck &

  # Prepare datasets from CSVs to SQLite db files
  convertCSVsToDB

else ##### Running bootstrap on a worker node #####

  if ! getMasterIPFromConsul; then
    echo "$(timestamp) Could not fetch master node's IP. Exiting..."
    exit 1
  fi

  echo "$(timestamp) Starting Exareme on worker node with IP: ${NODE_IP} and nodeName: ${NODE_NAME}"
  . ./start-worker.sh

  # Updating consul with node IP
  echo -e "\n$(timestamp) Updating consul with worker node IP."
  curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_ACTIVE_WORKERS_PATH}/${NODE_NAME} <<<${NODE_IP}

  if ! startupExaremeNodesHealthCheck; then
    echo "$(timestamp) HEALTH CHECK algorithm failed. Switch ENVIRONMENT_TYPE to 'DEV' to see error messages coming from EXAREME. Exiting..."
    exit 1
  fi

  periodicExaremeNodesHealthCheck &

  # Prepare datasets from CSVs to SQLite db files
  convertCSVsToDB

fi

# Updating consul with node's datasets.
echo "$(timestamp) Updating consul with node's datasets."
./set-local-datasets.sh

startTempFilesDeletionTask &

# Creating the python log file
echo "$(timestamp) Exareme Python Algorithms log file created." >/var/log/exaremePythonAlgorithms.log

# Printing logs of Exareme, madis server and python algorithms.
tail -fn +1 /var/log/exareme.log -fn +1 /var/log/exaremePythonAlgorithms.log -fn +1 /var/log/MadisServer.log
