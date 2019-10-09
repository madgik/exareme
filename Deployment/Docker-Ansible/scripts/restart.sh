#!/usr/bin/env bash

init_ansible_playbook

echo -e "\nChoose one of the options [ 1-2-3 ] :"
echo "1. Restart Exareme"
echo "2. Restart Portainer"
echo -e "3. Restart Exareme and Portainer\n"

read answer

while true
do
    if [[ "${answer}" == "1" ]]; then
        #Stop EXAREME services
        stop 1
        break
    elif [[ "${answer}" == "2" ]]; then
		#Stop EXAREME services
		stop 2
        break
    elif [[ "${answer}" == "3" ]]; then
		#Stop EXAREME services
		stop 3
        break
    else
        echo "$answer is not a valid answer! Try again.. [ 1-2-3 ]"
        read answer
    fi
done

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Stop-Services.yaml\" exited with error." >&2
    exit 1
fi

if [[ "${answer}" == "1" ]]; then
    echo -e "\nStarting Exareme services..."

	ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags portainer"
	${ansible_playbook_start}
	ansible_playbook_code=$?

	
elif [[ "${answer}" == "2" ]]; then
    echo -e "\nStarting Portainer services..."

	ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml --skip-tags exareme"
	${ansible_playbook_start}
	ansible_playbook_code=$?

elif [[ "${answer}" == "3" ]]; then
    echo -e "\nStarting all services..."
	
	ansible_playbook_start=${ansible_playbook}"../Start-Exareme.yaml"
	${ansible_playbook_start}
	ansible_playbook_code=$?

fi

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
    exit 1
fi
