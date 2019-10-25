#!/usr/bin/env bash

workerStop=0

echo -e "\nWhat is the IP of the worker target node, in which Exareme service is running, that you want to stop?"
read answer
while true
do
    if [[ ${answer} =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        for i in 1 2 3 4; do
            if [[ $(echo "$answer" | cut -d. -f$i) -gt 255 ]]; then
                echo "$answer" | cut -d. -f$i
                echo -e "\n${answer} is not a valid IP. Try again..."
                read answer
            fi
        done
        break
    else
        echo -e "\n${answer} is not a valid IP. Try again..."
        read answer
    fi
done

while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ ${line} == *"ansible_host"* ]]; then
        ip=$(echo "$line" | cut -d '=' -f 2)
        if [[ ${ip} == ${answer} ]]; then
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