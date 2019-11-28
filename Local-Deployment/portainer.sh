#!/usr/bin/env bash

test -d ${PORTAINER_DATA} \
        || sudo mkdir -p ${PORTAINER_DATA} \
        || ( echo Failed to create ${PORTAINER_DATA}; exit 1 )

echo -e "\nCreating a new instance of ${PORTAINER_NAME}.."

sudo docker run -d -p ${PORTAINER_PORT}:9000 \
	-v /var/run/docker.sock:/var/run/docker.sock \
	-v ${PORTAINER_DATA}:/data \
	-v /etc/letsencrypt/live/${DOMAIN_NAME}:/certs/live/${DOMAIN_NAME}:ro \
	-v /etc/letsencrypt/archive/${DOMAIN_NAME}:/certs/archive/${DOMAIN_NAME}:ro \
	--name ${PORTAINER_NAME} \
	${PORTAINER_IMAGE}${PORTAINER_VERSION} \
	--ssl --sslcert /certs/live/${DOMAIN_NAME}/cert.pem --sslkey /certs/live/${DOMAIN_NAME}/privkey.pem