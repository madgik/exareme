# Exareme Deployment Guide

Here you will find all the informations needed in order to deploy Exareme in your environment via Ansible scripts. 

We will refer to the machine from which you run the ansible scripts as Host and to the machines where you will install the Exareme nodes [master/workers] as Remote.

# Requirements

1) Install Ansible in Host machine:

```sudo apt install ansible```

2) Install Python in all Remote hosts, in order for playbooks to run:

```sudo apt-get install python```

### Important
In every node there should be a folder containing 2 files:
1) the datasets.csv file with all the datasets combined and
2) the CDEsMetadata.json file.
We will refer to the path of that folder as ```data_path```. The ```data_path``` can be different across the nodes.

# Preparation

## Initialize variables

1) Changes in ```hosts.ini```

Here is an example of hosts.ini where we have 3 Remote machines, one [master] of Exareme and two [workers] of Exareme.

```
   [master]
   master ansible_host=88.197.53.38
   master home_path=/home/exareme/
   master data_path=/home/exareme/data/

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
   worker1 data_path=/home/exareme/data/

   worker1 remote_user="{{worker1_remote_user}}"
   worker1 become_user="{{worker1_become_user}}"
   worker1 ansible_become_pass="{{worker1_become_pass}}"
   worker1 ansible_ssh_pass="{{worker1_ssh_pass}}"


   [worker2]
   worker2 ansible_host=88.197.53.100
   worker2 hostname=thanasis1
   worker2 data_path=/home/exareme/data/

   worker2 remote_user="{{worker2_remote_user}}"
   worker2 become_user="{{worker2_become_user}}"
   worker2 ansible_become_pass="{{worker2_become_pass}}"
   worker2 ansible_ssh_pass="{{worker2_ssh_pass}}"
```
[You can find the hostname of any machine by executing ```hostname``` in terminal]

[Requirement1: Mind that the variable ```data_path``` is the path where your Data CSV and the Metadata file are stored in your Remote Host.]
[Requirement2: Mind that the variable ```home_path``` is the path where ```Compose-Files``` will be stored in the master node. Compose-Files
contains 2 docker-compose.yaml files for deploying the services. It can be Any path]

You can see that there are 2 main categories in hosts.ini file. The first one is ```[master]```, the second one is ```[workers]```.

You can always add more workers following the template given above:
a) by adding the name of the worker under [workers] and
b) creating a tag [worker3] with all the necessary variables. For example: 

```
   worker3 ansible_host=Your_Remote_Machine_Host
   worker3 hostname=Your_Remote_Machine_Hostname
   worker3 data_path=Your_Remote_Data_Path_where_CSV_data_and_CDEsMetadata_are_stored

   worker3 remote_user="{{worker3_remote_user}}"
   worker3 become_user="{{worker3_become_user}}"
   worker3 ansible_become_pass="{{worker3_become_pass}}"
   worker3 ansible_ssh_pass="{{worker3_ssh_pass}}"
```

For consistency reasons we suggest you keep the names as shown above [master,worker1,worker2..], and just increase the number after [worker] each time you add one.

## Ansible-vault

As you can also see in hosts.ini file we have some sensitive data like usernames and passwords in both master and workers. These lines ```MUST not be changed!```.

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

With ansible-vault we can have an encrypted file which will contain sensitive information like the ones shown above.

In order to create the file you need to
```ansible-vault create vault_file.yaml``` inside ```Docker-Ansible``` folder.
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
all in plaintext. If you have more than 2 workers, you will add those too by adding ```workerN_...``` in front of each variable where N the increased number. 
[Keep in mind that your password can be anything you want But ansible has a special character for comments ```#``` . If your password contains that specific character ansible will take the characters next to it as comments.]
When you exit you can see that vault_file.yaml is encrypted with all your sensitive informations in there.

If you want to edit the file you can do so whenever by
```ansible-vault edit vault_file.yaml```
Place your vault password and edit the file.

### [Optional] Regarding Ansible-vault password. 
(source https://docs.ansible.com/ansible/latest/user_guide/playbooks_vault.html)

As mentioned before, each time you run a playbook you will need to enter your password.

Alternatively, ansible-vault password can be specified with a file ```~/.vault_pass.txt``` or a script (the script version will require Ansible 1.7 or later). When using this flag, ensure permissions on the file are such that no one else can access your key and do not add your key to source control:
examples:

```ansible-playbook site.yml --vault-password-file ~/.vault_pass.txt```

```ansible-playbook site.yml --vault-password-file ~/.vault_pass.py```

The password should be a string stored as a single line in the file.

If you are using a script instead of a flat file, ensure that it is marked as executable, and that the password is printed to standard output. If your script needs to prompt for data, prompts can be sent to standard error.

More guidance will be provided in that matter if you select to deploy via script (see below)

# Deployment

In the Docker-Ansible folder run the ```deploy.sh``` to start the deployment.

You will be prompted to provide any more information needed.

# Deployment [Manual]

### Swarm Initialization

For the initialization of Swarm you have to run on the master node:

```ansible-playbook -i hosts.ini Init-Swarm.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

### Join Workers

If you have worker nodes available you should do the following for each worker:

``` ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=worker1"``` by changing the value in ```my_host``` with your worker name.

### Start Exareme Services

Next thing would be to run Exareme services and Portainer service. The Exareme services will run on all available exareme nodes (master,workers).

```ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv```

If you want to exclude Portainer service from running, you need to add ```--skip-tags portainer``` in the command, meening:

```ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml --skip-tags portainer -vvvv```

If you want to start only Portainer Service you need to:
```
ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko --ask-vault-pass -e@vault_file.yaml -vvvv --tags portainer
```

### Stop Services

If you want to stop Exareme services [master/workers] but no Portainer services, you can do so by:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv --tags exareme -vvvv```

If you only want to stop the Portainer service you can do so by:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko --ask-vault-pass -e@vault_file.yaml -vvvv --skip-tags exareme -vvvv```

If you want to stop all services [Exareme master/Exareme workers/Portainer]:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko --ask-vault-pass -e@vault_file.yaml  -vvvv```


### Add an Exareme Worker when the master is already running

After inserting the nodes information in the hosts.ini and the ansible-vaut file you can run the ```deploy.sh``` script.

You can also do it manually with the following commands:
1) Join the particular worker by replacing workerN with the appropriate name: 
``` ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"``` 

2) Start the Exareme service for that particular worker by replacing workerN with the appropriate name: ``` ansible-playbook -i hosts.ini Start-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"```

### Stop ΟΝΕ Exareme Worker 

If at some point you need to stop only one worker, you can do so by the following command replacing workerN with the appropriate identifier: 
``` ansible-playbook -i hosts.ini Stop-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"```

## Test that everything is up and running 

If all went well, everything should be deployed! 

### Master's terminal

Check your Manager node of Swarm by 
```docker node ls ``` to see if you have the proper nodes and ```docker inspect ID_Of_A_Node --pretty``` to see if under ```Labels``` key ```name ``` has a value. 

### Portainer

You can also check the Portainer to see if all services are up and running by accessing the Address: ```Manager_Of_Swarm_IP:9000```.

The first time you launch Portainer you have to create a user. 
Fill in the ```Username```, ```Password```, ```Confirm Password``` fields and click ```Create user```. 
[Mind that we create a folder called ```portainer``` in your ```home_path``` where the credentials you provided will be saved for the next times, until you delete the folder] 
The next page ```Connect Portainer to the Docker environment you want to manage.``` will ask you to Connect Portainer to an Environment. Click the first option  ```Local``` and ```Connect```. 
After that, you should click on your Local Swarm and navigate from the left menu. 
Go to your ```Services``` to check each service's logs and see if everything is running properly.

### Troubleshooting

1) Under ```Services``` in the left menu check that all services has 1 replicas: ```replicated 1 / 1```. 

If this is not the case for the Worker nodes, meening you get 0 replicas: ```replicated 0 / 1```, then it is possible that you started an Exareme Worker service before joining that Worker in the Swarm. 
i) Stop the spesific Worker via:
``` ansible-playbook -i hosts.ini Stop-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"``` , 
ii) Join the spesific Worker in the Swarm via: 
``` ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"```  
iii) Start the spesific Worker Node: 
``` ansible-playbook -i hosts.ini Start-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault_file.yaml -vvvv -e "my_host=workerN"```

