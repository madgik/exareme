#!/usr/bin/env bash


joinWorker () {
    ansible_playbook_join+=${1}
    echo ${ansible_playbook_join}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Playbook \"Join-Workers.yaml\" exited with error." >&2
        exit 1
    fi
    echo -e "\n${1} is now part of the Swarm..\n"
    sleep 1
}

startWorker () {

    #Start specific worker Exareme node
    echo -e "\nStarting Exareme for worker node ${1}"

    ansible_playbook_start=${ansible_playbook}"Start-Exareme-Worker.yaml -e my_host="${1}
    echo ${ansible_playbook_start}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred

    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Playbook \"Start-Exareme-Worker.yaml\" exited with error." >&2
        exit 1
    fi

    echo -e "\nExareme service is now running.."
}

echo -e "\nWhat is the name of the worker node, as given in the hosts.ini, you would like to join the Swarm?"
read workerName

while IFS= read -r line; do
    if [[ "$line" = *"[workers]"* ]]; then
        tagExist=1
        while IFS= read -r line; do
            ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
            worker=$(echo "$line")
            if [[ ${workerName} != ${worker} ]]; then
                continue
            fi
            if [[ -z "$line" ]]; then
                continue        #If empty line continue..
            fi
            if [[ "$line" = *"["* ]]; then
                break
            fi
            flag=0
            joinWorker ${worker}
            startWorker ${worker}
        done
    fi
done < hosts.ini
#[workers] tag does not exist. Create everything
if [[ ${tagExist} != "1" ]]; then
    flag=0
    echo "It seams that no infos for ${workerName} exists.."
    echo "[workers]" >> hosts.ini
    echo ${workerName} >> hosts.ini
    infoWorker ${workerName}
    joinWorker ${workerName}
    startWorker ${workerName}
fi

#[workers] tag exist.[name] tag for specific worker does not exist
if [[ ${flag} != "0" ]]; then
    echo -e "\nIt seems that no workers with name \"${workerName}\" exist in host.ini file..\
Do you wish to add infos needed in order to add the worker now? [ y/n ]"
    read answer
    while true
    do
        if [[ ${answer} == y ]]; then
            echo "Update hosts.ini file"
            chmod 755 scripts/updateHosts.sh
            . scripts/updateHosts.sh
            joinWorker ${workerName}
            startWorker ${workerName}
            break
        elif [[ ${answer} == "n" ]]; then
            echo -e "Make sure you included name \"${workerName}\" manually \
below label [workers], so Ansible will not Ignore it.Exiting...\n"
            sleep 2
            break
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
fi

break
