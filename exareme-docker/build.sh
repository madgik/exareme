#!/bin/bash


#cp ../exareme-utils/src/main/resources/gateway.properties .
#sed -i '/composer.repository.path=/c\composer.repository.path=/root/mip-algorithms/' gateway.properties
#sed -i '/static.path/c\static.path=/root/exareme/static/' gateway.properties
#


docker build -t hbpmip/exaremelocal .