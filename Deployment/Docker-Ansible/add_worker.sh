#!/usr/bin/env bash


init_ansible_playbook

echo -e "\nWhat is the name of the worker node you would like to join the Swarm?"
read answer

while IFS= read -r line; do
    if [[ "$line" = *"[workers]"* ]]; then
        while IFS= read -r line; do
            ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
            worker=$(echo "$line")
            if [[ ${answer} != ${worker} ]]; then
                continue
            fi
            if [[ -z "$line" ]]; then
                continue        #If empty line continue..
            fi
            if [[ "$line" = *"["* ]]; then
                break
            fi
            ansible_playbook_join+=${worker}
            flag=0
            echo ${ansible_playbook_join}

            ansible_playbook_code=$?
            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Join-Workers.yaml\" exited with error." >&2
                exit 1
            fi
            echo -e "\n${worker} is now part of the Swarm..\n"
            sleep 1
        done
    fi
done < hosts.ini

#Join specific worker node in Swarm
if [[ ${flag} != "0" ]]; then
    echo -e "\nIt seems that no workers with name \"${answer}\" exist in host.ini file..Make sure you \
included name \"${answer}\" below label [workers], so Ansible will not Ignore it."
    echo -e "\nExiting.."
    exit 1
fi

#Start specific worker Exareme node
echo -e "\nStarting Exareme for worker node ${answer}"
echo ${ansible_playbook}"Start-Exareme-Worker.yaml -e "my_host=${answer}

ansible_playbook_code=$?
#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Start-Exareme-Worker.yaml\" exited with error." >&2
    exit 1
fi

echo -e "\nExareme service is now running.."