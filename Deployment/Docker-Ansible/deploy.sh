#!/usr/bin/env bash

: 'echo "Do you wish to copy Metadata file to Master node now? [y/n]. If [y] make sure you have the file \"CDEsMetadata.json\" inside Metadata folder"
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
'
password () {
    read answer
    while true
    do
        if [[ "${answer}" == "y" ]]; then
            echo "Type your Ansible password:"
            read -s password
            echo $password >> ~/.vault_pass.txt
            ansible_playbook+="--vault-password-file ~/.vault_pass.txt "
            break
        elif [[ "${answer}" == "n" ]]; then
            echo "You need to enter your Ansible password every single time ansible-playbooks ask for one."
            sleep 1
            ansible_playbook+="--ask-vault-pass "
            break
        else
            echo "$answer is not a valid answer! Try again.. [y/n]"
            read answer
        fi
    done
}


#Default ansible_playbook
ansible_playbook="ansible-playbook -i hosts.ini -c paramiko -e@vault_file.yaml "

echo "Ansible-vault gives you the simplicity of storing your Ansible password in a file. \
Looking for file \"~/.vault_pass.txt\"... (It may be required to enter your sudo password..)\""

if [[ -z $(sudo find ~/.vault_pass.txt) ]]; then
    echo -e "\nNo such file \"~/.vault_pass.txt\". Do you want to create one now? [y/n]"
    flag=1
    password
else
    if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
        echo -e "\nFile exists and it is not empty! Moving on..."
        ansible_playbook+="--vault-password-file ~/.vault_pass.txt "
    else
        echo -e "\nFile is empty.. Do you want to store your Ansible password now?[y/n]"
        password
    fi
fi

echo -e "\nInitializing Swarm..Initializing mip-federation network..Copying Compose-Files folder to Manager of Swarm..."
sleep 1
ansible_playbook_init=${ansible_playbook}"Init-Swarm.yaml"
${ansible_playbook_init}

ansible_playbook_code=$?
#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Init-Swarm.yaml\" exited with error." >&2
    exit 1
fi

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
done < hosts.ini
if [[ ${flag} != "0" ]]; then
    echo -e "\nIt seems that no workers will join the Swarm. If you have workers \
make sure you included their names below label [workers], so Ansible will not Ignore them."
    echo -e "\nContinue? [y/n]"

    read answer
    while true
    do
        if [[ "${answer}" == "y" ]]; then
            echo "Continue without Workers.."
            break
        elif [[ "${answer}" == "n" ]]; then
            echo "Exiting...(Leaving Swarm for Master node).."
            ansible_playbook_leave=${ansible_playbook}"Leave-Master.yaml"

            echo ${ansible_playbook_leave}
            #${ansible_playbook_leave}
            ansible_playbook_code=$?
            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Leave-Master.yaml\" exited with error." >&2
                exit 1
            fi
            exit 1
        else
            echo "$answer is not a valid answer! Try again.. [y/n]"
            read answer
        fi
    done
fi

echo -e "\nStarting Exareme services...Do you wish to run Portainer service as well [y/n]?"
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
        echo -e "\nExareme services, Portainer service are now running"
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
        echo "$answer is not a valid answer! Try again.. [y/n]"
        read answer
    fi
done