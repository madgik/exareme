#!/usr/bin/env bash




docker run -i -t --rm \
-e MASTER_FLAG='master' \
-e RAWUSERNAME="federation" \
-e RAWPASSWORD="federation" \
-e RAWHOST="rawdb" \
-e RAWPORT="54321" \
-p 9090:9090  --name exaremelocal exaremelocal
