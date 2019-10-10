#!/usr/bin/env bash

#TODO (not critical) create function for encryption Process


# Check if vault_pass file exists. 
# If it doesn't ask the user to provide it.
# If the user doesn't want to, add the --ask-vault-pass parameter to the ansible calls.
get_vault_authentication () {
	if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
		echo -e "\nAn ansible password exists in the vault_pass file. Moving on..."
		ansible_vault_authentication="--vault-password-file ~/.vault_pass.txt "
	else
		echo -e "\nIn order for the installation scripts to run an ansible vault file needs to be created. /
		Do you want to store your Ansible Vault Password in a text file, so that it's not required every time?[ y/n ]"
		read answer
		while true
		do
			if [[ "${answer}" == "y" ]]; then
				echo "Type your Ansible password:"
				read -s password
				echo $password > ~/.vault_pass.txt
				ansible_playbook+="--vault-password-file ~/.vault_pass.txt "

				#For encrypting/ decrypting vault.yaml file
				ansible_vault_authentication="--vault-password-file ~/.vault_pass.txt "
				break
			elif [[ "${answer}" == "n" ]]; then
				echo "You need to enter your Ansible password every single time ansible-playbooks asks for one."
				sleep 1
				ansible_playbook+="--ask-vault-pass "

				#For encrypting/ decrypting vault.yaml file
				ansible_vault_authentication="--ask-vault-pass "
				break
			else
				echo "$answer is not a valid answer! Try again.. [ y/n ]"
				read answer
			fi
		done
	fi
}


# Get master node information
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


# Create vault.yaml file
createFile () {

    masterVaultInfos

    echo ${master_remote_user} >> ../vault.yaml
    echo ${master_become_user} >> ../vault.yaml
    echo ${master_ssh_pass} >> ../vault.yaml
    echo ${master_become_pass} >> ../vault.yaml

    ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
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
                ansible_vault_decrypt="ansible-vault decrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
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

                ansible_vault_encrypt="ansible-vault encrypt ../vault.yaml "${ansible_vault_authentication}    #--vault-password-file or --ask-vault-pass depending if  ~/.vault_pass.txt exists
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


# If include-only flag is given don't execute the script
if [ "$1" == "include-only" ]; then
  return
fi


# Remove file if it already exists
if [[ -s ../vault.yaml ]]; then                          #if file not empty
    rm -f ../vault.yaml
fi

echo -e "\nPlease provide the private information of the target machines (vault.yaml). The information will be encrypted."
createFile