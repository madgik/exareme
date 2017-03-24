#!/bin/bash

LOCALORMIPALGORITHMS="repo" # local or repo

VersionOfMipAlgorithms="0b3d36712a30332a62ff2fc182792cb66261db45"

rm Dockerfile

if [ $LOCALORMIPALGORITHMS = "local" ]; then

    sed  "s/MIPALGORITHMSCONFIGURATION/ \
    RUN cp -R \/root\/exareme\/lib\/algorithms-dev \/root\/mip-algorithms    \
    /" ./Dockerfile.notready > Dockerfile

else
     sed  "s/MIPALGORITHMSCONFIGURATION/ \
        RUN  git clone https:\/\/github.com\/madgik\/mip-algorithms.git \/root\/mip-algorithms  #dd ;   \
        WORKDIR \/root\/mip-algorithms         ;     \
        RUN git reset --hard $VersionOfMipAlgorithms ;  \
        /" ./Dockerfile.notready > DockerTEMP

      tr ';' '\n' < DockerTEMP > Dockerfile

      rm DockerTEMP
fi





#uncomment to use the algorithms from the repo
#RUN  git clone https://github.com/madgik/mip-algorithms.git /root/mip-algorithms
#RUN rm /root/mip-algorithms/properties.json
#ADD properties.json /root/mip-algorithms/

docker build  -t exaremelocal .
