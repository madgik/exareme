#!/usr/bin/env bash

# Stop Exareme Services
stopExaremeService () {
    if [[ ${1} == "1" ]]; then
        echo -e "\nStopping Exareme services..."

        ansible_playbook_stop=${ansible_playbook}"../Stop-Services.yaml --skip-tags portainer"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    elif [[ ${1} == "2" ]]; then
        echo -e "\nStopping Portainer services..."

        ansible_playbook_stop=${ansible_playbook}"../Stop-Services.yaml --skip-tags exareme"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    elif [[ ${1} == "3" ]]; then
        echo -e "\nStopping all services..."

        ansible_playbook_stop=${ansible_playbook}"../Stop-Services.yaml"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    else
        echo "$answer is not a valid answer! Try again.. [ 1-2-3 ]"
        read answer
    fi
}

echo -e "\nChoose one of the options [ 1-2-3 ] :"
echo "1: Stop Exareme."
echo "2: Stop Portainer."
echo -e "3: Stop Exareme and Portainer.\n"

read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        # Stop Exareme services
        stopExaremeService 1
        break
    elif [[ "${answer}" == "2" ]]; then
        # Stop Portainer service
        stopExaremeService 2
        break
    elif [[ "${answer}" == "3" ]]; then
        # Stop Exareme services and Portainer
        stopExaremeService 3
        break
    else
        echo "$answer is not a valid answer! Try again.. [ 1-2-3 ]"
        read answer
    fi
done