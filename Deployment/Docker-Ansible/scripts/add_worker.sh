#!/usr/bin/env bash

tagExist=0
workerExist=0

init_ansible_playbook

#Join worker in Swarm
joinWorker () {
    ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
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

#Start worker
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

echo -e "\nWhat is the name of the worker node you would like to join the Swarm?"
read workerName

while IFS= read -r line || [ -n "$line" ]; do
    if [[ "$line" == *"[workers]"* ]]; then
        tagExist=1                          #[workers] tag exists
        while IFS= read -r line1 || [ -n "$line1" ]; do
            worker=$(echo "$line1")
            if [[ ${workerName} != ${worker} ]]; then
                continue
            else                            #workerN exists below [workers] tag
                workerExist=1
                joinWorker ${worker}
                startWorker ${worker}
                break
            fi
            if [[ -z "$line1" ]]; then
                continue                    #If empty line continue..
            fi
        done
    fi
done < ../hosts.ini; echo;

#[workers] tag does not exist. Create everything
if [[ ${tagExist} != "1" ]]; then
    workerExist=1
    echo -e "\nIt seams that no infos for target [workers] exist.Updating hosts.ini file.."
    echo -e "\n[workers]" >> ../hosts.ini
    echo ${workerName} >> ../hosts.ini
    infoWorker ${workerName}
    joinWorker ${workerName}
    startWorker ${workerName}
fi

#[workers] tag exist [workerN] tag does not exist
if [[ ${workerExist} != "1" ]]; then
    echo -e "\nIt seams that no infos for worker \"${workerName}\" exists..\
Do you wish to add infos needed in order to add the worker now? [ y/n ]"
    read answer
    while true
    do
        if [[ ${answer} == y ]]; then
            echo "Updating hosts.ini file..."
            . ./updateHosts.sh
            infoWorker ${workerName}
            joinWorker ${workerName}
            startWorker ${workerName}
            break
        elif [[ ${answer} == "n" ]]; then
            echo -e "Make sure you include manually all the infos needed in hosts.ini file.Exiting...\n"
            sleep 2
            break
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
fi

break
