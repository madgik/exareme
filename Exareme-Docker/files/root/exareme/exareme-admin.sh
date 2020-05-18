#!/usr/bin/env bash

# set up environment
####################################################################################################
#echo "Setting up environment..."
# set up home dir
if [[ -z ${EXAREME_HOME} ]]; then
    if [[ -d "$HOME/exareme" ]]; then
        EXAREME_HOME="$HOME/exareme";
        export EXAREME_HOME="$HOME/exareme";
    else
        EXAREME_HOME="$HOME/exareme";
        export EXAREME_HOME="$(pwd)";
    fi
fi
echo "EXAREME HOME DIR: $EXAREME_HOME";

#load environmental variables like JAVA, python
. ./exareme-env.sh  &> /dev/null

#maybe simply pass MASTER_IP from bootstrap
EXAREME_MASTER=`/sbin/ifconfig eth0 | grep "inet" | awk -F: '{print $2}' | cut -d ' ' -f 1`;
echo "EXAREME_HOST : $EXAREME_MASTER";
echo "EXAREME_USER: $EXAREME_USER";
####################################################################################################
# parse command line arguments
####################################################################################################
#echo "Parsing command line arguments..."
SCRIPT_NAME=$(basename $0)
function usage(){
    cat << EOF
    NAME
        "${SCRIPT_NAME}" - exareme administration script.

    SYNOPSIS
        "${SCRIPT_NAME}" [GENERIC_OPTIONS] OPTIONS [OPTIONS_PARAMETERS]

    OPTIONS [OPTIONS_PARAMETERS]

        --start                         - start daemons
        --kill                          - kill all java and python process
        --status                        - print daemons status
        --console                       - opens a madis console with exa* operators available.
EOF
}

TEMP=`getopt --options h \
             --long start,status,kill,console,help \
             -n $(basename "$0") -- "$@"`

if [[ $? != 0 ]]; then echo "Terminating..." >&2; exit 1; fi

eval set -- "${TEMP}"
EXAREME_ADMIN="" ;

while true; do
   case "$1" in
        --start|--console|--status|--kill)
            EXAREME_ADMIN="${1:2}"
            ;;
        -h|--help)
            usage
            exit 0 ;;
        --) shift
            break
            ;;
        *)
            echo "Internal error!"
            exit 1
            ;;
    esac
    shift;
done

if [[ -n "$1" ]]; then echo -e "Unresolved arguments:\n--> $1" ; exit 1; fi

####################################################################################################
# validate  command line arguments
####################################################################################################
#echo "Validating command line argumetns..."
if [[ -z ${EXAREME_ADMIN} ]]; then
    echo "Please provide one of the OPTIONS."
    echo "Use -h|--help to check available options."
    exit 1;
fi

####################################################################################################
# execute
####################################################################################################
function start_exareme(){               #Starts exareme daemon
    EXAREME_ADMIN_JMX_PORT=10000
    EXAREME_ADMIN_CLASS_PATH="$EXAREME_HOME/lib/exareme/*:$EXAREME_HOME/lib/exareme/external/*"
    EXAREME_ADMIN_MASTER_CLASS="madgik.exareme.master.admin.StartMaster"
    EXAREME_ADMIN_OPTS="${EXAREME_JAVA_OPTS}                        \
       -Djava.rmi.server.codebase=file:$EXAREME_HOME/lib/exareme/   \
       -Djava.security.policy=$EXAREME_HOME/etc/exareme/art.policy  \
       -Djava.rmi.server.hostname=$EXAREME_MASTER                   \
       -Dsun.rmi.activation.execTimeout=30000                       \
       -Dsun.rmi.activation.groupTimeout=30000                      \
       -Dsun.rmi.transport.connectionTimeout=30000                  \
       -Dsun.rmi.transport.proxy.connectTimeout=30000               \
       -Dsun.rmi.transport.tcp.handshakeTimeout=30000               \
       -Dsun.rmi.transport.tcp.responseTimeout=30000                \
       -Dcom.sun.management.jmxremote.port=$EXAREME_ADMIN_JMX_PORT  \
       -Dcom.sun.management.jmxremote.authenticate=false            \
       -Dcom.sun.management.jmxremote.ssl=false                     \
       -Djava.security.egd=file:///dev/urandom "

    DESC="exareme-master"
    EXAREME_ADMIN_CLASS=${EXAREME_ADMIN_MASTER_CLASS}

    echo ${EXAREME_ADMIN_CLASS_PATH}
    echo ${EXAREME_JAVA}
    echo ${EXAREME_ADMIN_CLASS}
    echo ${EXAREME_MASTER}

    mkdir -p /tmp/exareme/var/log /tmp/exareme/var/run

        $EXAREME_JAVA -cp $EXAREME_ADMIN_CLASS_PATH \
        $EXAREME_ADMIN_OPTS $EXAREME_ADMIN_CLASS > /var/log/exareme.log 2>&1 & echo $! > /tmp/exareme/var/run/$DESC.pid    #-cp requires class path specification

    exit 0

}

function kill_exareme(){              #kill exareme daemon. Be aware that kill_exareme will kill every java and python processes you have
    echo "killing all java and python ..."
    killall java python
    exit 0
}

function status_exareme(){            #status of EXAREME
    if [[ -e /tmp/exareme/var/run/*.pid ]]; then
        ps -f --pid $(cat /tmp/exareme/var/run/*.pid) | sed 1d
    else
        echo "Stopped."
    fi
    exit 0
}

function console_exareme(){
    ${EXAREME_PYTHON} ${EXAREME_HOME}/lib/madis/src/mterm.py
    exit 0
}

${EXAREME_ADMIN}_exareme
