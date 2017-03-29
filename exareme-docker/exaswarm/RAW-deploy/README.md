# Query Engine Deployment scripts
Deployment Scripts for the Query Engine

This deployment downloads and starts a Docker container for PostgresRAW and another for PostgresRAW-UI. It has been developed on Ubuntu and not tested on other platforms.

PostgresRAW is an extended version of PostgreSQL allowing direct access and querying over raw files.
PostgresRAW files (including configuration files) are stored in the host folder given by environment variable "pg_data_root".

PostgresRAW-UI offers a web UI for PostgresRAW and automates detection and registration of raw files (sniffer). Files to be automatically added to the database must be moved to the host folder given by environment variable "raw_data_root".

The env.sh configuration file sets these two environment variables respectively to the "data" and "datasets" folders found in the same folder as this README file. This configuration can be modified in env.sh, or overwritten as described below. The chosen folders are mounted inside PostgresRAW and PostgresRAW-UI containers at fixed locations (/data and /datasets).

## Installation

1. Requirements:
   * docker compose 1.6.2+
   * docker engine 1.10+
   * htpasswd
   * docker-machine 0.8.0-rc1+ [swarm deployment in VMs]

2. Clone this repository:
  ```!sh
  git clone https://github.com/HBPSP8Repo/RAW-deploy.git
  ```

### Single instance, directly on the host

1. Generate an ```raw-admin/conf/.htpasswd``` file:
  ```!sh
  $ cd RAW-deploy
  $ htpasswd -c raw-admin/conf/.htpasswd <username>
  ```

  Provide a password when requested. This is a **weak security scheme**, make sure to set up appropriately your network.

2. Remove the ```datasets/remove.me``` file, and add your data in that folder, or alternatively, set raw_data_root like:
  ```!sh
  $ cd RAW-deploy
  $ raw_data_root=<path to data folder> ./start <options>
  ```

3. Start the RAW engine container and the associated containers with:
  ```!sh
  $ ./start.sh single up
  ```
  The docker images will be downloaded and started. You will have the admin interface available on http://localhost by default.

### Swarm deployment, using docker-machine

In order to test a distributed deployment between nodes, a test setup based on docker-machine has been put together. Several scripts are provided to manage the virtual machines, in order to simplify the management and setup. These scripts are based on docker-machine.
 * ```vm-create.sh```: create and copy datasets to the node machines
 * ```vm-destroy.sh```: destroy and purge the created virtual machines

By default, 3 VMs will be created to manage the distributed federation, a keystore (**ks**), two Docker Swarm managers, with high availability enabled (**m0**, **m1**), as well as three nodes (**n0**, **n1**, **n2**), which each represent a geographically distributed node of the network.
The creation script expects the following folder structure, one per node:
  ```
  .
  ├── datasets-n0
  │   ├── table1.csv
  │   └── table2.csv
  └── raw-admin-n0
      └── conf
          ├── .htpasswd
          └── nginx.conf
  ```
where ```n0``` is the name of a node. This folder hierachy is expected to be located in a subfolder named ```shared```, at the same level as the ```vm-createi.sh``` script.

1. Create the proxy configuration for the node:
  ```!sh
  $ cd RAW-deploy
  $ mkdir -p shared/raw-admin-<NodeName>/conf/
  $ cp raw-admin/conf/nginx.conf shared/raw-admin-<NodeName>/conf/
  $ htpasswd -c shared/raw-admin-<NodeName>/conf/.htpasswd <username>
  ```

  Provide a password when requested. This is a **weak security scheme**, make sure to set up appropriately your network.
  The username / password will be asked by the proxy before allowing access to the web interace.

2. Add your data in the folder ```shared/datasets-<NodeName>```

Repeat the steps above for each node.

3. Create the virtual machines:
  ```!sh
  $ ./vm-create.sh
  ```

  If for any reason the script does not complete success fully, use ```vm-destroy.sh``` and restart.

4. Start the query engine containers with:
  ```!sh
  $ ./start.sh swarm <NodeName> create
  $ ./start.sh swarm <NodeName> start
  ```
  The docker images will be downloaded, containers created, and then started.

5. To access the **Shipyard** WebUI for Docker Swarm,
  The script add a VirtualBox forwarding rule from host:SHIPYARDPORT to ks:8080 (guest VM machine), which allows you to access the Shipyard WebUI directly.

  You will be able to login with the default user & password (admin / shipyard)

6. To access the Query Engine administration UI of a specific node, you will need to setup port forwarding rules as such:
  ```!sh
  $ VBoxManage controlvm n0 natpf1 "raw,tcp,,9010,,80"
  $ VBoxManage controlvm n1 natpf1 "raw,tcp,,9011,,80"
  $ VBoxManage controlvm n2 natpf1 "raw,tcp,,9012,,80"
  ```
  For the nodes n0, n1 and n2

# Further steps

For more options and configuration, see the online help of ```docker-compose``` and ```start.sh```.

# Exareme Notes
1. node n2 is Exareme's master (see start.sh)
2. Exareme's master node needs master flag argument and number of workers to wait (see docker-compose-swarm-examaster.yml)
3. consul's url is stored on consul_url.conf file (see vm-create.sh and start.sh)
