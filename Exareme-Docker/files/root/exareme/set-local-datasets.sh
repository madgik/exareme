#!/bin/bash

ALL_DATASETS=""
MADIS="/root/exareme/lib/madis/src/mterm.py"

for PATHOLOGY in ${DOCKER_DATA_FOLDER}/*
do
	if [[ -f ${PATHOLOGY}/datasets.csv ]]
	then
		PATHOLOGY_DATASETS=$(echo "select distinct dataset from (file header:t  file:$PATHOLOGY/datasets.csv);" | $MADIS | sed '1d ; $d' ) 

		PATHOLOGY_DATASETS=$(echo ${PATHOLOGY_DATASETS} | jq .[]  | sed 's/^\"//g ; s/\"$//g' | printf %s "$(cat)" | jq -R -c -s 'split("\n")')

        curl -s -X PUT -d @- ${CONSULURL}/v1/kv/datasets/${NODE_NAME}/${PATHOLOGY} <<< ${PATHOLOGY_DATASETS}

        PATHOLOGY_DATASETS=''
	fi

done
