#!/usr/bin/env bash

#TODO update vault.yaml for workers (decrypt and encrypt)

# --vault-password-file or --ask-vault-pass
if [[ -z $(sudo find ~/.vault_pass.txt) ]]; then
    echo -e "\nNo such file \"~/.vault_pass.txt\". Do you want to create one now? [ y/n ]"
    flag=1
    password
else
    if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
        echo -e "\nFile for Ansible password exists and it is not empty! Moving on..."
        ansible_vault="--vault-password-file ~/.vault_pass.txt "
    else
        echo -e "\nFile is empty.. Do you want to store your Ansible password in a text file?[ y/n ]"
        password
    fi
fi



masterInfos () {

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
    echo "master_become_pass:" ${become_pass} >> ../vault.yaml
}


workerInfos () {

    echo -e "\nWhat is the remote user for target \"worker"${1}"\""
    read remote_user
    echo ${1}"_remote_user:" ${remote_user} >> ../vault.yaml


    echo -e "\nWhat is the password for remote user: "${remote_user}" for target \"worker"${1}"\""
    read remote_pass

    echo -e "\nWhat is the become user for target \"worker"${1}"\" (root if possible)"
    read become_user
    echo ${1}"_become_user:" ${become_user} >> ../vault.yaml

    echo -e "\nWhat is the password for become user: "${become_user}" for target \"worker"${1}"\""
    read become_pass

    echo ${1}"_ssh_user:" ${remote_pass} >> ../vault.yaml
    echo ${1}"_become_pass:" ${become_pass} >> ../vault.yaml
}

#Create vault.yaml file
createFile () {

    masterInfos
    ansible_vault="ansible-vault encrypt ../vault.yaml "${ansible_vault}
    ${ansible_vault}

    #If status code != 0 an error has occurred
    if [[ ${ansible_vault} -ne 0 ]]; then
        echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information." >&2
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
    createFile
fi
