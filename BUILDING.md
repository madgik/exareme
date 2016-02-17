# Exareme

## Branches
* master: Compiled binary, ready to use.
* dev: The branch we are currently working on.

## Build

* mvn clean install
* docker build -t exareme .
* docker create --name=examaster -p 9090:9090 --hostname master.exareme.org \
    -w="/root/exareme" exareme /bin/bash \
    -c "./bin/exareme-admin.sh --start --local;tail -f ./var/log/exareme-master.log" 

