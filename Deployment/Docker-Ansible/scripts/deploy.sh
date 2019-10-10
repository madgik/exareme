#!/usr/bin/env bash

. ./vault.sh --source-only
. ./hosts.sh --source-only
. ./stop.sh --source-only

# TODO check what is happening when you give the wrong pass more than 3 times

export ANSIBLE_HOST_KEY_CHECKING=False      #avoid host key checking

# Initialize ansible_playbook variable with the basic command
# Ask the user if he wants to save the ansible vault password.
init_ansible_playbook () {

	ansible_playbook="ansible-playbook -i ../hosts.ini -c paramiko -e@../vault.yaml "

	provide_vault_password

}

init_ansible_playbook

chmod 755 *.sh

# Main menu
while true
do
    echo -e "Choose one of the below:\n"
    echo "1: Deploy the exareme swarm and start services."
    echo "2: (Re)Start services."
	echo "3: (Re)Start service."
    echo "4: Stop services."
    echo "5: Stop service."
	echo "6: Create or modify the exareme docker image version (exareme.yaml)."
    echo "7: (Re)Initialize the exareme swarm target machines' information (hosts.ini, vault.yaml)."
    echo "8: Add a new worker to the exareme swarm information (hosts.ini, vault.yaml)."
    echo -e "9:Exit.\n"

    read answer1

    while true
    do
		# 1: Deploy the exareme swarm and start services.
		if [[ "${answer1}" == "1" ]]; then
			echo -e "\nYou chose to deploy everything..."

			if [[ -f ../group_vars/exareme.yaml ]]; then
                echo -e "\nFile for holding docker exareme image information does not exist. Please create it first (Option 6)."
				break
			fi
			
            if [[ ! -s ../hosts.ini ]]; then
                echo -e "\nFile for holding target machines' information (hosts.ini) does not exist. Please create it first (Option 7)."
				break
            fi
			
            if [[ ! -s ../vault.yaml ]]; then
                echo -e "\nFile for holding target machines' information (vault.yaml) does not exist. Please create it first (Option 7)."
				break
            fi

			echo -e "\nAll neccessary files exist (hosts.ini, vault.yaml, exareme.yaml). Deploying..."
            . ./deploy_all.sh
            break
		
		# 2: (Re)Start services.
		elif [[ "${answer1}" == "2" ]]; then
            echo -e "\nYou chose to (re)start all services.."

			if [[ -f ../group_vars/exareme.yaml ]]; then
                echo -e "\nFile for holding docker exareme image information does not exist. Please create it first (Option 6)."
				break
			fi
			
            if [[ ! -s ../hosts.ini ]]; then
                echo -e "\nFile for holding target machines' information (hosts.ini) does not exist. Please create it first (Option 7)."
				break
            fi
			
            if [[ ! -s ../vault.yaml ]]; then
                echo -e "\nFile for holding target machines' information (vault.yaml) does not exist. Please create it first (Option 7)."
				break
            fi

			echo -e "\nAll neccessary files exist (hosts.ini, vault.yaml, exareme.yaml). Restarting..."
            . ./restart.sh
            break
		
		# 4: Stop services.
        elif [[ "${answer1}" == "4" ]]; then
            echo -e "\nYou chose to stop all services.."

			if [[ -f ../group_vars/exareme.yaml ]]; then
                echo -e "\nFile for holding docker exareme image information does not exist. Please create it first (Option 6)."
				break
			fi
			
            if [[ ! -s ../hosts.ini ]]; then
                echo -e "\nFile for holding target machines' information (hosts.ini) does not exist. Please create it first (Option 7)."
				break
            fi
			
            if [[ ! -s ../vault.yaml ]]; then
                echo -e "\nFile for holding target machines' information (vault.yaml) does not exist. Please create it first (Option 7)."
				break
            fi

			echo -e "\nAll neccessary files exist (hosts.ini, vault.yaml, exareme.yaml). Stoping..."
            . ./stop.sh
            break
			
		# 6: Create or modify the exareme docker image version (exareme.yaml).
        elif [[ "${answer1}" == "6" ]]; then
            echo -e "\nYou chose to change exareme docker image version..."
            . ./exareme.sh
            break
		
		# 7: (Re)Initialize the exareme swarm target machines' information (hosts.ini, vault.yaml).
        elif [[ "${answer1}" == "7" ]]; then
            echo -e "\nYou chose to (re)initialize the target machines' information (hosts.ini, vault.yaml)..."
            . ./hosts.sh
            . ./vault.sh
            break
		
		# 8: Add a new worker to the exareme swarm information (hosts.ini, vault.yaml)."
        elif [[ "${answer1}" == "8" ]]; then
            echo -e "\nYou chose to add a new worker to the exareme swarm information."

			if [[ -f ../group_vars/exareme.yaml ]]; then
                echo -e "\nFile for holding docker exareme image information does not exist. Please create it first (Option 6)."
				break
			fi
			
            if [[ ! -s ../hosts.ini ]]; then
                echo -e "\nFile for holding target machines' information (hosts.ini) does not exist. Please create it first (Option 7)."
				break
            fi
			
            if [[ ! -s ../vault.yaml ]]; then
                echo -e "\nFile for holding target machines' information (vault.yaml) does not exist. Please create it first (Option 7)."
				break
            fi
			
            . ./add_worker.sh
            break
		
		# 9:Exit.
        elif [[ "${answer1}" == "9" ]]; then
            echo -e "\nYou chose to Exit.."
            exit 0
        else
            echo "$answer1 is not a valid answer! Try again.. [ 1-2-3-4-5-6-7-8-9 ]"
            read answer1
        fi
    done
done