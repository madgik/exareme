Note:</br>
This is a modified Documentation from previous work that had been done by DIAS-EPFL.</br>
Initial source can be found here: https://github.com/HBPMedical/mip-federation/tree/master/Documentation

In this document you will find the specifications that must be followed in order to have a Federation (Manager node) or be part of one (Worker nodes).

# MIP Federation specifications

Contents:

- [Overview of the Federation](#overview-of-the-federation)
- [MIP Federated requirements](#mip-federated-requirements)
- [Troubleshooting](#troubleshooting)
- [Behaviour in case of failure](#behaviour-in-case-of-failure)
- [Security](#security)

## Overview of the Federation

The MIP Federation allows to connect multiple nodes securely over the web, so that privacy-aware analysis and queries on the data hosted at the Federation nodes can be performed in a distributed manner from the Federation manager, using the Exareme software.

### Federation architecture

The Federation is composed of one or more Manager nodes, and of any number of Worker nodes, usually hospitals sharing data on the Federation.

The Manager node of Swarm will run Docker engine (as all other nodes of Swarm) and create the Docker Swarm (standard Docker functionality).

The Manager node of Swarm will host the following Federation elements:

- Web Portal (container run locally)
- Consul (container run on the swarm. ```Optional``` service published on port 8500)
- (Optional) Portainer (UI for swarm management, container run on the swarm, service published on port 9000)
- Exareme Master (container run on the swarm, service published on port 9090)

For Worker nodes of Swarm:

- Each server dedicated to the Federation will have an internet access.
- Each server dedicated to the Federation (or more accurately its Docker engine instance) will join the Docker Swarm. (```Manually``` by each Hospital if credentials for the server ```will not/can not``` be given to the administrators or ```Automatically``` by scripts run by administrators.).
- The Manager node of Swarm will remotely start an Exareme worker on the node.

The software Exareme will expose federated analysis functionalities to the Web Portal. Exareme provides several algorithms that can be performed over the data distributed in multiple nodes. Exareme algorithms retrieve only aggregated results from each node (no individual patient data will leave the servers of the MIP partners). Exareme then combines the partial results in a statistically significant manner before returning results to the Web Portal.

### Regarding Docker swarm

As written in the official documentation, "Docker includes a _swarm mode_ for natively managing a cluster of Docker Engines called a _swarm_". The Docker swarm functionality creates a link among distant Docker engines. A Docker engine can only be part of one swarm, so all the Docker Engine instances running on the Federation servers will be part of the Docker Swarm. (In other words, the Federation servers cannot be part of another swarm, assuming the normal and recommended setup where only one Docker engine runs on each server.)

The swarm is created by the Manager node of Swarm; other nodes will join as Worker nodes of Swarm. The Manager node of Swarm will create a `mip-federation` network shared by the swarm nodes. All communications on this network will be encrypted using the option `--opt encrypted`.

Docker containers can be run in two ways: 

- On the swarm. To run on the swarm, the containers must be started **from the Manager node of Swarm**. This means that all Exareme containers (Master and Worker instances) will be started from the Manager node of Swarm.
- Outside the swarm. Docker containers running outside the swarm can be started locally as usual on the worker nodes. In that case, docker containers **cannot join the swarm for security reasons**. 

## MIP Federated requirements

### Manager node of Swarm server requirements

- Static public IP
- Network configuration:
  * TCP: ports 2377 and 7946 must be open and available
  * UDP: ports 4789 and 7946 must be open and available
  * IP protocol 50 (ESP) must be enabled

- If the configuration uses a whitelist of allowed IP addresses, the IP of all other Federation nodes must be authorised.

### Workers of Swarm requirements

- Static public IP
- Network configuration:
  * TCP: port 7946 must be open and available
  * UDP: ports 4789 and 7946 must be open and available
  * IP protocol 50 (ESP) must be enabled

## Troubleshooting

#### TCP and UDP ports

The netcat utility can help to check the connections from one Federation server to another (and in particular from the Federation manager):

- Testing that the UDP ports are open: ```nc -z -v -w1 -u <host> <port>```
- Testing that the TCP ports are open: ```nc -z -v -w1 -t <host> <port>```

As an example, here is for all the ports needed by the platform:

```sh
nc -z -v -w1 -t <host> 2377 # if <host> is a manager node
nc -z -v -w1 -t <host> 7946 # for all nodes
nc -z -v -w1 -u <host> 4789 # for all nodes
nc -z -v -w1 -u <host> 7946 # for all nodes
```

**Note:** netcat is not installed by default on RHEL; it can be done with the command `sudo yum install nc`. The -z option is not available on RHEL: simply run the commands above without it.

**Note 2:** Alternatively, if you are using `bash` as your command line shell and it was build with the support for it; tcp ports opening can be checked with this command (change tcp for udp to check udp ports):

```
</dev/tcp/<host>/<port> && echo "Port is open and docker is running" || echo "Port is closed and/or docker is not running"
```

#### IP protocol 50

If the firewall configuration for tcp and udp ports is correct, but IP protocol 50 (ESP) is not enabled at a node, it will be possible to start the Exareme master and workers, but they will not be able to communicate among themselves. This is because the protocol 50 is used when securing overlay networks over the swarm, which is used for communications among Exareme instances.

#### iptables interferences

As Docker documentation states:
> On Linux, Docker manipulates iptables rules to provide network isolation. This is an implementation detail, and you should not modify the rules Docker inserts into your iptables policies.

In case of network configuration issues, make sure that the IP tables rules were not modified manually or through scripts. This can interfere with Docker configuration or even prevent Docker to define the needed configuration.

#### IP Masquerade

If this is not the case by default, IP Masquerade must be enabled. When running `firewalld` on a federation server, it can be enabled with the following commands:

```
firewall-cmd --zone=public --permanent --add-masquerade
firewall-cmd --reload
```

## Behaviour in case of failure

The Swarm functionality of Docker is meant to orchestrate tasks in an unstable environment: "Swarm is resilient to failures and the swarm can recover from any number of temporary node failures (machine reboots or crash with restart) or other transient errors."

If a node crashes or reboots for any reason, docker will re-join the swarm automatically when restarted. The manager will then restart the missing services on the swarm and try and restore the previous status as soon as possible.

The swarm cannot recover if it definitively loses its manager (or quorum of manager) because of "data corruption or hardware failures". In this case, the only option will be to remove the previous swarm and build a new one, meaning that each node will have to perform a "join" command again, unless the docker swarm folders were properly [backed up](#back-up-the-swarm).

To increase stability, the manager role can be duplicated on several nodes (including worker nodes). For more information, see Docker documentation about [adding a manager node](https://docs.docker.com/engine/swarm/join-nodes/#join-as-a-manager-node") and [fault tolerance](https://docs.docker.com/engine/swarm/admin_guide/#add-manager-nodes-for-fault-tolerance").

A Worker node can leave the Swarm using this command:

```
docker swarm leave
```

The Manager node of Swarm must then remove that node from the known workers:

```
docker node ls
docker node rm <hostname>
```

## Security

This section documents a few elements regarding security.

### Swarm join tokens

The tokens allowing one node to join the swarm as a worker or a manager should not be made public. Joining the swarm as a manager, in particular, allows one node to control everything on the swarm. Ideally, the tokens should not leave the manager node except when a new node must join the swarm. There is no need to store these token somewhere else, as they can always be retrieved from the manager node running the command: ```docker swarm join-token manager```

Furthermore, the tokens can be changed (without impacting the nodes already in the swarm), following the documentation available <a href="https://docs.docker.com/engine/swarm/swarm-mode/#view-the-join-command-or-update-a-swarm-join-token">here</a>. It is recommended to rotate the tokens on a regular basis to improve security. (not implemented yet)

### Back up the Swarm

Note: Not implemented yet

See the official documentation <a href="https://docs.docker.com/engine/swarm/admin_guide/#back-up-the-swarm">here</a>.
