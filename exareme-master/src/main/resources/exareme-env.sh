#!/usr/bin/env bash

export EXAREME_HOME="$HOME"/exareme
export EXAREME_USER="$USER"
export EXAREME_MASTER_IP=$(< $EXAREME_HOME/etc/exareme/master)
export EXAREME_JAVA="$(which java)"
export EXAREME_JAVA_OPTS="-Xms512M -Xmx2046M"
export EXAREME_PYTHON="$(which python)"
export EXAREME_MADIS="$EXAREME_HOME/lib/madis/src/mterm.py"
export JAVA_HOME=$EXAREME_JAVA
export MADIS_PATH=$EXAREME_MADIS
