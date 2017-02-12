#!/bin/sh
#                    Copyright (c) 2016-2016
#   Data Intensive Applications and Systems Labaratory (DIAS)
#            Ecole Polytechnique Federale de Lausanne
#
#                      All Rights Reserved.
#
# Permission to use, copy, modify and distribute this software and its
# documentation is hereby granted, provided that both the copyright notice
# and this permission notice appear in all copies of the software, derivative
# works or modified versions, and any portions thereof, and that both notices
# appear in supporting documentation.
#
# This code is distributed in the hope that it will be useful, but WITHOUT ANY
# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
# A PARTICULAR PURPOSE. THE AUTHORS AND ECOLE POLYTECHNIQUE FEDERALE DE LAUSANNE
# DISCLAIM ANY LIABILITY OF ANY KIND FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE
# USE OF THIS SOFTWARE.

# Node-specific config:
: ${pg_data_root:="${PWD}/data"}
: ${raw_data_root:="${PWD}/../datasets"}
: ${raw_admin_root:="${PWD}/raw-admin"}
export pg_data_root raw_data_root raw_admin_root

# Whole Swarm config
export POSTGRES_USER=mip
export POSTGRES_PASSWORD=s3cret
export POSTGRES_PORT=5432

export COMPOSE_PROJECT_NAME="mip"
#export docker_pg_data_f...="/data"
#export docker_raw_data_f...="/datasets"

export raw_admin_conf=${raw_admin_root}/conf/nginx.conf
export raw_admin_htpasswd=${raw_admin_root}/conf/.htpasswd
export raw_admin_log=${raw_admin_root}/logs
