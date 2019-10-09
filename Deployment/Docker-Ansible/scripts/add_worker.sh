#!/usr/bin/env bash

tagExist=0
workerExist=0
workerVaultInfo=0

init_ansible_playbook

#Join worker in Swarm
joinWorker () {
    ansible_playbook_join=${ansible_playbook}"Join-Workers.yaml -e my_host="
    ansible_playbook_join+=${1}

    ${ansible_playbook_join}
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
    ${ansible_playbook_start}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred

    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Playbook \"Start-Exareme-Worker.yaml\" exited with error." >&2
        exit 1
    fi

    echo -e "\nExareme service is now running.."
}

#Check worker's vault info in vault.yaml file
checkWorkerVaultInfos () {
    echo -e "\nChecking if \"${1}'s\" vault infos exist in vault.yaml.."

    ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    ${ansible_vault_decrypt}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Decryption of file \"../vault.yaml\" exited with error. Exiting.." >&2
        exit 1
    fi  #TODO check what happens if script fails at right this point! vault.yaml decrypted

    while IFS= read -r line || [[ -n "$line" ]]; do
        if [[ "$line" == *${1}* ]]; then
            echo "\"${1}'s\" vault infos exists.."
            workerVaultInfo=1
            break
        else
            workerVaultInfo=0
        fi
    done < ../vault.yaml

    ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    ${ansible_vault_encrypt}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
         echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting.." >&2
         rm -rf ../vault.yaml
         exit 1
    fi

    if [[ ${workerVaultInfo} != "1" ]]; then
        echo "\"${1}'s\" vault infos does not exist..Updating file for holding private information for target machines's now.. (vault.yaml)"

        workerVaultInfos ${1}
        ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
        ${ansible_vault_decrypt}

        ansible_playbook_code=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Decryption of file \"../vault.yaml\" exited with error. Exiting.." >&2
            exit 1
        fi

        echo -e "\n" >> ../vault.yaml
        echo ${var_remote_user} >> ../vault.yaml
        echo ${var_become_user} >> ../vault.yaml
        echo ${ssh_user} >> ../vault.yaml
        echo ${become_pass} >> ../vault.yaml

        ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
        ${ansible_vault_encrypt}

        ansible_playbook_code=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting.." >&2
            rm -rf ../vault.yaml
            exit 1
        fi
    fi
}

echo -e "\nWhat is the name of the worker node you would like to join the Swarm?"
read workerName

while IFS= read -r line || [[ -n "$line" ]]; do
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
    #Check if info for worker exist in vault.yaml file
    checkWorkerVaultInfos ${workerName}

    #Add worker in hosts.ini, join worker in Swarm, Start Exareme in worker
    workerExist=1
    echo -e "\nIt seams that no infos for target [workers] exist.Updating target machines' information (hosts.ini)..."
    echo -e "\n[workers]" >> ../hosts.ini
    echo ${workerName} >> ../hosts.ini
    workerHostsInfo ${workerName}
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
            #Check if info for worker exist in vault.yaml file
            checkWorkerVaultInfos ${workerName}

            #Add worker in hosts.ini, join worker in Swarm, Start Exareme in worker
            echo -e "\nUpdating target machines' information (hosts.ini)...."
            . ./updateHosts.sh
            workerHostsInfo ${workerName}
            joinWorker ${workerName}
            startWorker ${workerName}
            break
        elif [[ ${answer} == "n" ]]; then
            echo -e "Make sure you include manually all the infos needed in target machines' information (hosts.ini)..Exiting...\n"
            sleep 2
            break
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
fi

break
