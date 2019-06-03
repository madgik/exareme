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

1) Changes in group_vars/all.yaml
```
cd docker-ansible/group_vars
vi all.yaml
```

The changes you need to make are:
```#Local
   METADATA_PATH: "Local absolute path to variablesMetadata.json file"
   COMPOSE_FILES_FOLDER_PATH: "Local absolute path to /Compose-Files. Relative path should be ~/Compose-files but ansible does not work with relative paths."

   #Remote
   COMPOSE_FILES_REMOTE_PATH: "Remote absolute path to /Compose-Files/ in your remote host who is the Manager of Swarm. Compose files is a folder in which you can find the 2 docker-compose files for docker stack deploy, one for manager one for workers."
```

2) Changes in hosts.ini
In hosts.ini you can find 2 main categories.
The first one is ```[manager]```. This demonstartion only tested with one (1) manager.
You should change
```
manager1 ansible_host={{remote host IP for Manager of Swarm}}
manager1 remote_data_path={{PATH where your data (csv file) is located}}
manager1 destination_path={{PATH where your Compose-Files folder will be located}}
```

```Ansible-vault```
With ansible-vault we can have an encrypted file which will contain sensitive information like the above:
```
manager1 remote_user="{{manager1_remote_user}}"
manager1 become_user="{{manager1_become_user}}"
manager1 ansible_become_pass="{{manager1_become_pass}}"
manager1 ansible_ssh_pass="{{manager1_ssh_pass}}"
```
In order to create the file you need to
```ansible-vault create vault_file.yaml```
Here you will add
```
manager1_remote_user={{your_remote_user_in_remote_host}}
manager1_become_user={{your_become_user_in_remote_host}}
manager1_become_pass={{your_become_password_in_remote_host}}
manager1_ssh_pass={{your_ssh_password_in_remote_host}}
```
all in plaintext. When you exit you will see that vault_file.yaml is encrypted with all your sensitive informations in there.

Second category will be [workers]. In there you will add your worker nodes. As shown in hosts.ini.
Same thing about the variables exist here, with the only different that we also need
```
worker1 hostname={{hostname_of_remote_worker_host}}
```

```Deploy everything```
Since we did the changes needed in docker-ansible/group_vars/all.yaml and hosts.ini, we are ready for the deployment.

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

