# Docker container for Exareme

This project creates a Docker image for running Exareme. It has been developed on Ubuntu 16.04 LTS and not tested on other platforms.

## Build procedure

1. Install build requirements
  ```
  sudo apt-get install -y git maven curl python python-apsw jq
  sudo apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
  ```

2. Clone the sources and build Exareme
  ```
  git clone -b mip https://github.com/madgik/exareme.git src/exareme
  (cd src/exareme && mvn clean install)
  ```

3. Retrieve the MIP Algorithms
  ```
  git clone https://github.com/madgik/mip-algorithms src/mip-algorithms
  ```

4. Building the Docker image
  ```
  docker build -t hbpmip/exareme .
  ```
