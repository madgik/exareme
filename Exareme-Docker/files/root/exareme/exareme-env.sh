#!/usr/bin/env bash

export EXAREME_USER="$USER" # TODO JC "root"
export EXAREME_JAVA="$(which java)"
export EXAREME_JAVA_OPTS="-Xms512M -Xmx2046M"
export EXAREME_PYTHON="$(which python)"
export EXAREME_MADIS="$EXAREME_HOME/lib/madis/src/mterm.py"
export MADIS_PATH=$EXAREME_MADIS
