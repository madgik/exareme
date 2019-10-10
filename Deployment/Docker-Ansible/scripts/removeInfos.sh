#!/usr/bin/env bash

init_ansible_playbook

workerStop=0

echo -e "\nYou chose to remove all the necessary info for a specific worker. Place here the IP of the worker: "
read IP
while true
do
    if [[ ${IP} =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        for i in 1 2 3 4; do
            if [[ $(echo "${IP}" | cut -d. -f$i) -gt 255 ]]; then
                echo "${IP}" | cut -d. -f$i
                echo -e "\n${IP} is not a valid IP. Try again.."
                read IP
            fi
        done
        break
    else
        echo -e "\n${IP} is not a valid IP. Try again.."
        read IP
    fi
done

echo "If an Exareme service is running in target worker with IP ${IP} it WILL be stopped. Do you want to continue? [ y/n ]"
read answer

while true
do
    if [[ ${answer} == "y" ]]; then
        while IFS= read -r line || [[ -n "$line" ]]; do
            if [[ ${line} == *"ansible_host"* ]]; then
                ip=$(echo "$line" | cut -d '=' -f 2)
                if [[ ${ip} == ${IP} ]]; then
                    workerStop=1
                    name=$(echo "$line" | cut -d ' ' -f 1)
                    break
                fi
            fi
        done < ../hosts.ini

    if [[ ${workerStop} == "1" ]]; then
            #TODO check if Exareme service exists and then ask if you want to continue
            #Stop worker
            echo -e "\nStopping Exareme service (if exists) for worker \"${name}\" with IP \"${ip}\"..."

            ansible_playbook_stop=${ansible_playbook}"../Stop-Exareme-Worker.yaml -e my_host=${name}"
            ${ansible_playbook_stop}
            ansible_playbook_code=$?

            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Stop-Exareme-Worker.yaml\" exited with error." >&2
                exit 1
            fi

            #Remove from hosts.ini
            echo -e "\nRemove information for worker target machine with IP ${ip}"

            sed -i "/$name/ d" ../hosts.ini

            #Remove from vault.yaml
            echo -e "\nRemove private information for worker target machine with IP ${ip}"

            ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
            ${ansible_vault_decrypt}

            ansible_playbook_code=$?
            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Decryption of file \"../vault.yaml\" exited with error. Exiting.." >&2
                exit 1
            fi

            sed -i "/$name/ d" ../vault.yaml


            ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
            ${ansible_vault_encrypt}

            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting.." >&2
                rm -rf ../vault.yaml
                exit 1
            fi
            break
        else
            echo "No worker with IP ${answer} found. None information will be removed."
            break
        fi
    elif [[ ${answer} == "n" ]]; then
        echo -e "\nNone information for worker ${name} with IP ${ip} will be removed.."
        break
    else
        echo "$answer1 is not a valid answer! Try again.. [ 1-2-3-4-5-6-7 ]"
        read answer1
    fi
done


