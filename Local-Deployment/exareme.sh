#!/usr/bin/env bash

#Check if EXAREME image exists
docker_image_exists() {
    curl -s -f https://index.docker.io/v1/repositories/$1/tags/$2 >/dev/null
}

echo -e "\nCreating file for Exareme image and Exareme tag.."

while true
do
    echo -e "\nType your EXAREME image name:"
    read name

    echo -e "\nType your EXAREME image tag:"
    read tag

    echo -e "\nChecking if EXAREME image: "\"${name}":"${tag}\"" exists"

    #docker image may exist in docker hub
    if docker_image_exists ${name} ${tag}; then
        echo "EXAREME Image exists. Continuing..."
        #If image exists in DockerHub write it to file exareme.yaml
        echo "EXAREME_IMAGE:" \"${name}\" >> exareme.yaml
        echo "EXAREME_TAG:" \"${tag}\" >> exareme.yaml
        break
    #or locally..
    elif [[ "$(sudo docker images -q ${name}:${tag} 2> /dev/null)" != "" ]]; then
        echo "EXAREME Image exists. Continuing..."
        #if image exists locally write it to file exareme.yaml
        echo "EXAREME_IMAGE:" \"${name}\" >> exareme.yaml
        echo "EXAREME_TAG:" \"${tag}\" >> exareme.yaml
        break
    else
        echo -e "\nEXAREME image does not exist! EXAREME image name should have a format like: \"hbpmip/exareme\". And EXAREME image tag should have a format like: \"latest\""
    fi
done


sleep 1
return