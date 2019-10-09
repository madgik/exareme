#!/usr/bin/env bash

#TODO (not critical) create function for encryption Process

workerVaultInfosFlag=1

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

    echo -e "\nWhat is the remote user for target \"master\"?"
    read remote_user
    master_remote_user="master_remote_user: "${remote_user}


    echo -e "\nWhat is the password for remote user:\"${remote_user}\" for target \"master\"?"
    read -s remote_pass

    echo -e "\nWhat is the become user for target \"master\"? (root if possible)"
    read become_user
    master_become_user="master_become_user: "${become_user}

    echo -e "\nWhat is the password for become user:\"${become_user}\" for target \"master\"?"
    read -s become_pass

    master_ssh_pass="master_ssh_pass: "${remote_pass}
    master_become_pass="master_become_pass: "${become_pass}

}


#Create vault.yaml file
createFile () {

    masterVaultInfos

    echo ${master_remote_user} >> ../vault.yaml
    echo ${master_become_user} >> ../vault.yaml
    echo ${master_ssh_pass} >> ../vault.yaml
    echo ${master_become_pass} >> ../vault.yaml

    ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
    ${ansible_vault_encrypt}

    ansible_playbook_code=$?
    #If status code != 0 an error has occurred
    if [[ ${ansible_playbook_code} -ne 0 ]]; then
        echo "Encryption of file \"../vault.yaml\" exited with error. Removing file with sensitive information. Exiting.." >&2
        rm -rf ../vault.yaml
        exit 1
    fi

    echo -e "\nAre there any target \"worker\" nodes? [ y/n ]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            echo -e "\nHow many target \"worker\" nodes are there?"
            read answer1
            #Check if what was given is a number
            while true
            do
                if ! [[ "$answer1" =~ ^[0-9]+$ ]]; then
                    echo "${answer1} is not a valid number! Try again.."
                    read answer1
                else
                    break
                fi
            done

            #For each worker1, worker2, .. workerN place infos in hosts.ini
            worker=${answer1}
            n=1
            while [[ ${worker} != 0 ]]
            do
                workerVaultInfos "worker"${n}

                #TODO (not critical) decrypt encrypt only once with dynamic variables
                ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
                ${ansible_vault_decrypt}

                ansible_playbook_code=$?
                #If status code != 0 an error has occurred
                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                    echo "Decryption of file \"../vault.yaml\" exited with error.Exiting.." >&2
                    exit 1
                fi

                echo -e "\n" >> ../vault.yaml
                echo ${var_remote_user} >> ../vault.yaml
                echo ${var_become_user} >> ../vault.yaml
                echo ${ssh_pass} >> ../vault.yaml
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

    echo -e "\nFile for holding private information for target machines's created (vault.yaml).\n"
}

if [[ -s ../vault.yaml ]]; then                          #if file not empty
    rm -f ../vault.yaml
else
    :
fi

echo -e "\nPrivate information for target machines' are needed (vault.yaml)."
createFile