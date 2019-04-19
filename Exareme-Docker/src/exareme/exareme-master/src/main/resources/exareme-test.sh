#!/usr/bin/env bash

ALGORITHMS=$(curl http://localhost:9090/mining/algorithms 2>/dev/null)

ALGORITHMS_NAMES=$(echo $ALGORITHMS | jq -r ".[] | .name")
COUNT=0
DT=$(date | tr " " "_" | tr ":" "_")
for alg in  $ALGORITHMS_NAMES; do

    STATUS=$(echo $ALGORITHMS | jq -r ".[] | select(.name == \"$alg\") | .status" )
    if [[ $STATUS == "enabled" ]]; then

        RESP=$(curl -X POST http://localhost:9090/mining/query/$alg 2>/dev/null)
        QUERY_KEY=$(echo "$RESP" | jq -r ".queryKey")
        echo "Algorithm $alg submitted."

        RESP=$(curl -X POST http://localhost:9090/mining/query/$QUERY_KEY/status 2>/dev/null)
        STATUS=$(echo $RESP | jq -r ".status")
        while [[ $STATUS != 100 ]]; do
            sleep 5;
            RESP=$(curl -X POST http://localhost:9090/mining/query/$QUERY_KEY/status 2>/dev/null)
            STATUS=$(echo $RESP | jq -r ".status")
        done

        echo "Algorithm $alg executed."
        mkdir -p /tmp/exareme-test/$DT/$alg/
        touch /tmp/exareme-test/$DT/$alg/result

        curl -X POST http://localhost:9090/mining/query/$QUERY_KEY/result > /tmp/exareme-test/$DT/$alg/result 2> /dev/null
        echo "Algorithm $alg results written"
    fi;
done;

echo "Done\nCheck results in /tmp/exareme-test/"
exit 0

