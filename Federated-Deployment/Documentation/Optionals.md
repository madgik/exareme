# [Optional] Initialize Exareme Version

```This step can be done through the deploy script.```

If you want to do it manually you can go to the ```Federated-Deployment/Docker-Ansible/group_vars``` folder and create an ```exareme.yaml``` file.

The file should contain the following lines, modify them depending on the version of Exareme you want to deploy.

```
EXAREME_IMAGE: "hbpmip/exareme"
EXAREME_TAG: "v21.2.0"
```

# [Optional] Initialize Hosts

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

[Requirement2: Mind that the variable ```home_path``` is the path where ```Federated-Deployment/Compose-Files/``` will be copied and stored in the master node. Compose-Files contains 2 docker-compose.yaml files for deploying the services.
The ```home_path``` can be Any path in which become_user has permissions. If the path does not exist in the master node,then any sub-folders are created automatically during the copy.]<br/>

You can see that there are 2 main categories in hosts.ini file. The first one is ```[master]```, the second one is ```[workers]```. <br/>
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

# [Optional] Ansible-vault

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
```ansible-vault create vault.yaml``` inside ```Federated-Deployment/Docker-Ansible/``` folder.
It will ask for a vault-password that you will need to enter it each time you run a playbook. So keep it in mind.

Here you will add
```
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
all in plaintext where:<br/>
a)remote_user and ssh_pass will be user to login to the target hostname<br/>
b)become_user and become_pass will be used to execute docker and other commands. Make sure that become_user has permission to run docker commands (add user to docker group in the target machines).You could use root for become_user if possible.
<br/><br/>
Keep in mind that you must be able to ```ssh remote_user@ansible_host``` in *all target machines* by only providing the ```ssh_pass``` as a password.If this is not the case, consider to change your ssh configurations.

If you have more than 2 workers, you will add those too by adding ```workerX_...``` in front of each variable where X is the IP of the node with ```.``` replaced by ```_```.<br/>
[Keep in mind that your password can be anything you want But ansible has a special character for comments ```#``` . If your password contains that specific character ansible will take the characters next to it as comments.]<br/>
When you exit you can see that vault.yaml is encrypted with all your sensitive information (credentials) in there.

If you want to edit the file you can do so whenever by running:
```ansible-vault edit vault.yaml```
Place your vault password and edit the file.

## [Optional] Regarding Ansible-vault password.
(source https://docs.ansible.com/ansible/latest/user_guide/playbooks_vault.html)

As mentioned before, each time you run a playbook you will need to enter your password.

Alternatively, ansible-vault password can be specified with a file ```~/.vault_pass.txt``` or a script (the script version will require Ansible 1.7 or later). When using this flag, ensure permissions on the file are such that no one else can access your key and do not add your key to source control:
examples:

```ansible-playbook site.yml --vault-password-file ~/.vault_pass.txt```

```ansible-playbook site.yml --vault-password-file ~/.vault_pass.py```

The password should be a string stored as a single line in the file.

If you are using a script instead of a flat file, ensure that it is marked as executable, and that the password is printed to standard output. If your script needs to prompt for data, prompts can be sent to standard error.

More guidance will be provided in that matter if you select to deploy via script (see below)

# [Optional] Secure Portainer

By default, Portainer’s web interface and API are exposed over HTTP. If you want them to be exposed over HTTPS check
<a href="https://github.com/madgik/exareme/tree/master/Documentation/SecurePortainer.md">here</a>.<br />

