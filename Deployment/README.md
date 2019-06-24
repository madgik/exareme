# Deployment of Exareme

Here you will find all the informations needed in order to deploy Exareme in your environment via Ansible. Your Host machine is the one from wich you run the ansible scripts. Remote machines are the Exareme nodes [master/workers]. 

# Installations

Install Ansible in Host machine:

```sudo apt install ansible```

Install Python in all Remote hosts, in order for playbooks to run:

```sudo apt-get install python```

## Deployment scripts structure

In the Deployment folder you can find 2 additional folders. 
   The first one "Compose-Files" contains 2 docker-compose.yml files which are needed in order to docker deploy the services in the right nodes [master/workers]. With the procedure we provide below, the folder will be copied in the appropriate node. 
   The second folder contains the group_vars, the host file, playbooks and roles in order to deploy via ansible.

## Initialize variables

In order to run the scripts with your own customized variables, you need to make some changes.

1) If you want to use your own Metadata file, you have to copy the file inside ```Metadata``` folder and name it ```variablesMetadata.json```.

2) Changes in ```hosts.ini```

Here is an example of hosts.ini where we have 3 Remote machines, one [master] of Exareme and two [workers] of Exareme.

```
[manager]
master ansible_host=88.197.53.38
master compose_files_remote_path=/home/exareme/
master remote_data_path=/home/exareme/data/

master remote_user="{{master_remote_user}}"
master become_user="{{master_become_user}}"
master ansible_become_pass="{{master_become_pass}}"
master ansible_ssh_pass="{{master_ssh_pass}}"

[workers]
worker1
worker2

[worker1]
worker1 ansible_host=88.197.53.44
worker1 hostname=dl044
worker1 remote_data_path=/home/exareme/data/

worker1 remote_user="{{worker1_remote_user}}"
worker1 become_user="{{worker1_become_user}}"
worker1 ansible_become_pass="{{worker1_become_pass}}"
worker1 ansible_ssh_pass="{{worker1_ssh_pass}}"


[worker2]
worker2 ansible_host=88.197.53.100
worker2 hostname=thanasis1
worker2 remote_data_path=/home/exareme/data/

worker2 remote_user="{{worker2_remote_user}}"
worker2 become_user="{{worker2_become_user}}"
worker2 ansible_become_pass="{{worker2_become_pass}}"
worker2 ansible_ssh_pass="{{worker2_ssh_pass}}"
```
[You can find the hostname of any machine by executing ```hostname``` in a terminal]

You can see that there are 2 main categories. The first one is ```[manager]```, the second one is ```[workers]```.You can always add more workers following the template given above, a) by adding the name of the worker under [workers] and b) by creating a tag [worker3] with all the necessary variables below: 

```
worker3 ansible_host=Your_Remote_Machine_Host
worker3 hostname=Your_Remote_Machine_Hostname
worker3 remote_data_path=YOur_Remote_Data_Path

worker3 remote_user="{{worker3_remote_user}}"
worker3 become_user="{{worker3_become_user}}"
worker3 ansible_become_pass="{{worker3_become_pass}}"
worker3 ansible_ssh_pass="{{worker3_ssh_pass}}"
```

For consistency reasons we suggest you keep the names as shown above [master,worker1,worker2..], and just increase the number after [worker] each time you add one.

As you can also see in hosts.ini file we have some sensitive data like usernames and passwords in both master and workers. These lines ```must not change```.

```
   master remote_user="{{master_remote_user}}"
   master become_user="{{master_become_user}}"
   master ansible_become_pass="{{master_become_pass}}"
   master ansible_ssh_pass="{{master_ssh_pass}}"
   
   .......
   
   worker1 remote_user="{{worker1_remote_user}}"
   worker1 become_user="{{worker1_become_user}}"
   worker1 ansible_become_pass="{{worker1_become_pass}}"
   worker1 ansible_ssh_pass="{{worker1_ssh_pass}}"
   
   ......
   
   worker2 remote_user="{{worker2_remote_user}}"
   worker2 become_user="{{worker2_become_user}}"
   worker2 ansible_become_pass="{{worker2_become_pass}}"
   worker2 ansible_ssh_pass="{{worker2_ssh_pass}}"
```

It is not a valid technique to just fill in your sensitive data there, so we will use ```Ansible-Vault```.
Ansible-vault comes with the installation of ansible. Make sure you have it installed by ```ansible-vault --version```

## Ansible-vault

With ansible-vault we can have an encrypted file which will contain sensitive information like the ones shown above.

In order to create the file you need to
```ansible-vault create vault_file.yaml``` inside ```docker-ansible``` folder.
It will ask for a vault-password that you will need to enter it each time you run a playbook. So keep it in mind.

Here you will add
```
   master_remote_user: your_username
   master_become_user: your_username
   master_become_pass: your_password
   master_ssh_pass: your_password
   
   worker1_become_user: your_username
   worker1_remote_user: your_username
   worker1_become_pass: your_password
   worker1_ssh_pass: your_password
   
   worker2_become_user: your_username
   worker2_remote_user: your_username
   worker2_become_pass: your_password
   worker2_ssh_pass: your_password
```
all in plaintext. If you have more than 2 workers, you will add those too. 
When you exit you can see that vault_file.yaml is encrypted with all your sensitive informations in there.

If you want to edit the file you can do so whenever by
```ansible-vault edit vault_file.yaml```
Place your vault password and edit the file.


### Deploy everything

Since we made the changes needed, we are ready for the deployment. Go inside the ```docker-ansible``` folder.

If your remote machines do not have the Metadata file available you can simply copy your file from your Host machine into the Remote machines by simply doing:
[Notice that every time you run a playbook you will need to place your ansible-vault password.]

```ansible-playbook -i hosts.ini Metadata.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv``` 
Keep in mind that you need to place the Metadata file inside the Metadata folder with name: ```variablesMetadata.json```.

For the initialization of Swarm you have to run:

```ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

If you have worker node[s] available:

``` ansible-playbook -i hosts.ini Join-workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

and then
```ansible-playbook -i hosts.ini Init-swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags labels``` 

in order for worker node[s] to have label names since only tasks with tags ```labels``` will run. 

Next thing would be to run Exareme services and Portainer service

```ansible-playbook -i hosts.ini Start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

If you want to exclude Portainer service from running, you need to add ```--skip-tags portainer``` in the command, meening:

```ansible-playbook -i hosts.ini Start-services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml --skip-tags portainer -vvvv```

If you want to stop all services:

```ansible-playbook -i hosts.ini Stop-services -c paramiko --ask-vault-pass -e@vault_file.yaml  -vvvv```

Or If you only want to stop Portainer you can do so by:

```ansible-playbook -i hosts.ini Stop-services -c paramiko --ask-vault-pass -e@vault_file.yaml -vvvv --skip-tags exareme -vvvv```

Or If you only want to stop Exareme services you can do so by:

```ansible-playbook -i hosts.ini Stop-services -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags exareme -vvvv```

If all went well, everything should be deployed! Check your Manager node of Swarm by 
```docker node ls ``` to see if you have the proper nodes and ```docker inspect ID_of_a_node``` to see if the label name has a value. You can also check the Portainer to see if all services are up and running.
