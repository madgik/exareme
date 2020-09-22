#!/usr/bin/env bash

EXAREME_HOME="$HOME/exareme"
. ./exareme-env.sh  &> /dev/null
mkdir -p /tmp/demo/db
source /etc/profile
EXAREME_ADMIN_JMX_PORT=10000
EXAREME_ADMIN_CLASS_PATH="$EXAREME_HOME/lib/exareme/*:$EXAREME_HOME/lib/exareme/external/*"
EXAREME_ADMIN_WORKER_CLASS="madgik.exareme.worker.admin.StartWorker"
EXAREME_ADMIN_OPTS="${EXAREME_JAVA_OPTS}  \
    -Djava.rmi.server.codebase=file:$EXAREME_HOME/lib/exareme/ \
    -Djava.security.policy=$EXAREME_HOME/etc/exareme/art.policy\
    -Djava.rmi.server.hostname=$NODE_IP                          \
    -Dsun.rmi.activation.execTimeout=$NODE_COMMUNICATION_TIMEOUT    \
    -Dsun.rmi.activation.groupTimeout=$NODE_COMMUNICATION_TIMEOUT   \
    -Dsun.rmi.dgc.ackTimeout=$NODE_COMMUNICATION_TIMEOUT            \
    -Dsun.rmi.transport.tcp.readTimeout=$NODE_COMMUNICATION_TIMEOUT \
    -Dcom.sun.management.jmxremote.port=$EXAREME_ADMIN_JMX_PORT\
    -Dcom.sun.management.jmxremote.authenticate=false          \
    -Dcom.sun.management.jmxremote.ssl=false                   \
    -Djava.security.egd=file:///dev/urandom "

EXAREME_ADMIN_CLASS=${EXAREME_ADMIN_WORKER_CLASS}
EXAREME_ADMIN_CLASS_ARGS=${MASTER_IP}

mkdir -p /tmp/exareme/var/log /tmp/exareme/var/run

${EXAREME_JAVA} -cp ${EXAREME_ADMIN_CLASS_PATH} \
${EXAREME_ADMIN_OPTS} ${EXAREME_ADMIN_CLASS}  \
${EXAREME_ADMIN_CLASS_ARGS} > /var/log/exareme.log 2>&1 & echo $! > /tmp/exareme/var/run/exareme-worker.pid


echo "Worker started."
