#!/usr/bin/env bash
. ./config.sh

docker run -i -t --rm --network=$EXA_OVERLAY_NETWORK \
-e MASTER_FLAG='master' \
-e EXA_WORKERS_WAIT=$EXA_WORKERS \
-e CONSULURL=$EXA_CONSUL_URL \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="raw-ui" \
-e RAWPORT="5555" \
-e RAWENDPOINT="query-start" \
-e RAWRESULTS="all" \
-e RAWDATAKEY="output" \
-p 9091:9090  --name exarememaster $EXA_IMAGE
