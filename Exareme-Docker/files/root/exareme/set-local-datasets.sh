#!/bin/bash

MADIS="/mnt/c/Users/Thanasis/Desktop/Madgik/exareme/Exareme-Docker/src/exareme/exareme-tools/madis/src/mterm.py"
DOCKER_DATA_FOLDER="/mnt/c/Users/Thanasis/Desktop/Madgik/data"
ALL_DATASETS=""

for PATHOLOGY in $DOCKER_DATA_FOLDER/*	
do
	PATHOLOGY_DATASETS=$(echo "select distinct dataset from (file header:t  file:$PATHOLOGY/datasets.csv);" | $MADIS | sed '1d ; $d' ) 
	
	ALL_DATASETS+=" $PATHOLOGY_DATASETS"
done


ALL_DATASETS=$(echo $ALL_DATASETS | jq .[]  | sed 's/^\"//g ; s/\"$//g' | printf %s "$(cat)" | jq -R -c -s 'split("\n")')

curl -s -X PUT -d @- $CONSULURL/v1/kv/datasets/$NODE_NAME <<< $ALL_DATASETS
