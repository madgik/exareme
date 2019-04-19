#!/bin/sh
#                    Copyright (c) 2018-2018
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

# Import settings
. ./settings.sh

# Enable access to the extra repository
subscription-manager repos --enable=rhel-7-server-extras-rpms

# Following official instructions from:
#  https://docs.docker.com/install/linux/docker-ce/centos/

# Install docker, for CentOS/RHEL
sudo yum install -y yum-utils \
  device-mapper-persistent-data \
  lvm2

sudo yum-config-manager \
    --add-repo \
    https://download.docker.com/linux/centos/docker-ce.repo

# You might be requested to accept the key:
echo <<EOT
Docker Repository correct key is:
  060A 61C5 1B55 8A7F 742B 77AA C52F EB6B 621E 9F35

If prompted, check the key is correct and accept it.
EOT

sudo yum install docker-ce-17.09.1.ce
sudo yum install yum-versionlock
sudo yum versionlock docker-ce

sudo systemctl start docker

# Install docker-compose
sudo curl -L https://github.com/docker/compose/releases/download/1.18.0/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

cat <<EOT
Add you user to the docker group to be able to use the docker command without
administrator rights or sudo.

  sudo usermod -a -G docker $USER

EOT
