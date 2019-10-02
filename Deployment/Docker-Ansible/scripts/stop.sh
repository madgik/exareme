#!/usr/bin/env bash

init_ansible_playbook

echo -e "\nChoose one of the options [ 1-2-3 ] :"
echo "1. Stop Exareme"
echo "2. Stop Portainer"
echo -e "3. Stop Exareme and Portainer\n"

read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        stop 1
        break
    elif [[ "${answer}" == "2" ]]; then
        stop 2
        break
    elif [[ "${answer}" == "3" ]]; then
        stop 3
        break
    else
        echo "$answer is not a valid answer! Try again.. [ 1-2-3 ]"
        read answer
    fi
done