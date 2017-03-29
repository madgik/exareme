#!/usr/bin/env bash
. ./config.sh

EXAREME_WORKERS_PATH="available_workers"
EXAREME_ACTIVE_WORKERS_PATH="active_workers"

curl -X DELETE  $EXA_CONSUL_URL/v1/kv/$EXAREME_WORKERS_PATH/?recurse
curl -X DELETE  $EXA_CONSUL_URL/v1/kv/$EXAREME_ACTIVE_WORKERS_PATH/?recurse
