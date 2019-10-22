#!/usr/bin/env bash

# Including functions only
source ./updateFiles.sh include-only

tagExist=0
workerExist=0
workerVaultInfo=0

init_ansible_playbook

# Check worker's vault info in vault.yaml file
checkWorkerVaultInfos () {
    echo -e "\nChecking if vault information for target worker node with IP: \"${1}'s\" exist in vault.yaml..."

    ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    ${ansible_vault_decrypt}

    ansible_playbook_code=$?
    # If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Decryption of file \"../vault.yaml\" exited with error. Exiting..." >&2
        exit 1
    fi  # TODO check what happens if script fails at right this point! vault.yaml decrypted

    while IFS= read -r line || [[ -n "$line" ]]; do
        if [[ "$line" == *${2}* ]]; then
            echo "\"${2}'s\" vault information exist..."
            workerVaultInfo=1
            break
        else
            workerVaultInfo=0
        fi
    done < ../vault.yaml

    ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    ${ansible_vault_encrypt}

    ansible_playbook_code=$?
    # If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
         echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting..." >&2
         rm -rf ../vault.yaml
         exit 1
    fi

    if [[ ${workerVaultInfo} != "1" ]]; then
        echo -e "\nVault information for targer worker with IP: \"${1}'s\" does not exist.\nUpdating file for holding private information for target machines's now.. (vault.yaml)"

        workerVaultInfos ${1} ${2}
        ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
        ${ansible_vault_decrypt}

        ansible_playbook_code=$?
        # If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Decryption of file \"../vault.yaml\" exited with error. Exiting..." >&2
            exit 1
        fi

        echo -e "\n" >> ../vault.yaml
        echo ${var_remote_user} >> ../vault.yaml
        echo ${var_become_user} >> ../vault.yaml
        echo ${ssh_pass} >> ../vault.yaml
        echo ${become_pass} >> ../vault.yaml

        ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
        ${ansible_vault_encrypt}

        ansible_playbook_code=$?
        # If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting..." >&2
            rm -rf ../vault.yaml
            exit 1
        fi
    fi
}

echo -e "\nWhat is the IP of the target worker you would like to add to the exareme swarm information files?"
read answer

checkIP ${answer}
workerIP=${answer}
workerName="worker"${workerIP}
workerName=${workerName//./_}

while IFS= read -r line || [[ -n "$line" ]]; do
    if [[ "$line" == *"[workers]"* ]]; then
        tagExist=1                          #[workers] tag exists
        while IFS= read -r line1 || [[ -n "$line1" ]]; do
            worker=$(echo "$line1")
            if [[ ${workerName} != ${worker} ]]; then
                continue
            else                            #workerN exists below [workers] tag
                workerExist=1
                break
            fi
            if [[ -z "$line1" ]]; then
                continue                    #If empty line continue..
            fi
        done
    fi
done < ../hosts.ini; echo;

# [workers] tag does not exist. Create everything
if [[ ${tagExist} != "1" ]]; then

    # Add worker in hosts.ini file
    workerExist=1
    echo -e "\nIt seems that no information for target [workers] exist. Updating target machines' information (hosts.ini)..."
    echo -e "\n[workers]" >> ../hosts.ini
    echo ${workerName} >> ../hosts.ini
    workerHostsInfo ${workerIP} ${workerName}
	
    # Check if info for worker exist in vault.yaml file
    checkWorkerVaultInfos ${workerIP} ${workerName}
fi

# [workers] tag exist [workerN] tag does not exist
if [[ ${workerExist} != "1" ]]; then
    echo -e "\nIt seems that no information for target worker with IP: \"${workerIP}\" exist. Do you wish to add the worker now? [ y/n ]"
    read answer
    while true
    do
        if [[ ${answer} == y ]]; then
            # Add worker in hosts.ini, join worker in Swarm, Start Exareme in worker
            echo -e "\nUpdating target machines' information (hosts.ini)...."
            . ./updateHosts.sh
            workerHostsInfo ${workerIP} ${workerName}
						
            # Check if info for worker exist in vault.yaml file
            checkWorkerVaultInfos ${workerIP} ${workerName}
            break
			
        elif [[ ${answer} == "n" ]]; then
            echo -e "Make sure you include manually all the neccessary target machines' information (hosts.ini). Exiting...\n"
            sleep 2
            break
			
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
			
        fi
    done
fi

break
