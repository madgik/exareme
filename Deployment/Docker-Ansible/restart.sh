#!/usr/bin/env bash

init_ansible_playbook

echo -e "\nChoose one of the options [ 1-2-3 ] :"
echo "1. Restart Exareme"
echo "2. Restart Portainer"
echo "3. Restart Exareme and Portainer"


read answer
while true
do
    if [[ "${answer}" == "1" ]]; then
        echo -e "\nStopping Exareme services..."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags portainer"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?
		
        break
    elif [[ "${answer}" == "2" ]]; then
        echo -e "\nStopping Portainer services..."

        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml --skip-tags exareme"
        ${ansible_playbook_stop}
        ansible_playbook_code=$?
		
        break
    elif [[ "${answer}" == "3" ]]; then
        echo -e "\nStopping all services..."
		
        ansible_playbook_stop=${ansible_playbook}"Stop-Services.yaml"
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

if [[ "${answer}" == "1" ]]; then
    echo -e "\nStarting Exareme services..."

	ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml --skip-tags portainer"
	${ansible_playbook_start}
	ansible_playbook_code=$?
	
	break
	
elif [[ "${answer}" == "2" ]]; then
    echo -e "\nStarting Portainer services..."

	ansible_playbook_stop=${ansible_playbook}"Start-Exareme.yaml --skip-tags exareme"
	${ansible_playbook_start}
	ansible_playbook_code=$?
	
	break
	
elif [[ "${answer}" == "3" ]]; then
    echo -e "\nStarting all services..."
	
	ansible_playbook_start=${ansible_playbook}"Start-Exareme.yaml"
	${ansible_playbook_start}
	ansible_playbook_code=$?
	
	break
	
fi

#If status code != 0 an error has occurred
if [[ ${ansible_playbook_code} -ne 0 ]]; then
    echo "Playbook \"Start-Exareme.yaml\" exited with error." >&2
    exit 1
fi
