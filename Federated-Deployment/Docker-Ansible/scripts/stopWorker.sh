#!/usr/bin/env bash

# Including functions only
source ./updateFiles.sh include-only

workerStop=0

echo -e "\nWhat is the IP of the worker target node, in which Exareme service is running, that you want to stop?"
read answer

checkIP ${answer}

while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ ${line} == *"ansible_host"* ]]; then
        node=$(echo "$line" | cut -d " " -f 1)
        ip=$(echo "$line" | cut -d '=' -f 2)
        if [[ ${ip} == ${answer} ]]; then
            if [[ ${node} == "master" ]]; then
                echo -e "\nThe target node you are trying to stop is the **Master node**. \
This operation can not be done..Returning to main menu.."
                return;
            fi
            workerStop=1
            name=$(echo "$line" | cut -d ' ' -f 1)

            echo -e "\nStopping Exareme service for worker \"${name}\" with IP \"${ip}\"..."
            ansible_playbook_stop=${ansible_playbook}"../Stop-Exareme-Worker.yaml -e my_host=${name}"
            ${ansible_playbook_stop}
            ansible_playbook_code=$?

            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Stop-Exareme-Worker.yaml\" exited with error." >&2
                exit 1
            fi
            break
        fi
    fi
done < ../hosts.ini

if [[ ${workerStop} != "1" ]]; then
    echo -e "\nNo worker with IP ${answer} exists. No Exareme service was stopped ..."
fi