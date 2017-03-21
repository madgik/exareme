#!/usr/bin/env bash


docker run -i -t --rm --network=my-net-at \
-e MASTER_FLAG='master' \
-e EXA_WORKERS_WAIT='2' \
-e CONSULURL="83.212.100.72:8500" \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="raw-ui" \
-e RAWPORT="5555" \
-e RAWENDPOINT="query-start" \
-e RAWRESULTS="all" \
-e RAWDATAKEY="output" \
-p 9091:9090  --name exarememaster exaremeswarm
