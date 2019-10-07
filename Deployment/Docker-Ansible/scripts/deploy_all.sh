#!/usr/bin/env bash

init_ansible_playbook

echo -e "\nInitializing Swarm..Initializing mip-federation network..Copying Compose-Files folder to Manager of Swarm..."
sleep 1

#Init_swarm
ansible_playbook_init=${ansible_playbook}"Init-Swarm.yaml"
${ansible_playbook_init}

ansible_playbook_code=$?

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Init-Swarm.yaml\" exited with error." >&2
    exit 1
fi

#Join_workers
echo -e "\nJoining worker nodes in Swarm..\n"
while IFS= read -r line; do
    if [[ "$line" = *"[workers]"* ]]; then
        while IFS= read -r line; do
            ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
            worker=$(echo "$line")
            if [[ -z "$line" ]]; then
                continue        #If empty line continue..
            fi
            if [[ "$line" = *"["* ]]; then
                break
            fi
            ansible_playbook_join+=${worker}
            flag=0
            ${ansible_playbook_join}

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
done < ../hosts.ini
if [[ ${flag} != "0" ]]; then
    echo -e "\nIt seems that no workers will join the Swarm. If you have workers \
make sure you included their names below label [workers], so Ansible will not Ignore them."
    echo -e "\nContinue? [ y/n ]"

    read answer
    while true
    do
        if [[ "${answer}" == "y" ]]; then
            echo "Continue without Workers.."
            break
        elif [[ "${answer}" == "n" ]]; then
            echo "Exiting...(Leaving Swarm for Master node).."
            ansible_playbook_leave=${ansible_playbook}"Leave-Master.yaml"

            ${ansible_playbook_leave}
            ansible_playbook_code=$?

            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Leave-Master.yaml\" exited with error." >&2
                exit 1
            fi
            exit 1
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
fi

#Start Exareme
echo -e "\nStarting Exareme services...Do you wish to run Portainer service as well [ y/n ]?"
read answer
while true
do
    if [[ "${answer}" == "y" ]]; then
        ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml"
        ${ansible_playbook_start}

        ansible_playbook_code=$?

        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services and Portainer service are now running"
        break
    elif [[ "${answer}" == "n" ]]; then
        ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml --skip-tags portainer"
        ${ansible_playbook_start}

        ansible_playbook_code=$?

        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services are now running"
        break
    else
        echo "$answer is not a valid answer! Try again.. [ y/n ]"
        read answer
    fi
done