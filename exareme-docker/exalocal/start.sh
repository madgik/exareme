#!/usr/bin/env bash


docker run -i -t --rm \
-e MASTER_FLAG='master' \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="raw-ui" \
-e RAWPORT="5555" \
-e RAWENDPOINT="query-start" \
-e RAWRESULTS="all"
-p 9090:9090  --name exaremelocal exaremelocal

