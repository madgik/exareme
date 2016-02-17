#!/usr/bin/env bash
#
####################################################################################################


####################################################################################################
# set up environment
####################################################################################################
#echo "Setting up environment..."
[[ -z $EXAREME_HOME ]] && export EXAREME_HOME="$HOME"/exareme
. $EXAREME_HOME/etc/exareme/exareme-env.sh

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
        --stop                          - stop daemons
        --kill                          - kill all java and python process
        --status                        - print daemons status

        --install                       - installs exareme on workers
        --update                        - update exareme libraries and configurations on workers
        --uninstall                     - uninstalls exareme on workers

        --console                       - opens a madis console with exa* operators available.

    GENERIC_OPTIONS
        --sync                          - waits each node response
        --local                         - executes only locally
EOF
}

TEMP=`getopt --options h \
             --long start,stop,status,kill,console,install,update,uninstall,sync,local,help \
             -n $(basename "$0") -- "$@"`

if [ $? != 0 ]; then echo "Terminating..." >&2; exit 1; fi

eval set -- "$TEMP"

EXAREME_ADMIN="" ;
EXAREME_ADMIN_ARG=();
EXAREME_ADMIN_LOCAL=false;
EXAREME_ADMIN_SYNC=false;

while true; do
   case "$1" in
        --start|--stop|--status|--kill)
            EXAREME_ADMIN="${1:2}"
            ;;
        --install|--uninstall|--update|--console)
            EXAREME_ADMIN="${1:2}";
            EXAREME_ADMIN_LOCAL=true
        ;;
        --sync)
            EXAREME_ADMIN_SYNC=true
            ;;
        --local)
            EXAREME_ADMIN_LOCAL=true
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

if [ -n "$1" ]; then echo -e "Unresolved arguments:\n--> $1" ; exit 1; fi

####################################################################################################
# validate  command line arguments
####################################################################################################
#echo "Validating command line argumetns..."
if [ -z $EXAREME_ADMIN ]; then
    echo "Please provide one of the OPTIONS."
    echo "Use -h|--help to check available options."
    exit 1;
fi

####################################################################################################
# execute
####################################################################################################
if [[ "true" == $EXAREME_ADMIN_LOCAL ]]; then   # run locally
#    echo "Running in local mode..."

    function start_exareme(){ # starts exareme daemon
        # set env
        EXAREME_CURRENT_IP=$(
            ALL_IPS=$(/sbin/ifconfig $1 | grep "inet " | awk -F: '{print $2}' | awk '{print $1}')
            for CURRENT_NODE_IP in $ALL_IPS; do
                for NODE_IP in $(cat $EXAREME_HOME/etc/exareme/master) $(cat $EXAREME_HOME/etc/exareme/workers); do
                    if [[ X$NODE_IP == X$CURRENT_NODE_IP ]]; then
                        echo $NODE_IP
                        break 2
                    fi
                done
            done
        )
        EXAREME_ADMIN_JMX_PORT=10000
        EXAREME_ADMIN_CLASS_PATH="$EXAREME_HOME/lib/exareme/*:$EXAREME_HOME/lib/exareme/external/*"
        EXAREME_ADMIN_MASTER_CLASS="madgik.exareme.master.admin.StartMaster"
        EXAREME_ADMIN_WORKER_CLASS="madgik.exareme.worker.admin.StartWorker"
        EXAREME_ADMIN_OPTS="${EXAREME_JAVA_OPTS}  \
                -Djava.rmi.server.codebase=file:$EXAREME_HOME/lib/exareme/                      \
                            -Djava.security.policy=$EXAREME_HOME/etc/exareme/art.policy         \
                            -Djava.rmi.server.hostname=$EXAREME_CURRENT_IP                      \
                            -Dcom.sun.management.jmxremote.port=$EXAREME_ADMIN_JMX_PORT         \
                            -Dcom.sun.management.jmxremote.authenticate=false                   \
                            -Dcom.sun.management.jmxremote.ssl=false                            \
                            -Djava.security.egd=file:///dev/urandom "

        # determine master/worker
        if [[ "$EXAREME_MASTER_IP" == "$EXAREME_CURRENT_IP" ]]; then
            DESC="exareme-master"
            EXAREME_ADMIN_CLASS=$EXAREME_ADMIN_MASTER_CLASS
            EXAREME_ADMIN_CLASS_ARGS=""
        else
            DESC="exareme-worker"
            EXAREME_ADMIN_CLASS=$EXAREME_ADMIN_WORKER_CLASS
            EXAREME_ADMIN_CLASS_ARGS="$EXAREME_MASTER_IP"
        fi

        # execute
        mkdir -p $EXAREME_HOME/var/log $EXAREME_HOME/var/run
        $EXAREME_JAVA -cp $EXAREME_ADMIN_CLASS_PATH $EXAREME_ADMIN_OPTS $EXAREME_ADMIN_CLASS\
            $EXAREME_ADMIN_CLASS_ARGS > $EXAREME_HOME/var/log/$DESC.log 2>&1 & echo $! > $EXAREME_HOME/var/run/$DESC.pid
        echo "$DESC started."
    }

    function stop_exareme(){
        if [ -f $EXAREME_HOME/var/run/*.pid ]; then
            kill -9 $( cat $EXAREME_HOME/var/run/*.pid)
            rm $EXAREME_HOME/var/run/*.pid
            echo "Stopped."
        else
            echo "Already stopped, no action taken."
        fi
    }

    function kill_exareme(){
        echo "killing all java and python ..."
        killall java python
    }

    function status_exareme(){
        if [ -e $EXAREME_HOME/var/run/*.pid ]; then
            ps -f --pid $(cat $EXAREME_HOME/var/run/*.pid) | sed 1d
        else
            echo "Stopped."
        fi
    }

    function install_exareme(){     # only workers
        for EXAREME_NODE in $(cat $EXAREME_HOME/etc/exareme/workers); do 
            rsync -aqvzhe ssh --delete  --exclude="var/*" $EXAREME_HOME/ $EXAREME_USER@$EXAREME_NODE:$EXAREME_HOME/ &
        done

        # Wait for all parallel jobs to finish
        for job in `jobs -p`; do
            wait $job
        done
    }

    function update_exareme(){      # only workers
        for EXAREME_NODE in $(cat $EXAREME_HOME/etc/exareme/workers); do
            rsync -avqzhe ssh --delete  --exclude="var/*" $EXAREME_HOME/ $EXAREME_USER@$EXAREME_NODE:$EXAREME_HOME/ &
        done

        # Wait for all parallel jobs to finish
        for job in `jobs -p`; do
            wait $job
        done
    }

    function uninstall_exareme(){   # only workers
        for EXAREME_NODE in $(cat $EXAREME_HOME/etc/exareme/workers); do
            ssh -n $EXAREME_USER@$EXAREME_NODE """rm -rf $EXAREME_HOME""" &
        done

        # Wait for all parallel jobs to finish
        for job in `jobs -p`; do
            wait $job
        done
    }

    function console_exareme(){
        $EXAREME_PYTHON $EXAREME_HOME/lib/madis/src/mterm.py
    }

    ${EXAREME_ADMIN}_exareme $EXAREME_ADMIN_ARG
else
#    echo "Running in distributed mode..."
    CMD_RUN="$EXAREME_HOME/bin/$(basename $0) --$EXAREME_ADMIN $EXAREME_ADMIN_ARG --local"

#    ssh -n $EXAREME_USER@$EXAREME_MASTER_IP """$CMD_RUN"""
    $CMD_RUN
    sleep 1

    if [[ "true" != $EXAREME_ADMIN_SYNC ]]; then
        for EXAREME_NODE in $(cat $EXAREME_HOME/etc/exareme/workers); do
            ssh -n $EXAREME_USER@$EXAREME_NODE """$CMD_RUN""" &
        done
        # Wait for all parallel jobs to finish
        for job in `jobs -p`; do
            wait $job
        done
    else
        for EXAREME_NODE in $(cat $EXAREME_HOME/etc/exareme/workers); do
            ssh -n $EXAREME_USER@$EXAREME_NODE """$CMD_RUN"""
        done
    fi
fi
#echo "Terminated."
exit 0
