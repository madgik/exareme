# MIP Federation deployment scripts and documentation

This repository contains all documentation regarding the MIP Federation, and scripts automating its deployment.

## Overview

The MIP Federation allows to connect multiple MIP Local instances securely over the web, so that privacy-preserving analysis and queries from the Federation manager. The queries will be performed in a distributed manner over the data stored at the Federation nodes using the Exareme software.

Complete documentation of the Federation can be found in [MIP Federation specifications](https://github.com/HBPMedical/mip-federation/blob/master/Documentation/MIP_Federation_specifications.md).

The steps to deploy the Federation are the following: 

- Setup the manager node(s).
- Add the worker nodes.
- Add `name` labels to the nodes to allow proper assignation of the different services.
- Start *services*, which are described in docker-compose.yml files: Exareme, Consul, etc…

In the following we are going to use only one master node. More can be added for improved availability.

## Deployement

### Requirements

MIP Local should be installed on the nodes that will join the MIP Federation. To join a node without MIP Local, see section [Adding a node without MIP Local](#adding-a-node-without-mip-local).

The Federation manager server must have a fixed IP address; other nodes must have a public IP, ideally also fixed. The firewall must allow connections on several ports: see details in [Firewall configuration](https://github.com/HBPMedical/mip-federation/blob/master/Documentation/Firewall_configuration.md).

### Deploy the Federation

1. Create the manager node(s).

   ```sh
   $ sudo ./setupFederationInfrastructure.sh
   # If you have multiple network interfaces, you might need to specify on which one to publish the swarm:
   $ MASTER_IP=<The Ip where the swarm should be published> sudo ./setupFederationInfrastructure.sh
   ```
   The output will include the command to add a node to the swarm.

2. *Optional:* Start a web interface (Portainer) to Docker.

   ```sh
   $ ./portainer.sh
   ```
   You can contact it on `http://localhost:9000/` by default.

3. On each worker node (a.k.a node of the federation), run the swarm join command.

   ```sh
   $ sudo docker swarm join --token <Swarm Token> <Master Node URL>
   ```

   The command to execute on the worker node, including the `Swarm Token` and the `Master Node URL`, is provided when performing point 1. It can be obtained again at any time from the manager, with the following command:

   ```sh
   $ sudo docker swarm join-token worker
   To add a worker to this swarm, run the following command:

   docker swarm join --token SWMTKN-1-11jmbp9n3rbwyw23m2q51h4jo4o1nus4oqxf3rk7s7lwf7b537-9xakyj8dxmvb0p3ffhpv5y6g3 10.2.1.1:2377
   ```

4. Add informative name labels for each worker node, on the swarm master.

   ```sh
   $ sudo docker node update --label-add name=<Alias> <node hostname>
   ```

   * `<node hostname>` can be found with `docker node ls`
   * `<Alias>` will be used when bringing up the services and should be a short descriptive name.

5. Deploy the Federation service

   ```sh
   $ sudo ./start.sh <Alias>
   ```

   * `<Alias>` will be used when bringing up the services and should be a short descriptive name.
   * if you set `SHOW_SETTINGS=true` a printout of all the settings which will be used will be printed before doing anything.

## Settings

All the settings have default values, but you can change them by either exporting in your shell the setting with its value, or creating `settings.local.sh` in the same folder as `settings.sh`:

```sh
: ${VARIABLE:="Your value"}
```

**Note**: To find the exhaustive list of parameters available please take a look at `settings.default.sh`.

**Note**: If the setting is specific to a node of the federation, you can do this in `settings.local.<Alias>.sh` where `<Alias>` is the short descriptive name given to a node.

Settings are taken in the following order of precedence:

  1. Shell Environment, or on the command line
  2. Node-specific settings `settings.local.<Alias>.sh`
  3. Federation-specific `settings.local.sh`
  4. Default settings `settings.default.sh`


## Adding a node without MIP Local

The following are required on all nodes. This is installed by default as part of the MIP, but can be installed manually when MIP Local is not present.

1. Install docker and docker-compose

   ```sh
   $ sudo install-$OS.sh # OS=ubuntu or OS=redhat, depending on your system
   ```

2. Add your user to the `docker` group, so that you don't need to `sudo` all the time:

   ```sh
   $ sudo usermod -G docker -a <username>
   ```

3. If necessary, adapt the Database configuration options in `settings.local.sh`. Check `settings.default.ch` to see the databases which are currently used by default. You can extend the list as well.

4. If you want to use the research data, which is available only in private repositories:

   ```sh
   $ docker login registry.gitlab.com
   ```

5. Load the binary data with `./load_data.sh`.

6. Add in the folder pointed by `${DB_DATASETS}` your CSV files, or if you want to only provide access to research data, add:

   ```sh
   : ${DB_UI_FEDERATION_SOURCES:="mip_cde_features"}
   ```
   in `settings.local.sh` (You might have to create the file).

7. Start the database services with `./run_db.sh up -d`

8. Start the federation services as described above.
