#!/usr/bin/env bash

test -d ${PORTAINER_DATA} \
        || sudo mkdir -p ${PORTAINER_DATA} \
        || ( echo Failed to create ${PORTAINER_DATA}; exit 1 )


echo -e "\nCreating a new instance of ${PORTAINER_NAME}.."

#Secure Portainer
if [[ ${flag} == "1" ]]; then
    sudo docker service create \
    --publish mode=host,target=${PORTAINER_PORT},published=9000 \
    --constraint 'node.role == manager' \
    --detach=true --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
    --mount type=bind,src=${PORTAINER_DATA},dst=/data \
    --mount type=bind,src=/etc/letsencrypt/live/${DOMAIN_NAME},dst=/certs/live/${DOMAIN_NAME} \
    --mount type=bind,src=/etc/letsencrypt/archive/${DOMAIN_NAME},dst=/certs/archive/${DOMAIN_NAME} \
    --name ${PORTAINER_NAME} ${PORTAINER_IMAGE}${PORTAINER_VERSION} \
    --ssl --sslcert /certs/live/${DOMAIN_NAME}/cert.pem --sslkey /certs/live/${DOMAIN_NAME}/privkey.pem

#Non Secure
else
    sudo docker service create \
    --publish mode=host,target=${PORTAINER_PORT},published=9000 \
    --constraint 'node.role == manager' \
    --detach=true --mount type=bind,src=/var/run/docker.sock,dst=/var/run/docker.sock \
    --mount type=bind,src=${PORTAINER_DATA},dst=/data \
    --name ${PORTAINER_NAME} ${PORTAINER_IMAGE}${PORTAINER_VERSION}
fi
