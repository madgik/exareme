#!/usr/bin/env bash

EXAREME_HOME="$HOME/exareme"
. ./exareme-env.sh  &> /dev/null
mkdir -p /tmp/demo/db
source /etc/profile
EXAREME_ADMIN_JMX_PORT=10000
        EXAREME_ADMIN_CLASS_PATH="$EXAREME_HOME/lib/exareme/*:$EXAREME_HOME/lib/exareme/external/*"
        EXAREME_ADMIN_WORKER_CLASS="madgik.exareme.worker.admin.StartWorker"
        EXAREME_ADMIN_OPTS="${EXAREME_JAVA_OPTS}  \
                -Djava.rmi.server.codebase=file:$EXAREME_HOME/lib/exareme/                      \
                -Djava.security.policy=$EXAREME_HOME/etc/exareme/art.policy         \
                -Djava.rmi.server.hostname=$MY_IP                      \
                -Dcom.sun.management.jmxremote.port=$EXAREME_ADMIN_JMX_PORT         \
                -Dcom.sun.management.jmxremote.authenticate=false                   \
                -Dcom.sun.management.jmxremote.ssl=false                            \
                -Djava.security.egd=file:///dev/urandom "

DESC="exareme-worker"
EXAREME_ADMIN_CLASS=$EXAREME_ADMIN_WORKER_CLASS
EXAREME_ADMIN_CLASS_ARGS=$MASTER_IP
echo "BB"
echo $EXAREME_ADMIN_CLASS_PATH
echo $EXAREME_JAVA
echo $EXAREME_ADMIN_CLASS
echo $EXAREME_ADMIN_CLASS_ARGS
echo "CC"

mkdir -p /tmp/exareme/var/log /tmp/exareme/var/run

$EXAREME_JAVA -cp $EXAREME_ADMIN_CLASS_PATH \
$EXAREME_ADMIN_OPTS $EXAREME_ADMIN_CLASS  \
$EXAREME_ADMIN_CLASS_ARGS > /tmp/exareme/var/log/$DESC.log 2>&1 & echo $! > /tmp/exareme/var/run/$DESC.pid


echo "$DESC started."
