#!/usr/bin/env bash
#
#
# curl, jq

TARGET_ALGORITHM="$1";
TARGET_HOST="";
TARGET_PORT="9090";
WAIT_TIME=10;
QUERY_KEY="";
QUERY_STATUS="0";


function submit_algorithm() {
    QUERY_KEY=$(curl -X POST http://$TARGET_HOST:$TARGET_PORT/mining/query/$TARGET_ALGORITHM 2> /dev/null | jq -r '.queryKey')
    if [[ $? != 0 ]]
    then
        echo "Unable to sumbit algorithm.";
        exit 1;
    fi
}


function get_status() {
    QUERY_STATUS=$(curl -X POST http://$TARGET_HOST:$TARGET_PORT/mining/query/$QUERY_KEY/status 2> /dev/null | jq -r '.status')
    if [[ $? != 0 ]]
    then
        echo "Unable to status algorithm.";
        exit 1;
    fi
}

function get_result(){
    curl -X POST "http://$TARGET_HOST:$TARGET_PORT/mining/query/$QUERY_KEY/result" &> /dev/null
    if [[ $? != 0 ]]
    then
        echo "Unable to result algorithm.";
        exit 1;
    fi
    return 0;
}

function check_status() {

    while [[ $QUERY_STATUS != "100" ]]
    do
        sleep $WAIT_TIME;
        get_status;
    done
    get_result;
}

submit_algorithm
check_status
