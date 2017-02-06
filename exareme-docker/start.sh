#!/usr/bin/env bash

docker run -i -t --rm -e MASTER_FLAG='master' -p 9090:9090  --link  rawsniffer --name exaremelocal hbpmip/exaremelocal