#!/bin/sh
#                    Copyright (c) 2017-2018
#   Data Intensive Applications and Systems Labaratory (DIAS)
#            Ecole Polytechnique Federale de Lausanne
#
#                      All Rights Reserved.
#
# Permission to use, copy, modify and distribute this software and its
# documentation is hereby granted, provided that both the copyright notice
# and this permission notice appear in all copies of the software, derivative
# works or modified versions, and any portions thereof, and that both notices
# appear in supporting documentation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
# A PARTICULAR PURPOSE. THE AUTHORS AND ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE
# DISCLAIM ANY LIABILITY OF ANY KIND FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE
# USE OF THIS SOFTWARE.

# Import settings
. ./settings.sh

# Permanent storage for Portainer
test -d ${PORTAINER_DATA} \
	|| mkdir -p ${PORTAINER_DATA} \
	|| ( echo Failed to create ${PORTAINER_DATA}; exit 1 )

docker service create \
	--publish mode=host,target=${PORTAINER_PORT},published=9000 \
	--constraint 'node.role == manager' \
	--detach=true \
	--mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
	--mount type=bind,src=${PORTAINER_DATA},dst=/data \
	--name ${COMPOSE_PROJECT_NAME}_portainer \
	${PORTAINER_IMAGE}${PORTAINER_VERSION}
