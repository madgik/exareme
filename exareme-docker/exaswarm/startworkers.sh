#!/usr/bin/env bash
. ./config.sh

docker service create -t --network=$EXA_OVERLAY_NETWORK \
--name exareme-worker \
--replicas $EXA_WORKERS \
-e CONSULURL=$EXA_CONSUL_URL \
-e MASTER_FLAG='' \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="raw-ui" \
-e RAWPORT="5555" \
-e RAWENDPOINT="query-start" \
-e RAWRESULTS="all" \
-e RAWDATAKEY="output" \
 $EXA_IMAGE
