#!/usr/bin/env bash

init_ansible_playbook

n=0

while IFS= read -r line; do
    n=$[${n}+1]
    if [[ "$line" = *"[workers]"* ]]; then
        while IFS= read -r line; do
            if [[ -z "$line" ]]; then
                continue        #If empty line continue..
            fi
            n=$[$n+1]
            ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
            worker=$(echo "$line")

            if [[ "$line" == *"["* ]]; then
                sed -i ${n}'i'${workerName} ../hosts.ini
                break
            else
                :
            fi
        done
    fi
done < ../hosts.ini

infoWorker ${workerName}