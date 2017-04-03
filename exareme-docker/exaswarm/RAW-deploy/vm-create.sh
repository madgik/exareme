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
SHIPYARDPORT=9000
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
    ####ΕΧΑΡΕΜΕ####    
    echo "$(docker-machine ip $KEYSTORE):$CONSULPORT" > consul_url.conf
    ###############    
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
	    -p 8080:8080 \
	    shipyard/shipyard:latest \
	    server \
	    --tls-ca-cert=/certs/ca.pem \
	    --tls-cert=/certs/server.pem \
	    --tls-key=/certs/server-key.pem \
	    -d tcp://$(docker-machine ip $MANAGER):$MASTERPORT

	VBoxManage controlvm ks natpf1 "Shipyard,tcp,,$SHIPYARDPORT,,8080"
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

	# / is mounted from a tmpfs, and PostgreSQL seems not like it for its
	# log files. The only partition mounted in the VM is /mnt/sda1.
	docker-machine ssh $NODENAME 'mkdir -p shared/data \
		 && sudo chown 999 shared/data \
		 && sudo mv shared /mnt/sda1/shared'

	# Copy the proxy config, and create files for the logs with
	# appropriate rights & owner.
	docker-machine scp -r shared/raw-admin-$NODENAME $NODENAME:/mnt/sda1/shared/raw-admin
	docker-machine ssh $NODENAME 'mkdir /mnt/sda1/shared/raw-admin/logs \
		&& touch /mnt/sda1/shared/raw-admin/logs/access.log \
		&& touch /mnt/sda1/shared/raw-admin/logs/error.log \
		&& sudo chmod 666 /mnt/sda1/shared/raw-admin/logs/*.log'

	# Copy the datasets which are accessible  and exposed by the node.
	docker-machine scp -r shared/datasets-$NODENAME $NODENAME:/mnt/sda1/shared/datasets

	eval $(docker-machine env $NODENAME)
	docker run -d --name swarm-agent swarm join --addr=$(docker-machine ip $NODENAME):$SLAVEPORT \
	    consul://$(docker-machine ip $KEYSTORE):$CONSULPORT
    )
done
