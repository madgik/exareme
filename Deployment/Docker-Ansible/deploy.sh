#!/usr/bin/env bash

echo "Do you wish to copy Metadata file to Master node now? [y/n]. If [y] make sure you have the file \"CDEsMetadata.json\" inside Metadata folder"
read answer
while true
do
    if [[ "${answer}" == "y" ]]; then
        echo -e "\nCopying Metadata file to Master node.."
        ansible-playbook -i hosts.ini Metadata-Master.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml
        ansible_playbook=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook} -ne 0 ]]; then
            echo "Playbook \"Metadata-Master.yaml\" exited with error." >&2
            exit 1
        fi
        break
    elif [[ "${answer}" == "n" ]]; then
        echo -e "\nOK.Make sure you have the Metadata file \"CDEsMetadata.json\" under the correct data path in your Master node"
        break
    else
        echo "$answer is not a valid answer! Try again.. [y/n]"
        read answer
    fi
done

echo -e "\nDo you wish to copy Metadata file for Worker nodes now? [y/n]"
read answer
while true
do
    if [[ "${answer}" == "y" ]]; then
        while IFS= read -r line; do
            if [[ "$line" = *"hostname="* ]]; then
                worker=$(echo "$line" | cut -d'=' -f 1)
                worker_name=$( echo "$line" | cut -d'=' -f 2)

                echo -e "\nCopying Metadata file to ${worker_name} node"
                ansible-playbook -i hosts.ini Metadata-Master.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml -e "my_host=${worker}"
                ansible_playbook=$?
                #If status code != 0 an error has occurred
                if [[ ${ansible_playbook} -ne 0 ]]; then
                    echo "Playbook \"Metadata-Master.yaml\" exited with error." >&2
                    exit 1
                fi
            fi
        done < hosts.ini
        break
    elif [[ "${answer}" == "n" ]]; then
        echo "OK. Make sure you have the Metadata file \"CDEsMetadata.json\" under the correct data path in Worker nodes"
        break
    else
        echo "$answer is not a valid answer! Try again.. [y/n]"
        read answer
    fi
done

echo -e "\nInitializing Swarm..Initializing mip-federation network..Copying Compose-Files folder to Manager of Swarm..."
sleep 1
ansible-playbook -i hosts.ini Init-Swarm.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml
ansible_playbook=$?
#If status code != 0 an error has occurred
if [[ ${ansible_playbook} -ne 0 ]]; then
    echo "Playbook \"Init-Swarm.yaml\" exited with error." >&2
    exit 1
fi

echo -e "\nJoining worker nodes in Swarm.."
while IFS= read -r line; do
    if [[ "$line" = *"hostname="* ]]; then
        worker=$(echo "$line" | cut -d'=' -f 1)
        worker_name=$( echo "$line" | cut -d'=' -f 2)
        ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml -e "my_host=${worker}"
        ansible_playbook=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook} -ne 0 ]]; then
            echo "Playbook \"Join-Workers.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\n ${worker_name} is now part of the Swarm.."
        sleep 1
    fi
done < hosts.ini


echo -e "\nStarting Exareme services...Do you wish to run Portainer service as well [y/n]?"
read answer
while true
do
    if [[ "${answer}" == "y" ]]; then
        ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml
        ansible_playbook=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook} -ne 0 ]]; then
            echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services, Portainer service are now running"
        break
    elif [[ "${answer}" == "n" ]]; then
        ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko --vault-password-file ~/.vault_pass.txt -e@vault_file.yaml --skip-tags portainer
        ansible_playbook=$?
        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook} -ne 0 ]]; then
            echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services are now running"
        break
    else
        echo "$answer is not a valid answer! Try again.. [y/n]"
        read answer
    fi
done