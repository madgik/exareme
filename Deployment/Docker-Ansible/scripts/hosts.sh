#!/usr/bin/env bash

usernamePassword () {
echo -e "\n"${1}" remote_user=\"{{"${1}"_remote_user}}\"" >> hosts.ini
echo ${1}" become_user=\"{{"${1}"_become_user}}\"" >> hosts.ini
echo ${1}" ansible_become_pass=\"{{"${1}"_become_pass}}\"" >> hosts.ini
echo ${1}" ansible_ssh_pass=\"{{"${1}"_ssh_pass}}\"" >> hosts.ini

}

createFile () {
    echo "Infos for target master and worker nodes are needed. What is the ansible host for target \"master\"? (expecting IP)"
    read answer
    echo "[master]" >> hosts.ini
    echo "master ansible_host="${answer} >> hosts.ini  #check if what given is an IP

    echo "What is the home path for target \"master\"?"
    read answer
    echo "master home_path="${answer} >> hosts.ini

    echo "What is the data path for targer \"master\"?"
    read answer
    echo "master data_path="${answer} >> hosts.ini


    usernamePassword "master"

    echo "Are there any target \"worker\" nodes? [ y/n ]"
    read answer
    while true
    do
        if [[ ${answer} == "y" ]]; then
            echo "How many target \"worker\" nodes are there?"
            read answer1
            #check if what the user gave is a number
            echo -e "\n[workers]" >> hosts.ini
            worker=1
            while [[ ${answer1} != 0 ]]
            do
                echo "worker"${worker} >> hosts.ini
                worker=$[${worker}+1]
                answer1=$[${answer1}-1]
            done
            worker=$[${worker}-1]
            n=1
            while [[ ${worker} != 0 ]]
            do
                echo "What is the ansible host for target \"worker\"? (expecting IP)"
                read answer
                echo -e "\n[worker"${n}"]" >> hosts.ini
                echo "worker"${n} "ansible_host="${answer} >> hosts.ini  #check if what given is an IP

                echo "What is the hostname for target \"worker\"${n}?"
                read answer
                echo "worker"${n} "hostname="${answer} >> hosts.ini

                echo "What is the data_path for target \"worker\"?"
                read answer
                echo "worker"${n} "data_path="${answer} >> hosts.ini

                usernamePassword "worker"${n}
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


if [[ -f hosts.ini ]]; then
    echo "hosts.ini file already exists. Do you wish to create it again? [ y/n]"
    read answer
    while true
    do
        if [[ ${answer} == "y" ]]; then
            rm -f hosts.ini
            createFile
            break
        elif [[ ${answer} == "n" ]]; then
            echo -e "Existing hosts.ini wiil be used.Continuing..\n"
            sleep 1
            break
        else
            echo ${answer}" is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
else
    createFile
fi



