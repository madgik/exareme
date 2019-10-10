#!/usr/bin/env bash

init_ansible_playbook

# Join worker in Swarm
joinWorker () {
    ansible_playbook_join=${ansible_playbook}"../Join-Workers.yaml -e my_host="
    ansible_playbook_join+=${1}

    ${ansible_playbook_join}
    ansible_playbook_code=$?
    # If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Playbook \"Join-Workers.yaml\" exited with error." >&2
        exit 1
    fi
    echo -e "\n${1} is now part of the Swarm..\n"
    sleep 1
}

# Start worker
startWorker () {

    # Start specific worker Exareme node
    echo -e "\nStarting Exareme for worker node ${1}"

    ansible_playbook_start=${ansible_playbook}"../Start-Exareme-Worker.yaml -e my_host="${1}
    ${ansible_playbook_start}

    ansible_playbook_code=$?
    # If status code != 0 an error has occurred

    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Playbook \"Start-Exareme-Worker.yaml\" exited with error." >&2
        exit 1
    fi

    echo -e "\nExareme service is now running.."
}

echo -e "\nWhat is the name of the worker node on which you want to start Exareme?"
read workerName

while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ "$line" == *"[workers]"* ]]; then
        while IFS= read -r line1 || [ -n "$line1" ]; do
            worker=$(echo "$line1")
            if [[ ${workerName} != ${worker} ]]; then
                continue
            else                            #workerN exists in hosts.ini
				joinWorker ${workerName}
				startWorker ${workerName}
                return
            fi
        done
		echo -e "\nCould not find the worker. Did not start any service. You can add it from the menu."
    fi
done < ../hosts.ini; echo;
