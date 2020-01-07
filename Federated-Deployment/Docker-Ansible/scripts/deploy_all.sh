#!/usr/bin/env bash

notFound=0

portainer () {
    echo -e "\nDo you wish to run Portainer in a secure way? (SSL certificate required in Target machine) [ y/n ]"
    read answer

    while true
    do
        #Run Secure Portainer
        if [[ ${answer} == "y" ]]; then
            while IFS= read -r line || [[ -n "$line" ]]; do
                 #If DOMAIN_NAME exists as key in ../group_vars/all.yaml
                if [[ "$line" == *DOMAIN_NAME:* ]]; then
                    #Get the value
                    domain_name=$(echo "$line" | cut -d ':' -d ' ' -d '"' -f 2 -d '"')

                    #If domain_name not null
                    if [[ ${domain_name} != "" ]]; then
                        #Check if Domain name exists in the Target machine
                        ansible_playbook_check=${ansible_playbook}"../CheckDomain.yaml -e domain_name="${domain_name}
                        #Playbook will write in file $(pwd)/domain.txt True if domain name exists or False if not
                        ${ansible_playbook_check}

                        ansible_playbook_code=$?

                        #If status code != 0 an error has occurred
                        if [[ ${ansible_playbook_code} -ne 0 ]]; then
                            echo "Playbook \"../CheckDomain.yaml\" exited with error." >&2
                            exit 1
                        fi

                        #Get True or False from $(pwd)/domain.txt
                        flag=$(cat domain.txt)
                        rm -f domain.txt

                        #Domain name exists in Target machine
                        if [[ ${flag} == "True" ]]; then

                            #If portainer function called from restart.sh, restart Secure Portainer only
                            if [[ ${1} == "restart" ]]; then
                                #Run Secure Portainer Only
                                tags="portainerSecure"
                                echoErrorMessage="Playbook \"../Start-Exareme.yaml --tags portainerSecure\" exited with error."
                                echoMessage="\nSecure Portainer service just restarted.."
                            else
                                #portainer function called from start-services/tasks/main.yaml, start Secure Portainer + exareme
                                tags="portainerSecure,exareme"
                                echoErrorMessage="Playbook \"../Start-Exareme.yaml --tags portainerSecure,exareme\" exited with error."
                                echoMessage="\nExareme services and Secure Portainer service are now running"
                            fi

                            ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --tags "${tags}
                            ${ansible_playbook_start}

                            ansible_playbook_code=$?

                            #If status code != 0 an error has occurred
                            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                echo ${echoErrorMessage} >&2
                                exit 1
                            fi
                            echo -e ${echoMessage}

                        #Domain name does not exist in Target machine
                        elif [[ ${flag} == "False" ]]; then
                            if [[ ${1} == "restart" ]]; then
                                 #If portainer function called from restart.sh
                                echo -e "\nNo certificate for that Domain name: "${domain_name}". Portainer service did not restart.."
                                sleep 1
                            else
                                #portainer function called from start-services/tasks/main.yaml, start only Exareme because certificate not correct
                                echo -e "\nNo certificate for that Domain name: "${domain_name}". Starting without Portainer.."
                                sleep 1
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer,portainerSecure"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"Start-Exareme.yaml --skip-tags portainer,portainerSecure\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nExareme services are now running"
                            fi
                        fi
                    #DOMAIN_NAME exists in ../group_vars/all.yaml as key but has no value
                    else
                       notFound=1
                       break
                    fi
                #DOMAIN_NAME does not exist in ../group_vars/all.yaml as key
                else
                    notFound=1      #TODO the way it is now it only sees the first line.. fix it
                    break
                fi
                break
            done < ../group_vars/all.yaml

            if [[ ${notFound} == "1" ]]; then
                echo -e "\nWhat is the Domain name for which an SSL certificate created in the Target machine?" #TODO master IP
                read domain_name

                #Check if Domain name exists in the Target machine
                ansible_playbook_check=${ansible_playbook}"../CheckDomain.yaml -e domain_name="${domain_name}
                #Playbook will write in file $(pwd)/domain.txt True if domain name exists or False if not
                ${ansible_playbook_check}

                ansible_playbook_code=$?

                #If status code != 0 an error has occurred
                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                    echo "Playbook \"../CheckDomain.yaml\" exited with error." >&2
                    exit 1
                fi

                #Get True or False from $(pwd)/domain.txt
                flag=$(cat domain.txt)
                rm -f domain.txt

                if [[ ${flag} == "True" ]]; then
                    echo "Do you wish that Domain name to be stored so you will not be asked again? [y/n]"
                    read info

                    #Delete DOMAIN_NAME key from ../group_vars/all.yaml IF previously existed
                    sed -i "/DOMAIN_NAME/ d" ../group_vars/all.yaml
                    #Store info DOMAIN_NAME: "{domain_name}" in ../group_vars/all.yaml
                    sed -i -e '1iDOMAIN_NAME: "'${domain_name}'"\' ../group_vars/all.yaml

                    if [[ ${1} == "restart" ]]; then
                        #If portainer function called from restart.sh, restart Secure Portainer only
                        skippedTags="portainer,exareme"
                        echoErrorMessage="Playbook \"../Start-Exareme.yaml --skip-tags portainer,exareme\" exited with error."
                        echoMessage="\nSecure Portainer service just restarted.."
                    else
                        #portainer function called from start-services/tasks/main.yaml, start Secure Portainer + exareme
                        skippedTags="portainer"
                        echoErrorMessage="Playbook \"../Start-Exareme.yaml --skip-tags portainer\" exited with error."
                        echoMessage="\nExareme services and Secure Portainer service are now running"

                    fi

                    ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags "${skippedTags}
                    ${ansible_playbook_start}

                    ansible_playbook_code=$?

                    #If status code != 0 an error has occurred
                    if [[ ${ansible_playbook_code} -ne 0 ]]; then
                        echo ${echoErrorMessage} >&2
                        exit 1
                    fi
                    echo -e ${echoMessage}

                    while true
                    do
                        if [[ ${info} == "y" ]]; then
                            break
                        elif [[ ${info} == "n" ]]; then
                            echo -e "\nYou will be asked again to provide the domain name.."

                            #After Playbook is done, remove that info since the user did not want to store info for domain_name
                            sed -i "/DOMAIN_NAME/ d" ../group_vars/all.yaml
                            break
                        else
                            echo ${info}" is not a valid answer! Try again.. [ y/n ]"
                            read info
                        fi
                    done
                #Domain_name provided by the user does not exist
                else
                    #If portainer function called from restart.sh
                    if [[ ${1} == "restart" ]]; then
                        echo -e "\nNo certificate for that Domain name: "${domain_name}". Portainer services did not restart.."
                        sleep 1
                    #portainer function called from start-services/tasks/main.yaml, start only exareme because certificate not exist
                    else
                        echo -e "\nNo certificate for that Domain name: "${domain_name}". Starting without Portainer.."
                        sleep 1
                        ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer,portainerSecure"
                        ${ansible_playbook_start}

                        ansible_playbook_code=$?

                        #If status code != 0 an error has occurred
                        if [[ ${ansible_playbook_code} -ne 0 ]]; then
                            echo "Playbook \"Start-Exareme.yaml --skip-tags portainer,portainerSecure\" exited with error." >&2
                            exit 1
                        fi
                        echo -e "\nExareme services are now running"
                    fi
                fi
            fi
            break

        #Run Non secure Portainer
        elif [[ ${answer} == "n" ]];then
            if [[ ${1} == "restart" ]]; then
                #if portainer service called from restart.sh, restart only Non secure Portainer
                skippedTags="portainerSecure,exareme"
                echoErrorMessage="Playbook \"../Start-Exareme.yaml --skip-tags portainerSecure,exareme\" exited with error."
                echoMessage="\nNon Secure Portainer service just restarted.."
            else
                #portainer function called from start-services/tasks/main.yaml, start Non secure portainer + exareme
                skippedTags="portainerSecure"
                echoErrorMessage="Playbook \"../Start-Exareme.yaml --skip-tags portainerSecure\" exited with error."
                echoMessage="\nExareme services and Non secure Portainer service are now running"

            fi
                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags "${skippedTags}
                ${ansible_playbook_start}

                ansible_playbook_code=$?

                #If status code != 0 an error has occurred
                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                    echo ${echoErrorMessage} >&2
                    exit 1
                fi
                echo -e ${echoMessage}
            break
        else
            echo ${answer}" is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
    return
}

# If include-only flag is given don't execute the script
if [[ "$1" == "include-only" ]]; then
  return
fi

echo -e "\nInitializing Swarm, initializing mip-federation network, copying Compose-Files folder to Manager of Swarm..."
sleep 1

# Init_swarm
ansible_playbook_init=${ansible_playbook}"../Init-Swarm.yaml"
${ansible_playbook_init}

ansible_playbook_code=$?
# If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Init-Swarm.yaml\" exited with error." >&2
    exit 1
fi

# Join_workers
echo -e "\nJoining worker nodes in Swarm..\n"
while IFS= read -r line; do
    if [[ "$line" = *"[workers]"* ]]; then
        while IFS= read -r line; do
            ansible_playbook_join=${ansible_playbook}"../Join-Workers.yaml -e my_host="
            worker=$(echo "$line")
            if [[ -z "$line" ]]; then
                continue        #If empty line continue..
            fi
            if [[ "$line" = *"["* ]]; then
                break
            fi
            ansible_playbook_join+=${worker}
            flag=0
            ${ansible_playbook_join}

            ansible_playbook_code=$?
            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Join-Workers.yaml\" exited with error." >&2
                exit 1
            fi
            echo -e "\n${worker} is now part of the Swarm..\n"
            sleep 1
        done
    fi
done < ../hosts.ini
if [[ ${flag} != "0" ]]; then
    echo -e "\nIt seems that no workers will join the Swarm. If you have workers \
make sure you include them when initializing the exareme swarm target machines' information (hosts.ini, vault.yaml)."
    echo -e "\nContinue? [ y/n ]"

    read answer
    while true
    do
        if [[ "${answer}" == "y" ]]; then
            echo "Continuing without Workers.."
            break
        elif [[ "${answer}" == "n" ]]; then
            echo "Exiting...(Leaving Swarm for Master node).."
            ansible_playbook_leave=${ansible_playbook}"../Leave-Master.yaml"
            ${ansible_playbook_leave}

            ansible_playbook_code=$?
            # If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"Leave-Master.yaml\" exited with error." >&2
                exit 1
            fi
            exit 1
        else
            echo "$answer is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
fi

#Start Exareme
echo -e "\nStarting Exareme services...Do you wish to run Portainer service as well [ y/n ]?"
read answer
while true
do
    if [[ "${answer}" == "y" ]]; then
        portainer
        break
    elif [[ "${answer}" == "n" ]]; then
        #Run only Exareme, skip portainer and portainerSecure tags
        ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer,portainerSecure"
        ${ansible_playbook_start}

        ansible_playbook_code=$?

        #If status code != 0 an error has occurred
        if [[ ${ansible_playbook_code} -ne 0 ]]; then
            echo "Playbook \"Start-Exareme.yaml --skip-tags portainer,portainerSecure\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services are now running"
        break
    else
        echo "$answer is not a valid answer! Try again.. [ y/n ]"
        read answer
    fi
done