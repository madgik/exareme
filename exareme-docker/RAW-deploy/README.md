# RAW-deploy
Deployment Scripts for the RAW DB Engine

This deployment downloads and starts a Docker container for PostgresRAW and another for PostgresRAW-UI. It has been developed on Ubuntu and not tested on other platforms.

PostgresRAW is an extended version of Postgresql allowing direct access and querying over raw files.
PostgresRAW files (including configuration files) are stored in the host folder given by environment variable "pg_data_root".

PostgresRAW-UI offers a web UI for PostgresRAW and automates detection and registration of raw files (sniffer). Files to be automatically added to the database must be moved to the host folder given by environment variable "raw_data_root".

The env.sh configuration file sets these two environment variables respectively to the "data" and "datasets" folders found in the same folder as this README file. This configuration can be modified in env.sh, or overwritten as described below. The chosen folders are mounted inside PostgresRAW and PostgresRAW-UI containers at fixed locations (/data and /datasets).


## Installation

1. Requirements:
   * docker compose 1.6.2+
   * docker engine 1.10+
   * htpasswd

2. Clone this repository:
  ```!sh
  git clone https://github.com/HBPSP8Repo/RAW-deploy.git
  ```

3. Generate an ```raw-admin/conf/.htpasswd``` file:
  ```!sh
  $ cd RAW-deploy
  $ htpasswd -c raw-admin/conf/.htpasswd <username>
  ```
  
  Provide a password when requested. This is a **weak security scheme**, make sure to set up appropriately your network.

4. Remove the ```datasets/remove.me``` file, and add your data in that folder, or alternatively, set raw_data_root like:
  ```!sh
  $ cd RAW-deploy
  $ raw_data_root=<path to data folder> ./start <options>
  ```

5. Start the RAW engine container and the associated containers with:
  ```!sh
  $ ./start.sh single up
  ```
  The docker images will be downloaded and started. You will have the admin interface available on http://localhost by default.

6. For more options and configuration, see the online help of ```docker-compose``` and ```start.sh```.
