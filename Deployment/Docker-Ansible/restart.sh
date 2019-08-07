#!/usr/bin/env bash

init_ansible_playbook

echo -e "\nChoose one of the options:"
echo "If you want to stop Exareme services ALONG with Portainer service press 1"
echo "If you want to stop Exareme services ONLY press 2"
echo "If you want to stop Portainer service ONLY press 3"


read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        echo -e "\nYou choose to stop everything.."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?
        break
    elif [[ "${answer}" == "2" ]]; then
        echo -e "\nYou choose to stop Exareme services only.."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags portainer"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?
        break
    elif [[ "${answer}" == "3" ]]; then
         echo -e "\nYou choose to stop Portainer service only.."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags exareme"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?
        break
    else
        echo "$answer is not a valid answer! Try again.. [1/2/3"
        read answer
    fi
done

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Stop-Services.yaml\" exited with error." >&2
    exit 1
fi


echo -e "\nChoose one of the options:"
echo "If you want to start Exareme services ALONG with Portainer service press 1"
echo "If you want to start Exareme services ONLY press 2"
echo "If you want to start Portainer service ONLY press 3"

read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        echo -e "\nYou choose to start everything.."
        ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml"
        ${ansible_playbook_start}
        ansible_playbook_code=$?
        break
    elif [[ "${answer}" == "2" ]]; then
        echo -e "\nYou choose to start Exareme services only.."

        ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml --skip-tags portainer"
        ${ansible_playbook_start}
        ansible_playbook_code=$?
        break
    elif [[ "${answer}" == "3" ]]; then
         echo -e "\nYou choose to start Portainer service only.."

        ansible_playbook_stop=${ansible_playbook}"Start-Exareme.yaml --skip-tags exareme"
        ${ansible_playbook_start}
        ansible_playbook_code=$?
        break
    else
        echo "$answer is not a valid answer! Try again.. [1/2/3"
        read answer
    fi
done

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
    exit 1
fi
