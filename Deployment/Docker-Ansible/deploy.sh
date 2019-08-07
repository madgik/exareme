#!/usr/bin/env bash
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

init_ansible_playbook () {
#Default ansible_playbook
ansible_playbook="ansible-playbook -i hosts.ini -c paramiko -e@vault_file.yaml "

echo -e "\nAnsible-vault gives you the simplicity of storing your Ansible password in a file. \
Looking for file \"~/.vault_pass.txt\"... (It may be required to enter your sudo password..)\""

# --vault-password-file or --ask-vault-pass
if [[ -z $(sudo find ~/.vault_pass.txt) ]]; then
    echo -e "\nNo such file \"~/.vault_pass.txt\". Do you want to create one now? [y/n]"
    flag=1
    password
else
    if [[ -s $(sudo find ~/.vault_pass.txt) ]]; then
        echo -e "\nFile exists and it is not empty! Moving on..."
        ansible_playbook+="--vault-password-file ~/.vault_pass.txt "
    else
        echo -e "\nFile is empty.. Do you want to store your Ansible password in a text file?[y/n]"
        password
    fi
fi

}

echo -e "Choose one of the below:\n"
echo "1:Deploy everything"
echo "2:Add a specific worker in an already initialized swarm"
echo -e "3:Restart services\n"

read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        echo -e "\nYou choose to deploy everything.."
        chmod 755 deploy_all.sh
        . ./deploy_all.sh
        exit 0
    elif [[ "${answer}" == "2" ]]; then
        echo -e "\nYou choose to add a specific worker in an already initialized swarm.."
        chmod 755 add_worker.sh
        . ./add_worker.sh
        exit 0
    elif [[ "${answer}" == "3" ]]; then
         echo -e "\nYou choose to restart Services.."
         chmod 755 restart.sh
         . ./restart.sh
         exit 0
    else
        echo "$answer is not a valid answer! Try again.. [1/2/3"
        read answer
    fi
done
