#!/usr/bin/env bash

test -d ${PORTAINER_DATA} \
        || sudo mkdir -p ${PORTAINER_DATA} \
        || ( echo Failed to create ${PORTAINER_DATA}; exit 1 )

docker service create \
--publish mode=host,target=${PORTAINER_PORT},published=9000 \
--constraint 'node.role == manager' \
--detach=true --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
--mount type=bind,src=${PORTAINER_DATA},dst=/data \
--name mip_portainer ${PORTAINER_IMAGE}${PORTAINER_VERSION}
