#!/usr/bin/env bash

#Create hosts.ini file
createFile () {

    #Infos for target Master
    echo -e "\nInfos for target master and worker nodes are needed."
    echo "[master]" >> ../hosts.ini
    masterHostsInfo

    echo -e "\nAre there any target \"worker\" nodes? [ y/n ]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            echo -e "\nHow many target \"worker\" nodes are there?"
            read answer1
            #TODO check if what the user gave is a number
            echo "[workers]" >> hosts.ini
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

if [[ -s ../hosts.ini ]]; then                          #if file not empty
    echo -e "\nhosts.ini file already exists. Do you wish to create it again? [ y/n]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            rm -f ../hosts.ini
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
else                                            #If file empty, create it
    echo -e "\nhosts.ini file does not exist. Creating it now.."
    createFile
fi



