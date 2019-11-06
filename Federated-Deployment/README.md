# Federated Exareme Deployment Guide

Here you will find all the information needed in order to deploy Exareme in your environment via Ansible scripts.

We will refer to the machine from which you run the ansible scripts as Admin and to the machines where you will install the Exareme nodes [master/workers] as Target.

# Requirements

1) Install Ansible (version 2.0.0.2) in Admin machine.

2) Install Python (version 2.7) in all Target machines, in order for playbooks to run.

3) Install Docker in all Target machines.

# Ports

Make sure the following ports are available:

```9090: for accessing Exareme```

```(Optional): 8500 for accessing Consul Key Value Store```

```(Optional): 9000 for accessing Portainer.io```

# Documentation

Make sure you have read the ```Federation_Specifications.md``` and ```Firewall_Configuration.md``` files exist under Documentation folder.
The first doc sums up everything regarding Docker Swarm and the second one how to deal if a firewall exists in the Federation nodes.

# Preparation

## Data Structure
In every node the DATA should follow a specific structure. We will refer to the path of the DATA folder as ```data_path```. The ```data_path``` can be different across the nodes.

The data folder should contain one folder for each pathology that it has datasets for. Inside that folder there should be:
1) the datasets.csv file with all the datasets combined and
2) the CDEsMetadata.json file for that specific pathology.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> datasets.csv <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> datasets.csv <br />
----------> CDEsMetadata.json <br />

The master node should have the CDEsMetadata.json for every pathology even if it doesn't contain a datasets.csv file.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> datasets.csv <br />
----------> CDEsMetadata.json <br />


## [Optional] Initialize Exareme Version

```This step can be done through the deploy script.```

If you want to do it manually you can go to the ```Federated-Deployment/Docker-Ansible/group_vars``` folder and create an ```exareme.yaml``` file.

The file should contain the following lines, modify them depending on the version of Exareme you want to deploy.

```
EXAREME_IMAGE: "hbpmip/exareme"
EXAREME_TAG: "v21.1.0"
```

## [Optional] Initialize Hosts

```This step can be done through the deploy script. If you have many nodes though it is easier to do it manually.```

If you want to do it manually you can go to the ```Federated-Deployment/Docker-Ansible/``` folder and create a ```hosts.ini``` file.

Here is an example of hosts.ini where we have 3 Target machines, one [master] of Exareme and two [workers] of Exareme.

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
worker88_197_53_44
worker88_197_53_100

[worker88_197_53_44]
worker88_197_53_44 ansible_host=88.197.53.44
worker88_197_53_44 hostname=dl044
worker88_197_53_44 data_path=/home/exareme/data/

worker88_197_53_44 remote_user="{{worker88_197_53_44_remote_user}}"
worker88_197_53_44 become_user="{{worker88_197_53_44_become_user}}"
worker88_197_53_44 ansible_become_pass="{{worker88_197_53_44_become_pass}}"
worker88_197_53_44 ansible_ssh_pass="{{worker88_197_53_44_ssh_pass}}"


[worker88_197_53_100]
worker88_197_53_100 ansible_host=88.197.53.100
worker88_197_53_100 hostname=thanasis1
worker88_197_53_100 data_path=/home/exareme/data/

worker88_197_53_100 remote_user="{{worker88_197_53_100_remote_user}}"
worker88_197_53_100 become_user="{{worker88_197_53_100_become_user}}"
worker88_197_53_100 ansible_become_pass="{{worker88_197_53_100_become_pass}}"
worker88_197_53_100 ansible_ssh_pass="{{worker88_197_53_100_ssh_pass}}"
```
[You can find the hostname of any machine by executing ```hostname``` in terminal]

[Requirement1: Mind that the variable ```data_path``` is the path where pathology folders are stored in your Target machine (Each Pathology folder includes the Data CSV (datasets.csv) and the Metadata file (CDEsMetadata.json))]<br/>
[Requirement2: Mind that the variable ```home_path``` is the path where ```Federated-Deployment/Compose-Files/``` will be stored in the master node. Compose-Files
contains 2 docker-compose.yaml files for deploying the services. The ```home_path``` can be Any path]

You can see that there are 2 main categories in hosts.ini file. The first one is ```[master]```, the second one is ```[workers]```.

You can always add more workers following the template given above: </br>
a) by adding the name workerX of the worker under [workers] and </br>
b) creating a tag [workerX] with all the necessary variables. </br>
X in the name ```workerX``` is a convention of the IP of the node where ```.``` are replaced with ```_```. For example:

```
   worker88_197_53_101 ansible_host=Your_Remote_Machine_Host
   worker88_197_53_101 hostname=Your_Remote_Machine_Hostname
   worker88_197_53_101 data_path=Your_Remote_Data_Path_where_CSV_data_and_CDEsMetadata_are_stored

   worker88_197_53_101 remote_user="{{worker88_197_53_101_remote_user}}"
   worker88_197_53_101 become_user="{{worker88_197_53_101_become_user}}"
   worker88_197_53_101 ansible_become_pass="{{worker88_197_53_101_become_pass}}"
   worker88_197_53_101 ansible_ssh_pass="{{worker88_197_53_101_ssh_pass}}"
```

For consistency reasons we suggest you keep the names of the workers following the workerX convention as described above.

## [Optional] Ansible-vault

```Just like the previous step this one can be done through the deploy script, too. If you have many nodes though it is easier to do it manually.```

As you can also see in hosts.ini file we have some sensitive data like usernames and passwords (credentials) in both master and workers. These lines ```MUST not be changed!```.

```
   master remote_user="{{master_remote_user}}"
   master become_user="{{master_become_user}}"
   master ansible_become_pass="{{master_become_pass}}"
   master ansible_ssh_pass="{{master_ssh_pass}}"
   
   .......
   
   worker88_197_53_44 remote_user="{{worker88_197_53_44_remote_user}}"
   worker88_197_53_44 become_user="{{worker88_197_53_44_become_user}}"
   worker88_197_53_44 ansible_become_pass="{{worker88_197_53_44_become_pass}}"
   worker88_197_53_44 ansible_ssh_pass="{{worker88_197_53_44_ssh_pass}}"
   
   ......
   
   worker88_197_53_100 remote_user="{{worker88_197_53_100_remote_user}}"
   worker88_197_53_100 become_user="{{worker88_197_53_100_become_user}}"
   worker88_197_53_100 ansible_become_pass="{{worker88_197_53_100_become_pass}}"
   worker88_197_53_100 ansible_ssh_pass="{{worker88_197_53_100_ssh_pass}}"
```

It is not a valid technique to just fill in your sensitive data (credentials) there, so we will use ```Ansible-Vault```.
Ansible-vault comes with the installation of ansible. Make sure you have it installed by running: ```ansible-vault --version```

With ansible-vault we can have an encrypted file which will contain sensitive information (credentials) like the ones shown above.

In order to create the file you need to run 
```ansible-vault create vault_file.yaml``` inside ```Federated-Deployment/Docker-Ansible/``` folder.
It will ask for a vault-password that you will need to enter it each time you run a playbook. So keep it in mind.

Here you will add
```
# remote_user and ssh_pass will be user to login to the target hostname
# become_user and become_pass will be used to execute docker and other commands. Make sure that user has permission to run docker commands. You could use root if possible.

master_remote_user: your_username
master_become_user: your_username
master_ssh_pass: your_password
master_become_pass: your_password
   
worker88_197_53_44_remote_user: your_username
worker88_197_53_44_become_user: your_username
worker88_197_53_44_ssh_pass: your_password
worker88_197_53_44_become_pass: your_password
   
worker88_197_53_100_remote_user: your_username
worker88_197_53_100_become_user: your_username
worker88_197_53_100_ssh_pass: your_password
worker88_197_53_100_become_pass: your_password
```
all in plaintext. If you have more than 2 workers, you will add those too by adding ```workerX_...``` in front of each variable where X is the IP of the node with ```.``` replaced by ```_```.<br/>
[Keep in mind that your password can be anything you want But ansible has a special character for comments ```#``` . If your password contains that specific character ansible will take the characters next to it as comments.]<br/>
When you exit you can see that vault_file.yaml is encrypted with all your sensitive information (credentials) in there.

If you want to edit the file you can do so whenever by running:
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

# Deployment by Hospital

In case when a Hospital ```can not/will not``` give the sensitive data like usernames and passwords (credentials) needed in order for Ansible to run, here is a workaround:

1) The Hospital must contact the system administrator so he/she will handle the command needed in order for Hospital to be part of the Swarm as ```Worker node```.
2) The Hospital must run the command ```docker swarm join --token <Swarm Token> <Master Node URL>:2377``` given by the system administrator.

For example the command could look like this:

```(sudo) docker swarm join --token SWMTKN-1-22ya4cjf2c1aq4sbnypwkvs2z87wg2897xi35qvp1hs54s85of-doah1kp92psb8rqvbgshu7ro2 88.197.53.38:2377```

If a node is behind a NAT, its local IP will be advertised by default and other nodes will not be able to contact it.<br/>
**Note:** Non-static public IP should be fine for worker nodes, but this has not been tested.

If the worker node is behind a NAT server, you must specify the Public IP to use to contact that Worker node from other nodes with:

 ```docker swarm join --advertise-addr <Public IP> --token <Swarm Token> <Master Node URL>:2377```

3) Inform the system administrator that the command run, so he/she can Start Exareme instance at the specific Hospital from the ```Manager node``` of Swarm.

# Deployment

Under Docker-Ansible/scripts/ folder run the ```deploy.sh``` to start the deployment.

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

If you want to exclude Portainer service from running, you need to add ```--skip-tags portainer``` in the command, meaning:

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

After inserting the nodes information in the hosts.ini and the ansible-vault file (under folder /Docker-Ansible/scripts/)
you can run the ```deploy.sh``` script.

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
Fill in the ```Username```, ```Password```, ```Confirm Password``` fields and click ```Create user```.<br/> 
[Mind that we create a folder called ```portainer``` in your ```home_path``` where the credentials you provided will be saved for the next times, until you delete the folder]<br/> 
The next page ```Connect Portainer to the Docker environment you want to manage.``` will ask you to Connect Portainer to an Environment. Click the first option  ```Local``` and ```Connect```.<br/> 
After that, you should click on your Local Swarm and navigate from the left menu. 
Go to your ```Services``` to check each service's logs and see if everything is running properly.

### Troubleshooting

a) If Portainer service is launched:

Under ```Services``` in the left menu check that all services has 1 replicas: ```replicated 1 / 1```.<br/>

If this is not the case, meaning you get ```replicated 0 / 1```, the service for the specific node did not run:

    From the specific node:

    1) Make sure you have enough space on the specific node

    From the Manager node of Swarm:

    1) Check the ERROR message by doing ```sudo docker service ps --no-trunc NAME_or_ID_of_service``` 
    2) If Worker node ```Manually``` joined the Swarm:
        - Make sure the node has actually joined the Swarm and that it is tagged with a proper name.


b) Check that all workers are seen by Exareme

Connect to the Exareme test page at `http://localhost:9090/exa-view/index.html`.
The algorithm LIST\_DATASET should show datasets for each pathology available at each node of the Federation.

If a node's information is missing:

    From the specific node of Swarm:

    1) Check the network configuration for this node. ```Obtaining the correct network configuration for each server that must join the Federation might not be straightforward.```
    2) Make sure the datasets and the CDEs are in the correct data path under ```pathology``` folder.
    
    From the Manager node of Swarm:

    1) Check the logs and report them to Exareme expertise team.
    
c) Restart Exareme

In some cases a simple restart may be enough. From the Manager node of Swarm under exareme/Federated-Deployment/Docker-Ansible/scripts/ folder run:
```
./deploy.sh
```

choose:
```
2. (Re)Start all services.
```

and then choose one of the options given:
```
1. Restart Exareme
2. Restart Portainer
3. Restart Exareme and Portainer
```
