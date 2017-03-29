#!/usr/bin/env bash

. ./config.sh
docker network create --driver overlay --subnet 10.0.9.0/24 --attachable $EXA_OVERLAY_NETWORK
