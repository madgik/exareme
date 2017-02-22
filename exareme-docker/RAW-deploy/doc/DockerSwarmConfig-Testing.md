% MI Bundle deployment

This Guide will present the requirements, installation and deployment procedure of the Medical Informatics Platform, using Docker Engine, for **testing** or **development**.

The Medical Informatics Hospital Bundle expects the following:

 * Discovery service
 * Global secure network between nodes
 * Docker containers support

There are many platform which can be used to setup a distributed cluster. There are extremely powerful platforms out there, but those usually trade off adaptability at the expense of simplicity.

The following specific products have been chosen as they provide the services required, while being as lightweight and simple as possible.

 * Docker Swarm (Cluster management)
 * Docker Compose (Container deployment management)
 * Consul (Discovery service and key store)

For extra confort, the following can be added:

 * Shipyard (Web UI)

# Platform Requirements

 1. Ubuntu server 16.04+ or RHEL 7.2+
 2. Docker v1.10+
 3. Docker-compose v1.6.2+
 4. Consul 

**The system installation is outside of the scope of this document.**

# Docker Compose

Docker compose is a tool which manages services composed of several micro-services, each packaged in its own docker container.

It provides a simple way of starting all the docker container required and specify their network connection and file accesses.

**The installation is required only on the machine used to control the deployment.**

```bash
# curl -L https://github.com/docker/compose/releases/download/1.6.2/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose && chmod +x /usr/local/bin/docker-compose
```

# Deployment using docker-machines (Sandbox)

The following names are assumed in this section:

 * `ks` is the name of the docker-machine runnnig Consul
 * `manager` is the name of the docker-machine runnnig the swarm master
 * `m0` is the name of the docker-machine runnnig the swarm master (with replication)
 * `m1` is the name of the docker-machine runnnig the swarm master (with replication)
 * `n0` is the name of the docker-machine runnnig a swarm node
 * `n1` is the name of the docker-machine runnnig a swarm node
 
Those names are arbitrary, and can be changed as you wish, just be sure to use them consistently.

Execute the following to be able to run the commands below:

```shell
$ CONSULPORT=8500
$ SLAVEPORT=2376
$ MASTERPORT=3376
# optionnaly
$ SHIPYARDPORT=8080
```

## Docker Machine

Docker Machine simplify the provision of virtual machines which can run docker containers.

```bash
# curl -L https://github.com/docker/machine/releases/download/v0.8.0-rc1/docker-machine-`uname 
-s`-`uname -m` >/usr/local/bin/docker-machine && chmod +x /usr/local/bin/docker-machine
```

## Using Docker Compose with Docker Swarm and Docker Machine

1. Point docker to the swarm manager.

  ```bash
  $ MASTERPORT=<the port to the swarm daemon> # In this tutorial 3376
  $ NODENAME=<name of the swarm master>
  $ eval "$(docker-machine env $NODENAME)"
  $ export DOCKER_HOST=tcp://$(docker-machine ip $NODENAME):$MASTERPORT
  ```

2. Start using Docker Compose as usual, for example assuming a `docker-compose.yml` file in the current directory:

  ```bash
  $ docker-compose up
  ```

## Docker Swarm, using a local keystore

### Keystore VM

1. Create a new virtual machine to run Consul, named `ks`.

  ```shell
  $ docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=ks" \
	    --engine-label "eu.hbp.function=keystore" \
	    ks

  ```

2. Setup the shell to point docker to the `ks` docker daemon.

  ```shell
  $ eval $(docker-machine env ks)
  ```

3. Start the Consul container.

  ```shell
  $ docker run --restart=unless-stopped -d -p $CONSULPORT:$CONSULPORT -h consul progrium/consul -server -bootstrap
  ```

4. Check Consul is running and reachable.

  ```shell
  $ curl $(docker-machine ip ks):$CONSULPORT/v1/catalog/nodes
  ```
#### Shipyard (Web UI)

This is installed in this tutorial on the same machines as the keystore, merely for simplification.

1. Start the RethinkDB container.

  ```shell
  docker run -d --restart=unless-stopped -d \
	    --name shipyard-rethinkdb \
	    rethinkdb
  ```

2. Start the Shipyard container.

  ```shell
  docker run -d --restart=unless-stopped -d \
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
  ```

3. Add Network port redirection in VirtualBox for the `ks` virtual machine, pointing to `$SHIPYARDPORT`.

4. Connect to the Web UI and login with `admin` and `shipyard`.

### Master VM – without replication

1. Create a new virtual machine to run the new Swarm master, named `manager`.

  ```bash
  $ docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=manager" \
	    --engine-label "eu.hbp.function=manager" \
	    --engine-opt="cluster-store=consul://$(docker-machine ip ks):$CONSULPORT" \
	    --engine-opt="cluster-advertise=eth1:$MASTERPORT" \
	    manager
  ```

2. Set the environment to point to the `manager` docker daemon.

  ```bash
  $ eval $(docker-machine env manager)
  ```

3. Start the Swarm agent daemon.

  ```bash
  $ docker run --restart=unless-stopped -d -p $MASTERPORT:$MASTERPORT \
	    -v /var/lib/boot2docker:/certs:ro \
	    swarm manage -H 0.0.0.0:$MASTERPORT \
	    --tlsverify \
	    --tlscacert=/certs/ca.pem \
	    --tlscert=/certs/server.pem \
	    --tlskey=/certs/server-key.pem \
	    consul://$(docker-machine ip ks):$CONSULPORT
  ```

### Master VM – with replication

In order to create two Swarm managers, named `m0` and `m1` do the follow two times, one with `NODENAME=m0`, and a second time with `NODENAME=m1`.

```bash
  $ NODENAME=mX
```

1. Create the virtual machine to run the new Swarm master.

  ```bash
  $ docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=$NODENAME" \
	    --engine-label "eu.hbp.function=manager" \
	    --engine-opt="cluster-store=consul://$(docker-machine ip ks):$CONSULPORT" \
	    --engine-opt="cluster-advertise=eth1:$MASTERPORT" \
	    $NODENAME
  ```

2. Set the environment to point to the swarm master docker daemon.

  ```bash
  $ eval $(docker-machine env $NODENAME)
  ```

3. Start it.

  ```bash
  $ docker run --restart=unless-stopped -d -p $MASTERPORT:$MASTERPORT \
	    -v /var/lib/boot2docker:/certs:ro \
	    swarm manage -H 0.0.0.0:$MASTERPORT \
	    --tlsverify \
	    --tlscacert=/certs/ca.pem \
	    --tlscert=/certs/server.pem \
	    --tlskey=/certs/server-key.pem \
	    --replication --advertise $(docker-machine ip $NODENAME):$MASTERPORT \
	    consul://$(docker-machine ip ks):$CONSULPORT
  ```

### Slave VM

In order to create a Swarm agents, do the follow once per agent, with `NODENAME=nX`.

For example for two agents `n0` and `n1` do the following two times, once with `NODENAME=n0`, and a seconde time with `NODENAME=n1`.

  ```bash
  $ NODENAME=nX
  ```

1. Create the virtual machine to run the new Swarm slave.

  ```bash
  $ docker-machine create -d virtualbox \
	    --engine-label "eu.hbp.name=$NODENAME" \
	    --engine-label "eu.hbp.function=worker" \
	    --engine-opt="cluster-store=consul://$(docker-machine ip ks):$CONSULPORT" \
	    --engine-opt="cluster-advertise=eth1:$SLAVEPORT" \
	    $NODENAME
  ```

2. Set the environment to point to the swarm slave docker daemon.

  ```bash
  $ eval $(docker-machine env $NODENAME)
  ```

3. Start it.

  ```bash
  $ docker run -d swarm join --addr=$(docker-machine ip $NODENAME):$SLAVEPORT \
	    consul://$(docker-machine ip ks):$CONSULPORT
  ```

## Docker Swarm, using the public keystore

1. Create one Controller VM (manager) and two worker VMs (n0 & n1).

  ```shell
  $ docker-machine create -d virtualbox manager
  $ docker-machine create -d virtualbox n0
  $ docker-machine create -d virtualbox n1
  ```

2. Point Docker to the manager VM docker daemon.

  ```shell
  $ eval $(docker-machine env manager)
  ```

3. Create a new docker Swarm instance. This will create a swarm token using the public docker servers.

  ```shell
  $ docker run --rm swarm create
  ```

  This will return a token like the following on the last line:
  `64afa494ac34a1a5b28234e335baab52`
  
  Then do:

  ```shell
  $ SWARMTOKEN=64afa494ac34a1a5b28234e335baab52
  ```

4. Start the Swarm manager with the newly created `SWARMTOKEN`

  ```shell
  $ docker run -d -p $MASTERPORT:$MASTERPORT \
        -t -v /var/lib/boot2docker:/certs:ro \
        swarm manage -H 0.0.0.0:$MASTERPORT \
        --tlsverify \
        --tlscacert=/certs/ca.pem \
        --tlscert=/certs/server.pem \
        --tlskey=/certs/server-key.pem \
        token://$SWARMTOKEN
  ```

5. Start `n0`

  ```shell
  $ eval $(docker-machine env n0)
  $ docker run -d swarm join --addr=$(docker-machine ip n0):$SLAVEPORT token://$SWARMTOKEN
  ```

5. Start `n1`

  ```shell
  $ eval $(docker-machine env n1)
  $ docker run -d swarm join --addr=$(docker-machine ip n1):$SLAVEPORT token://$SWARMTOKEN
  ```
  
6. Check you can access you new Swarm:

  ```shell
  $ DOCKER_HOST=$(docker-machine ip manager):$MASTERPORT
  $ docker info
  ```
