```In progress```

```Installations```

Install Ansible in host machine

```
   apt install python-setuptools -y
   easy_install pip
   pip install ansible
```
OR

```
   sudo apt-get install python-pip
   pip install ansible
```

All nodes should have python installed beforehand, in order for playbooks to run in every node
```
   sudo apt-get install python
```

```Initialize variables```

1) Changes in ```group_vars/all.yaml```
```
   cd docker-ansible/group_vars
   vi all.yaml
```
Changes you need to make are:

```
   #Local
   METADATA_PATH: "Local absolute path to variablesMetadata.json file" (this may not be needed)
   COMPOSE_FILES_FOLDER_PATH: "Local absolute path to /Compose-Files. Relative path should be ~/Compose-files but ansible does not work with relative paths."

   #Remote
   COMPOSE_FILES_REMOTE_PATH: "Remote absolute path to /Compose-Files/ in your remote host who is the Manager of Swarm. Compose files is a folder in which you can find the 2 docker-compose files for docker stack deploy, one for manager one for workers." (maybe it can be changed to "remote absolute path to home"/Compose-Files provided by me)
```

2) Changes in hosts.ini

In hosts.ini you can find 2 main categories.

The first one is ```[manager]```. This demo is only tested with one (1) manager node.

You should change
```
manager1 ansible_host={{remote host IP_for_Manager_of_Swarm}}
manager1 remote_data_path={{PATH where your data (csv file) is located}}
manager1 destination_path={{PATH where your Compose-Files folder will be located}} (or else /home/Compose-Files)
```

```Ansible-vault```
With ansible-vault we can have an encrypted file which will contain sensitive information like the ones shown below:
```
manager1 remote_user="{{manager1_remote_user}}"
manager1 become_user="{{manager1_become_user}}"
manager1 ansible_become_pass="{{manager1_become_pass}}"
manager1 ansible_ssh_pass="{{manager1_ssh_pass}}"
```
Mind that ```manager1``` is the name of our host in host.ini. You can replace is with your own identification. Just replace everything above and in hosts.ini for consistency.

In order to create the file you need to
```ansible-vault create vault_file.yaml```
It will ask for a vault-password that you will need to enter it each time you run a playbook. So keep it in mind

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

There you will add your worker nodes. As shown in hosts.ini. It is important to keep the stracture as is, and just add workers. So every time you add a worker node you have to add only the name under [worekrs] and have a separate tag [your_worker] with the appropriate variables. 

At this point and if you have a worker node available you will need to edit vault_files in order to include sensitive informations about the remote host that will play the role of a worker. Meening 

```
worker1_remote_user={{your_remote_user_in_remote_host}}
worker1_become_user={{your_become_user_in_remote_host}}
worker1_become_pass={{your_become_password_in_remote_host}}
worker1_ssh_pass={{your_ssh_password_in_remote_host}}
```
Regarding workers in hosts.ini, you also need the information about the hostname of the node. So add
```
worker1 hostname={{hostname_of_remote_worker_host}}
```

```Deploy everything```
Since we did the changes needed in ```docker-ansible/group_vars/all.yaml``` and ```hosts.ini```, we are ready for the deployment.

If you do not have a variablesMetadata.json file you can simply run. Metadata file will be copied in every node.

```ansible-playbook -i hosts.ini Metadata.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

For the initialization of Swarm you have to run
```
ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```

4) If you have worker nodes available
```
ansible-playbook -i hosts.ini Join-workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```
and then
```
ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags labels
```
With the above command only tasks with tags ```labels``` will run. That is needed in order to run exareme services in each node

5) Next thing would be to run Exareme services and Portainer service
```
ansible-playbook -i hosts.ini Start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv
```
If you want to exclude Portainer service from running, you need to add --skip-tags portainer in the command
```
ansible-playbook -i hosts.ini docker-start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml --skip-tags portainer -vvvv
```
6) If you want to stop all services:
```
ansible-playbook -i hosts.ini Stop-services -c paramiko --ask-vault-pass -e@vault_file.yaml  -vvvv
```

Or If you only want to stop portainer you can do so by:
```
ansible-playbook -i hosts.ini docker-stop-services.yaml -c paramiko -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --skip-tags exareme -vvvv
```
Or If you only want to stop Exareme services you can do so by:
```
ansible-playbook -i hosts.ini docker-stop-services.yaml -c paramiko -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags exareme -vvvv
```

