#!/usr/bin/env bash

init_ansible_playbook
notFound=0

portainer () {
    echo -e "\nDo you wish to run Portainer in a secure way? (SSL certificate required) [ y/n ]"
    read answer

    while true
    do
        if [[ ${answer} == "y" ]]; then
            while IFS= read -r line || [[ -n "$line" ]]; do
                if [[ "$line" == *DOMAIN_NAME:* ]]; then
                    domain_name=$(echo "$line" | cut -d ':' -d ' ' -d '"' -f 2 -d '"')

                    if [[ ${domain_name} != "" ]]; then
                        ansible_playbook_check=${ansible_playbook}"../CheckDomain.yaml -vvvv -e domain_name="${domain_name}
                        ${ansible_playbook_check}

                        flag=$(cat domain.txt)

                        if [[ ${flag} == "True" ]]; then
                            if [[ ${1} == "restart" ]]; then
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml -tags portainerSecure"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nSecure Portainer service are now running"

                            else
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nExareme services and Secure Portainer service are now running"

                            fi

                        elif [[ ${flag} == "False" ]]; then
                            echo -e "\nNo certificate for that Domain name: "${domain_name}". Starting without Portainer.."

                            ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer,portainerSecure"
                            ${ansible_playbook_start}

                            ansible_playbook_code=$?

                            #If status code != 0 an error has occurred
                            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
                                exit 1
                            fi
                            echo -e "\nExareme services are now running"
                        fi
                        rm -f domain.txt
                    else
                       notFound=1
                       break
                    fi
                else
                    notFound=1
                    break
                fi
                break
            done < ../group_vars/all.yaml

            if [[ ${notFound} == "1" ]]; then
                echo -e "\nWhat is the Domain name for which an SSL certificate created?"
                read domain
                domain_name=${domain}

                ansible_playbook_check=${ansible_playbook}"../CheckDomain.yaml -e domain_name="${domain_name}
                ${ansible_playbook_check}

                flag=$(cat domain.txt)

                if [[ ${flag} == "True" ]]; then
                    echo "Do you wish that Domain name to be stored so you will not be asked again? [y/n]"
                    read info
                    while true
                    do
                        if [[ ${info} == "y" ]]; then
                            sed -i "/DOMAIN_NAME/ d" ../group_vars/all.yaml
                            sed -i -e '1iDOMAIN_NAME: "'${domain_name}'"\' ../group_vars/all.yaml

                            if [[ ${1} == "restart" ]]; then
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml -tags portainerSecure"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nSecure Portainer service are now running"
                            else
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nExareme services and Secure Portainer service are now running"
                            fi

                            break
                        elif [[ ${info} == "n" ]]; then
                            echo "You will be asked again to provide the domain name.."

                            sed -i "/DOMAIN_NAME/ d" ../group_vars/all.yaml
                            sed -i -e '1iDOMAIN_NAME: "'${domain_name}'"\' ../group_vars/all.yaml

                            if [[ ${1} == "restart" ]]; then
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml -tags portainerSecure"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nSecure Portainer service are now running"
                            else
                                echo ${1}
                                #Run secure Portainer
                                ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer"
                                ${ansible_playbook_start}

                                ansible_playbook_code=$?

                                #If status code != 0 an error has occurred
                                if [[ ${ansible_playbook_code} -ne 0 ]]; then
                                    echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                                    exit 1
                                fi
                                echo -e "\nExareme services and Secure Portainer service are now running"
                            fi

                            sed -i "/DOMAIN_NAME/ d" ../group_vars/all.yaml
                            break
                        else
                            echo ${info}" is not a valid answer! Try again.. [ y/n ]"
                            read info
                        fi
                    done

                else
                    echo -e "\nNo certificate for that Domain name: "${domain_name}". Starting without Portainer.."
                    ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer,portainerSecure"
                    ${ansible_playbook_start}

                    ansible_playbook_code=$?

                    #If status code != 0 an error has occurred
                    if [[ ${ansible_playbook_code} -ne 0 ]]; then
                        echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
                        exit 1
                    fi
                    echo -e "\nExareme services are now running"
                fi
            fi
            break
        elif [[ ${answer} == "n" ]];then
            ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainerSecure"
            ${ansible_playbook_start}

            ansible_playbook_code=$?

            #If status code != 0 an error has occurred
            if [[ ${ansible_playbook_code} -ne 0 ]]; then
                echo "Playbook \"../Start-Exareme.yaml\" exited with error." >&2
                exit 1
            fi
            echo -e "\nExareme services and non Secure Portainer service are now running"
            break
        else
            echo ${answer}" is not a valid answer! Try again.. [ y/n ]"
            read answer
        fi
    done
    return
}

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
            echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
            exit 1
        fi
        echo -e "\nExareme services are now running"
        break
    else
        echo "$answer is not a valid answer! Try again.. [ y/n ]"
        read answer
    fi
done