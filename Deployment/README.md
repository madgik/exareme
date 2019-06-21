```Installations```

Install Ansible in host machine:

```apt install python-setuptools -y```

```easy_install pip```

```pip install ansible```

or

```sudo apt-get install python-pip```

```pip install ansible```

All nodes should have python installed beforehand, in order for playbooks to run in every node:

```sudo apt-get install python```

```PROJECT STRUCTURE```

In the Deployment folder you can find 2 additional folders. 
   The first one "Compose-Files" contains 2 docker-compose.yml files which are needed in order to docker deploy the services in the right nodes [manager/workers]. With procedure we provide, the folder will be copied in the correct remote host. 
   The second folder contains the group_vars, the host file, playbooks and roles in order to deploy via ansible.

```Initialize variables```

In order to run the scripts with your own customized variables, you need to make some changes.

1) Changes in ```group_vars/all.yaml```

```cd docker-ansible/group_vars```

```vi all.yaml```

Changes you need to make is fill in the ```METADATA_LOCAL_PATH``` value, and ```COMPOSE_FILES_LOCAL_PATH```:

```
   #Local [refers to your HOST machine from which you will run your playbooks]
   METADATA_LOCAL_PATH: "Local_absolute_path_to_CDEsMetadata.json_file" (this may not be needed)
   
  COMPOSE_FILES_LOCAL_PATH: "Local_absolute_path_to_/Compose-Files_Relative_path_should_be_~/Compose-files_but_ansible_does_not_work_with_relative_paths."

 ```

2) Changes in ```hosts.ini```

The structure in hosts.ini file is the following:

```
   [manager]
   name_of_manager_node_(ex manager1): variable1
   name_of_manager_node_(ex manager1): variable2
   etc..
   
   [workers]
   name_of_worker1_node_(ex worker1)
   name_of_worker2_node_(ex worker2)
   etc..
   
   [worker1] (each worker's name should be in a tag like this)
   name_of_worker1_node: variable1
   etc..
   
   [worker2]
   name_of_worker2_node: variable1
   etc..
```

You can see that there are 2 main categories. The first one is ```[manager]```, the second one is ```[workers]```. This demo is only tested with one (1) manager node and one (1) worker. You can always add more than one at each category following the template in hosts.ini.

The changes you should make regarding the [master] node are the following: (keep in mind that ```manager1``` is just a name and if you wish you can change it to whatever you like But make sure you change it Everywhere in the file)

```
   manager1 ansible_host={{Remote_HOST_IP_for_Manager_of_Swarm}}
   manager1 remote_data_path={{PATH_where_your_data_(csv_file)_is_located_in_your_Remote_HOST}}
   manager1 compose_files_remote_path={{PATH_to_your_Compose_Files_folder_in_your_Remote_HOST}}
```
As you can also see in hosts.ini file we have some sensitive data like ```remote_user``` and some passwords. 
```
   manager1 remote_user="{{manager1_remote_user}}"
   manager1 become_user="{{manager1_become_user}}"
   manager1 ansible_become_pass="{{manager1_become_pass}}"
   manager1 ansible_ssh_pass="{{manager1_ssh_pass}}"
```
It is not a valid technique to just fill in your sensitive data there, so we used ```ansible-vault```
Ansible-vault comes the installation of ansible. Make sure you have it installed by ```ansible-vault --version```

```Ansible-vault```
With ansible-vault we can have an encrypted file which will contain sensitive information like the ones shown above.
Mind again that ```manager1``` is the name of our machine in host.ini. You can replace is with your own identification. Just replace everything above and in hosts.ini for consistency.

In order to create the file you need to
```ansible-vault create vault_file.yaml```
It will ask for a vault-password that you will need to enter it each time you run a playbook. So keep it in mind.

Here you will add
```
   manager1_remote_user={{your_remote_user_in_remote_host}}
   manager1_become_user={{your_become_user_in_remote_host}}
   manager1_become_pass={{your_become_password_in_remote_host}}
   manager1_ssh_pass={{your_ssh_password_in_remote_host}}
```
all in plaintext. When you exit you can see that vault_file.yaml is encrypted with all your sensitive informations in there.

If you want to edit the file you can do so by
```ansible-vault edit vault_file.yaml```
Place your vault password and edit the file.

Second category of hosts.ini will be [workers]. 

There you will add your worker nodes. As shown in hosts.ini. It is important to keep the stracture as is, and just add workers. So every time you add a worker node you have to add only the name under [worekrs] and have a separate tag [name_of_worker1_node_(ex worker1)] with the appropriate variables. 

The first things you need to change are:
```
   worker1 ansible_host={{Remote_HOST_IP_for_worker1_of_Swarm}}
   worker1 remote_data_path={{PATH_where_your_data_(csv_file)_is_located_in_your_Remote_HOST}}
   worker1 hostname={{Hostname_of_Remote_Host}}
```

At this point you will need to edit the ```vault_file.yaml``` by ```ansible-vault edit vault_file.yaml``` in order to include sensitive informations about the remote host that will play the role of a worker. Meening:

```
   worker1_remote_user={{your_remote_user_in_remote_host}}
   worker1_become_user={{your_become_user_in_remote_host}}
   worker1_become_pass={{your_become_password_in_remote_host}}
   worker1_ssh_pass={{your_ssh_password_in_remote_host}}
```

```Deploy everything```
Since we made the changes needed in ```docker-ansible/group_vars/all.yaml``` and ```hosts.ini```, we are ready for the deployment. Go inside the ```docker-ansible``` folder.

If your remote machines do not have ```CDEsMetadata.json``` file available you can simply copy your file from the host machine into the remote machines by simply doing:

```ansible-playbook -i hosts.ini Metadata.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv``` 
Notice that every time you will need to place your ansible-vault password.

For the initialization of Swarm you have to run
```
   ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```

If you have worker nodes available
```
   ansible-playbook -i hosts.ini Join-workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```
and then
```
   ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags labels
``` 
in order for worker nodes to have label names since only tasks with tags ```labels``` will run. That is needed in order to run Exareme services in each node.

Next thing would be to run Exareme services and Portainer service
```
   ansible-playbook -i hosts.ini Start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```
If you want to exclude Portainer service from running, you need to add ```--skip-tags portainer``` in the command, meening:
```
   ansible-playbook -i hosts.ini Start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml --skip-tags portainer -vvvv
```
If you want to stop all services:
```
ansible-playbook -i hosts.ini Stop-services -c paramiko --ask-vault-pass -e@vault_file.yaml  -vvvv
```
Or If you only want to stop portainer you can do so by:
```
   ansible-playbook -i hosts.ini Stop-services -c paramiko --ask-vault-pass -e@vault_file.yaml -vvvv --skip-tags exareme -vvvv
```
Or If you only want to stop Exareme services you can do so by:
```
   ansible-playbook -i hosts.ini Stop-services -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags exareme -vvvv
```

If all went well, everything should be deployed! Check your manager node of Swarm by 
```docker node ls ``` to see if you have the proper nodes and ```docker inspect ID_of_a_node``` to see if the label name has a value. 
