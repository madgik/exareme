#!/usr/bin/env bash

# --vault-password-file or --ask-vault-pass
if [[ -z $(sudo find ~/.vault_pass.txt) ]]; then
    echo -e "\nNo such file \"~/.vault_pass.txt\" for storing Ansible password. Do you want to create one now? [ y/n ]"
    flag=1
    password
else
    if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
        echo -e "\nFile for storing Ansible password exists and it is not empty! Moving on..."
        ansible_vault="--vault-password-file ~/.vault_pass.txt "
    else
        echo -e "\nFile is empty.. Do you want to store your Ansible password in a text file?[ y/n ]"
        password
    fi
fi

#master infos
masterVaultInfos () {

    echo -e "\nWhat is the remote user for target \"master\""
    read remote_user
    echo "master_remote_user:" ${remote_user} >> ../vault.yaml


    echo -e "\nWhat is the password for remote user: "${remote_user}" for target \"master\""
    read -s remote_pass

    echo -e "\nWhat is the become user for target \"master\" (root if possible)"
    read become_user
    echo "master_become_user:" ${become_user} >> ../vault.yaml

    echo -e "\nWhat is the password for become user: "${become_user}" for target \"master\""
    read -s become_pass

    echo "master_ssh_user:" ${remote_pass} >> ../vault.yaml
    echo -e "master_become_pass:" ${become_pass}"\n" >> ../vault.yaml
}

#Create vault.yaml file
createFile () {

    masterVaultInfos
    echo -e "\nAre there any target \"worker\" nodes? [ y/n ]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            echo -e "\nHow many target \"worker\" nodes are there?"
            read answer1
            #TODO check if what the user gave is a number

            #For each worker1, worker2, .. workerN place infos in hosts.ini
            worker=${answer1}
            n=1
            while [[ ${worker} != 0 ]]
            do
                workerVaultInfos "worker"${n}
                n=$[${n}+1]
                worker=$[${worker}-1]
            done
            break

        elif [[ ${answer} == "n" ]]; then
            break
        else
            echo "${answer} is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done


    ansible_vault="ansible-vault encrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    echo ${ansible_vault}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting.." >&2
        rm -rf ../vault.yaml
        exit 1
    fi

    echo -e "\nvault.yaml file created."
}


if [[ -s ../vault.yaml ]]; then                          #if file not empty
    echo -e "\nvault.yaml file already exists. Do you wish to create it again? [ y/n]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            rm -f ../vault.yaml
            createFile
            break
        elif [[ ${answer} == "n" ]]; then
            echo -e "Existing vault.yaml wiil be used.Continuing..\n"
            sleep 1
            break
        else
            echo ${answer}" is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
else                                            #If file empty, create it
    echo -e "\nvault.yaml does not exist. Creating it now.."
    createFile
fi
