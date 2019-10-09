#!/usr/bin/env bash
#TODO check what is happening when you give the wrong pass more than 3 times

export ANSIBLE_HOST_KEY_CHECKING=False      #avoid host key checking

#Default ansible_playbook
init_ansible_playbook () {
ansible_playbook="ansible-playbook -i ../hosts.ini -c paramiko -e@vault_file.yaml "

echo -e "\nAnsible-vault gives you the simplicity of storing your Ansible password in a file. \
Place the user's sudo password in this machine for looking file \"~/.vault_pass.txt\"...\""

# --vault-password-file or --ask-vault-pass
if [[ -z $(sudo find ~/.vault_pass.txt) ]]; then
    echo -e "\nNo such file \"~/.vault_pass.txt\". Do you want to create one now? [ y/n ]"
    flag=1
    password
else
    if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
        echo -e "\nFile for Ansible password exists and it is not empty! Moving on..."
        ansible_playbook+="--vault-password-file ~/.vault_pass.txt "
    else
        echo -e "\nFile is empty.. Do you want to store your Ansible password in a text file?[ y/n ]"
        password
    fi
fi

}

#choose --vault-password-file (if ~/.vault_pass.txt exists) or --ask-vault-pass (if ~/.vault_pass.txt not exists)
password () {
    read answer
    while true
    do
        if [[ "${answer}" == "y" ]]; then
            echo "Type your Ansible password:"
            read -s password
            echo $password > ~/.vault_pass.txt
            ansible_playbook+="--vault-password-file ~/.vault_pass.txt "

            #For encrypting/ decrypting vault.yaml file
            ansible_vault="--vault-password-file ~/.vault_pass.txt "
            break
        elif [[ "${answer}" == "n" ]]; then
            echo "You need to enter your Ansible password every single time ansible-playbooks ask for one."
            sleep 1
            ansible_playbook+="--ask-vault-pass "

            #For encrypting/ decrypting vault.yaml file
            ansible_vault="--ask-vault-pass "
            break
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
}

#Stop Exareme Services
stop () {
    if [[ ${1} == "1" ]]; then
        echo -e "\nStopping Exareme services..."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags portainer"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    elif [[ ${1} == "2" ]]; then
        echo -e "\nStopping Portainer services..."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags exareme"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    elif [[ ${1} == "3" ]]; then
        echo -e "\nStopping all services..."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?

    else
        echo "$answer is not a valid answer! Try again.. [ 1-2-3 ]"
        read answer
    fi
}

#Infos for username/password for hosts.ini & vault.yaml files
usernamePassword () {
echo -e "\n"${1}" remote_user=\"{{"${1}"_remote_user}}\"" >> ../hosts.ini
echo ${1}" become_user=\"{{"${1}"_become_user}}\"" >> ../hosts.ini
echo ${1}" ansible_become_pass=\"{{"${1}"_become_pass}}\"" >> ../hosts.ini
echo -e ${1}" ansible_ssh_pass=\"{{"${1}"_ssh_pass}}\"\n" >> ../hosts.ini
}

#Infos for target node "worker"
workerHostsInfo () {
echo -e "\nWhat is the ansible host for target \"${1}\"? (expecting IP)"
read answer

while true
do
    if [[ ${answer} =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
        for i in 1 2 3 4; do
            if [ $(echo "$answer" | cut -d. -f$i) -gt 255 ]; then
                echo "$answer" | cut -d. -f$i
                echo -e "\n${answer} is not a valid IP. Try again.."
                read answer
            fi
        done
        break
    else
        echo -e "\n${answer} is not a valid IP. Try again.."
        read answer
    fi
done

echo -e "\n[${1}]" >> ../hosts.ini
echo ${1} "ansible_host="${answer} >> ../hosts.ini

echo -e "\nWhat is the hostname for target \"${1}\"?"
read answer
echo ${1} "hostname="${answer} >> ../hosts.ini

echo -e "\nWhat is the data_path for target \"${1}\"?"
read answer
#Check that path ends with /
if [[ "${answer: -1}"  != "/" ]]; then
        answer=${answer}"/"
fi
echo ${1} "data_path="${answer} >> ../hosts.ini

usernamePassword ${1}
}

#Infos for target node "master"
masterHostsInfo () {
    echo -e "\nWhat is the ansible host for target \"master\"? (expecting IP)"
    read answer
    while true
    do
        if [[ ${answer} =~ ^[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}$ ]]; then
            for i in 1 2 3 4; do
                if [ $(echo "$answer" | cut -d. -f$i) -gt 255 ]; then
                    echo "$answer" | cut -d. -f$i
                    echo -e "\n${answer} is not a valid IP. Try again.."
                    read answer
                fi
            done
            break
        else
            echo -e "\n${answer} is not a valid IP. Try again.."
            read answer
        fi
    done

    echo "master ansible_host="${answer} >> ../hosts.ini
    echo -e "\nWhat is the home path for target \"master\"?"
    read answer
    #Check that path ends with /
    if [[ "${answer: -1}"  != "/" ]]; then
        answer=${answer}"/"
    fi
    echo "master home_path="${answer} >> ../hosts.ini

    echo -e "\nWhat is the data path for targer \"master\"?"
    read answer
    #Check that path ends with /
    if [[ "${answer: -1}"  != "/" ]]; then
        answer=${answer}"/"
    fi
    echo "master data_path="${answer} >> ../hosts.ini
    usernamePassword "master"
}

#worker infos
workerVaultInfos () {

    echo -e "\nWhat is the remote user for target \"${1}\"?"
    read remote_user
    var_remote_user=${1}"_remote_user: "${remote_user}

    echo -e "\nWhat is the password for remote user:\"${remote_user}\" for target \"${1}\"?"
    read -s remote_pass

    echo -e "\nWhat is the become user for target \"${1}\"? (root if possible)"
    read become_user
    var_become_user=${1}"_become_user: "${become_user}

    echo -e "\nWhat is the password for become user:\"${become_user}\" for target \"${1}\"?"
    read -s become_pass

    ssh_user=${1}"_ssh_user: "${remote_pass}
    become_pass=${1}"_become_pass: "${become_pass}
}


chmod 755 *.sh

#Main menu
while true
do
    echo -e "Choose one of the below:\n"
    echo "1:Change the exareme docker image version."
    echo "2:Change the host machines' information."
    echo "3:Change the host machines' private information."
    echo "4:Deploy everything."
    echo "5:Add a specific worker in an already initialized swarm."
    echo "6:(Re)Start services."
    echo "7:Stop services."
    echo -e "8:Exit.\n"

    read answer1

    while true
    do
        if [[ "${answer1}" == "1" ]]; then
            echo -e "\nYou chose to change exareme docker image version..."
            . ./exareme.sh
            break
        elif [[ "${answer1}" == "2" ]]; then
            echo -e "\nYou chose to create hosts.ini file..."
            . ./hosts.sh
            . ./vault.sh
            break
        elif [[ "${answer1}" == "3" ]]; then
            echo -e "\nYou chose to create vault.yaml file..."
            . ./vault.sh
            break
        elif [[ "${answer1}" == "4" ]]; then
            . ./exareme.sh
            . ./hosts.sh
            . ./vault.sh
            echo -e "\nYou chose to deploy everything..."
            . ./deploy_all.sh
            break
        elif [[ "${answer1}" == "5" ]]; then
            . ./exareme.sh
            . ./hosts.sh
            . ./vault.sh
            echo -e "\nYou chose to add a specific worker in an already initialized swarm.."
            . ./add_worker.sh
            break
        elif [[ "${answer1}" == "6" ]]; then
            . ./exareme.sh
            . ./hosts.sh
            . ./vault.sh
            echo -e "\nYou chose to restart Services.."
            . ./restart.sh
            break
        elif [[ "${answer1}" == "7" ]]; then
            . ./hosts.sh
            . ./vault.sh
            echo -e "\nYou chose to stop Services.."
            . ./stop.sh
            break
        elif [[ "${answer1}" == "8" ]]; then
            echo -e "\nYou chose to Exit.."
            exit 0
        else
            echo "$answer1 is not a valid answer! Try again.. [ 1-2-3-4-5-6-7-8 ]"
            read answer1
        fi
    done
done