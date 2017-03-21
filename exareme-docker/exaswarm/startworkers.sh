#!/usr/bin/env bash


docker service create -t --network=my-net-at \
--name exareme-worker \
--replicas 3 \
-e MASTER_FLAG='' \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="raw-ui" \
-e RAWPORT="5555" \
-e RAWENDPOINT="query-start" \
-e RAWRESULTS="all" \
-e RAWDATAKEY="output" \
-e CONSULURL="83.212.100.72:8500" \
  exaremeswarm
