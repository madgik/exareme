#!/bin/sh
#                    Copyright (c) 2016-2016
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

set -e
CONSULPORT=8500
SHIPYARDPORT=8080
SLAVEPORT=2376
MASTERPORT=3376

KEYSTORE=ks
MANAGER=m0

# Keystore
(
	docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=$KEYSTORE" \
	    --engine-label "eu.hbp.function=keystore" \
	    $KEYSTORE
	eval $(docker-machine env $KEYSTORE)
	docker run --restart=unless-stopped -d --name swarm-keystore -p $CONSULPORT:$CONSULPORT progrium/consul -server -bootstrap
	curl $(docker-machine ip $KEYSTORE):$CONSULPORT/v1/catalog/nodes
)

# Manager HA
for NODENAME in $MANAGER m1
do
    (
	docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=$NODENAME" \
	    --engine-label "eu.hbp.function=manager" \
	    --engine-opt="cluster-store=consul://$(docker-machine ip $KEYSTORE):$CONSULPORT" \
	    --engine-opt="cluster-advertise=eth1:$MASTERPORT" $NODENAME
	eval $(docker-machine env $NODENAME)
	docker run --restart=unless-stopped -d -p $MASTERPORT:$MASTERPORT \
		--name swarm-controller \
		-v /var/lib/boot2docker:/certs:ro \
		swarm manage -H 0.0.0.0:$MASTERPORT \
		--tlsverify \
		--tlscacert=/certs/ca.pem \
		--tlscert=/certs/server.pem \
		--tlskey=/certs/server-key.pem \
		--replication --advertise $(docker-machine ip $NODENAME):$MASTERPORT \
		consul://$(docker-machine ip $KEYSTORE):$CONSULPORT
    )
done

# Start Shipyard (Web UI)
(
	eval $(docker-machine env $KEYSTORE)

	# Add Shipyard controller here as well
	docker run -d --restart=unless-stopped -d \
	    --name shipyard-rethinkdb \
	    rethinkdb

	docker run --restart=unless-stopped -d \
	    --name shipyard-controller \
	    --link shipyard-rethinkdb:rethinkdb \
	    -v /var/lib/boot2docker:/certs:ro \
	    -p $SHIPYARD:8080 \
	    shipyard/shipyard:latest \
	    server \
	    --tls-ca-cert=/certs/ca.pem \
	    --tls-cert=/certs/server.pem \
	    --tls-key=/certs/server-key.pem \
	    -d tcp://$(docker-machine ip $MANAGER):$MASTERPORT
)

# Slaves
for NODENAME in n0 n1 n2
do
    (
	docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=$NODENAME" \
	    --engine-label "eu.hbp.function=worker" \
	    --engine-opt="cluster-store=consul://$(docker-machine ip $KEYSTORE):$CONSULPORT" \
	    --engine-opt="cluster-advertise=eth1:$SLAVEPORT" $NODENAME
	eval $(docker-machine env $NODENAME)
	docker run -d --name swarm-agent swarm join --addr=$(docker-machine ip $NODENAME):$SLAVEPORT \
	    consul://$(docker-machine ip $KEYSTORE):$CONSULPORT
	docker-machine scp -r raw-admin $NODENAME:
	docker-machine scp -r data $NODENAME:
    )
done
