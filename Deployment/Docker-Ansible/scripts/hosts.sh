#!/usr/bin/env bash

# Information for username/password for hosts.ini & vault.yaml files
usernamePassword () {
	echo -e "\n"${1}" remote_user=\"{{"${1}"_remote_user}}\"" >> ../hosts.ini
	echo ${1}" become_user=\"{{"${1}"_become_user}}\"" >> ../hosts.ini
	echo ${1}" ansible_become_pass=\"{{"${1}"_become_pass}}\"" >> ../hosts.ini
	echo -e ${1}" ansible_ssh_pass=\"{{"${1}"_ssh_pass}}\"\n" >> ../hosts.ini
}

# Get Worker Node Info
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

# Get Master Node Info
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

# Get Worker Vault Info
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

    ssh_pass=${1}"_ssh_pass: "${remote_pass}
    become_pass=${1}"_become_pass: "${become_pass}
}

# (Re)Initialize hosts.ini file
createFile () {

    # Information for target Master
    echo -e "\nInformation for target machines' are needed (hosts.ini)."
    echo "[master]" >> ../hosts.ini
    masterHostsInfo

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

            echo "[workers]" >> ../hosts.ini
            worker=1

            #Construct worker1, worker2 .. workerN below [workers] tag
            while [[ ${answer1} != 0 ]]
            do
                echo "worker"${worker} >> ../hosts.ini
                worker=$[${worker}+1]
                answer1=$[${answer1}-1]
            done

            #For each worker1, worker2, .. workerN place infos in hosts.ini
            worker=$[${worker}-1]
            n=1
            while [[ ${worker} != 0 ]]
            do
                workerHostsInfo "worker"${n}
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

}


# If include-only flag is given don't execute the script
if [ "$1" == "include-only" ]; then
  return
fi


# Remove file if it already exists
if [[ -s ../hosts.ini ]]; then
    rm -f ../hosts.ini
fi

createFile
