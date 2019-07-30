#!/bin/bash

MADIS="/root/exareme/lib/madis/src/mterm.py"
DATASETS=$(echo "select  distinct dataset from (file header:t  file:$DOCKER_DATASETS_FOLDER/datasets.csv);" | $MADIS | \
	 sed '1d ; $d' | jq .[]  | sed 's/^\"//g ; s/\"$//g' | printf %s "$(cat)"| jq -R -c -s 'split("\n")')

curl -s -X PUT -d @- $CONSULURL/v1/kv/datasets/$NODE_NAME <<< $DATASETS