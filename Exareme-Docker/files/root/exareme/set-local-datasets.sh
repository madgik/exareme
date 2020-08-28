#!/bin/bash

ALL_DATASETS=""
MADIS="/root/exareme/lib/madis/src/mterm.py"

for PATHOLOGY in ${DOCKER_DATA_FOLDER}/*
do
	if [[ -f ${PATHOLOGY}/datasets.db ]]
	then
		PATHOLOGY_DATASETS=$(echo "select distinct dataset from data;" | $MADIS ${PATHOLOGY}/'datasets.db'| sed '1d ; $d' )

		PATHOLOGY_DATASETS=$(echo ${PATHOLOGY_DATASETS} | jq .[]  | sed 's/^\"//g ; s/\"$//g' | printf %s "$(cat)" | jq -R -c -s 'split("\n")')

        pathology=$(basename ${PATHOLOGY})

        curl -s -X PUT -d @- ${CONSULURL}/v1/kv/${CONSUL_DATA_PATH}/${NODE_NAME}/${pathology} <<< ${PATHOLOGY_DATASETS}

        PATHOLOGY_DATASETS=''
	fi

done
