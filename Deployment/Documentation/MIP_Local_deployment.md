# MIP Local deployment and documentation

This document summarises the knowledge of DIAS-EPFL regarding the deployment and upgrade process of MIP Local. It is based on the version 2.5.3 released on Dec 14, 2017.

**Disclaimer:** The authors of this document are not in charge of the MIP development and its deployment scripts. They have limited knowledge of most of the elements that are deployed. No guaranties are offered as to the correctness of this document.

See also the official documentation of the deployment scripts project on Github: [README](https://github.com/HBPMedical/mip-microservices-infrastructure/blob/master/README.md) file, [installation instructions](https://github.com/HBPMedical/mip-microservices-infrastructure/blob/master/docs/installation/mip-local.md) and some [more documentation](https://github.com/HBPMedical/mip-microservices-infrastructure/blob/master/docs).

See also a [simpler deployment procedure](https://github.com/HBPMedical/mip-local) for MIP Local without the Data Factory, using Docker compose.

## Contents

- [Introduction](#introduction)
- [Requirements](#requirements)
- [Security warning](#security-warning)
- [Network configuration](#network-configuration)
- [User management](#user-management)
- [Known limitations](#known-limitations)
- [Deployment steps](#deployment-steps)
- [Deployment validation](#deployment-validation)
- [Direct access to the deployed databases](#direct-access-to-the-deployed-databases)
- [Reboot](#reboot)
- [Upgrades](#upgrades)
- [Adding clinical data](#adding-clinical-data)
- [Cleanup MIP installation](#cleanup-mip-installation)
- [Troubleshooting](#troubleshooting)

## Introduction

The MIP (Medical Informatics Platform) is a bundle of software developed by the HBP sub-project SP8.
Its goal is to enable research and studies on neurological medical data, locally at one hospital and in a federated manner across hospitals, while maintaining the privacy of sensitive data. For more information, please refer to "SP8 Medical Informatics Platform – Architecture and Deployment Plan" (filename `SGA1_D8.6.1_FINAL_Resubmission`).

The MIP is composed of four main parts:

- Web Portal (interface: metadata about the available data, functionalities for privacy-preserving exploration and analysis of the data).
- Structural software (aka "Hospital Bundle": anonymisation, data harmonisation, query engine, federated query engine).
- Data Factory (extraction of features from medical imaging data).
- Algorithm Factory (library of research algorithms that can be run in the MIP).

It is populated with:

- The research datasets PPMI, ADNI and EDSD.
- Local clinical datasets, once prepared and processed.

The MIP can be deployed using the scripts available in the [mip-microservices-infrastructure](https://github.com/HBPMedical/mip-microservices-infrastructure) project on Github.

The software is organised into "building blocks" that should facilitate the deployment of the MIP on two or three servers, in an infrastructure that improves security in order to guaranty data privacy.

Based on the [Ansible inventory file](https://github.com/HBPMedical/mip-microservices-infrastructure/blob/master/roles/mip-local/templates/hosts.j2), the building blocks are the following:

- infrastructure
- hospital-database
- reference
- data-factory
- algorithm-factory
- web-analytics

This file lists the building blocks that will be installed. In theory, it can be modified before running `./setup.sh` to install only specific block (this has not been tested).


## Requirements

- Ubuntu 16.04 system (partial support for RHEL).
- Matlab R2016b. (Required for the Data Factory. Alternatively the MIP can be installed without the Data Factory: see below the corresponding deployment option.)
- Fixed IP address and possibly a DNS alias to simplify the access to the Web Portal.
- According to the official documentation, python version 2.7 and (in some cases at least) the library `jmespath` need to be installed beforehand.
    For ubuntu:

	```sh
	sudo apt install python2.7
	ln -s /usr/bin/python2.7 /usr/bin/python
	sudo apt install python-jmespath
	```


## Security warning

The official MIP Local documentation includes the following statement:
> Security: it is the responsibility of the hosting party to secure access to MIP at the network level.

As of 29.03.2018, a standard MIP Local deployment will expose several ports giving access to the database services (password protected) and administration tools (not password protected). Using a firewall to prevent outside access to these services is highly recommended. However, using a firewall on the server itself requires the correct configuration to not break functionalities; as of end of March 2018 the relevant configuration is not known.

All information available to the DIAS-EPFL team on this subject is provided in the next section. DIAS-EPFL is not responsible for the security of the platform: please contact the development team for further questions.


## Network configuration


### Internet access for deployment

Access to the following internet domains is required during the deployment (the list might not be exhaustive):

- amazonaws.com
- fr.archive.ubuntu.com
- archive.ubuntu.com
- security.ubuntu.com
- launchpad.net
- hub.docker.com
- download.docker.com
- docker.io
- repos.mesosphere.com
- pypi.python.org
- github.com
- bitbucket.org
- gitlab.com
- cloudfront.net
- keyserver.ubuntu.com
- services.humanbrainproject.eu
- hbps1.chuv.ch

If internet access is limited, make sure to allow connections to these domains.


### Operational firewall configuration

The firewall in front of the MIP server must be set up and deny all incoming connections, except on the following ports:

- 22 for ssh access
- 80 for Web Portal access
- MIP Local requirements
- Federation requirements (see Federation documentation)
- User management requirements: access to [services.humanbrainproject.eu](services.humanbrainproject.eu)



### MIP Local requirements

Some ports must be open for intra-server connections (accept only requests coming from the local server itself, from localhost and from its public address):

- 31432 ("LDSM", PostgresRAW database)
- 31433 (Postgres "analytics-db")
- 31555 (PostgresRAW-UI)

**TODO:**

- Get list of ports to open for MIP-Local from development team.
- Test configuration of firewall.
- Determine which ports are only needed locally.

Until the list can be completed, the only stable option is to run MIP Local with **no firewall enabled on the server**.


## User management

The Web Portal of MIP Local can be deployed in two settings:

- No user management: anybody who has access to the port 80 of the MIP Local server can access the Web Portal and all the data available in the MIP. This can either be
	- Everybody that has access to the local network, if the firewall is open.
	- Only users who have access to the server itself, if the firewall prevents external access.
- User authentification required: every user must obtain credentials to access the Web Portal. In this case, user rights and authentification are managed by the main HBP servers, so network access to these servers must be allowed ([services.humanbrainproject.eu](http://services.humanbrainproject.eu) domain).

Further information:

[//]: # ( from Jacek Manthey to Lille)

[... Users] can create accounts on the HBP Portal (see https://mip.humanbrainproject.eu/intro) through invitation, which means that the access control is not stringent.
[... Only] users that can access [the local] network and have an HBP account would be able to access MIP Local. In case you would need more stringent access control, we would need to implement in your MIP-Local a whitelist of authorized HBP accounts.  

In order to activate the user access using the authentication through the HBP Portal, we would need a private DNS alias for your MIP local machine, something like ‘mip.your\_domain\_name’. [...]

## Known limitations

The following are known limitations of the deployment scripts, version 2.5.3.

- It is currently not possible to deploy MIP Local with a firewall enabled. MIP Local cannot run either with the firewall up, unless the correct rules are configured (see [MIP Local requirements](#mip-local-requirements)). 

- The deployed MIP will include research datasets (PPMI, ADNI and EDSD), but the process to include hospital data in MIP-Local is as yet unclear.

Note: Clinical data processed and made available in the Local Data Store Mirror (LDSM) will not be visible from the Local Web Portal without further configuration, but they will be available to the Federation if the node is connected (variables included in the CDE only).


## Deployment steps

This section describes how to deploy MIP Local without clinical data, on a clean server. If a previous installation was attempted, please see [Cleanup MIP installation](#cleanup-mip-installation). To add hospital data see the section [Adding clinical data](#adding-clinical-data).

At the time of writing (25.01.2018), the <a href="https://github.com/HBPMedical/mip-microservices-infrastructure/blob/master/docs/installation/mip-local.md">official installation doc</a> contains several errors.

1. Retrieve informations required for the deployment:

	- Matlab installation folder path,
	- server's address on the local network,
	- credentials for the gitlab repository, to download the research data sets,
	- sudo access to the target server.

2. Clone the version 2.5.3 from the `mip-microservices-infrastructure` git repo in the desired location (here a `mip-infra` folder) and cherry-pick two bug fixes:

	```sh
	git clone --origin mmsi --branch stable https://github.com/HBPMedical/mip-microservices-infrastructure.git mip-infra
	cd mip-infra/
	git cherry-pick f877ddd3e2b6bf937fe6d31f89cd281f81218da3
	git cherry-pick 44788978c98be4828164eee64024b6a32497c7bf
	```
	This will clone the version 2.5.3; replace "2.5.3" by another tag or the "stable" branch as needed.
	This command will also name the remote repository mmsi.
	
	You might want to create a local branch named master, in order to upload the configuration later (optional):
	
	```
	git checkout -b master
	```
	
	Running the following scripts might be needed after cloning or updating the repository, but their use is not documented and it does not seem necessary on a fresh clone:
	
	```
	./after-git-clone.sh
	./after-update.sh
	```
	

3. The versions used for each software can be found and modified in `vi vars/versions.yml`. In particular, use the latest stable versions for the LDSM:
    
    ```
    ldsm_db_version: 'v1.4.1'
    postgresraw_ui_version: 'v1.5'
    ```
    
    And apply one more patch to adapt to PostgresRAW-UI version 1.4 or higher:
    
    ```
    git cherry-pick 86b0787ba11b21c4dc9a0fdf64d9bb8bea05d404
    ```

4. Run the configuration script:

	```
	./common/scripts/configure-mip-local.sh
	```

	Provide the requested parameters.

	Summary of requested input (and tested parameters):

	```
	Where will you install MIP Local?
	1) This machine
	2) A remote server
	> 1
	
	Does sudo on this machine requires a password?
	1) yes
	2) no
	> 1
	
	>Which components of MIP Local do you want to install?
	1) All				     3) Data Factory only
	2) Web analytics and databases only
	> 1
	
	Do you want to store research-grade data in CSV files or in a relational database?
	1) CSV files
	2) Relational database
	> 1
	```
	**WARNING:** Both options load the research data (ADNI, PPMI and EDSD) in a relational database. The first option will upload the data in the LDSM database using PostgresRAW, and the second in an unofficial postgres database named "research-db". **Choose 1 to deploy the official MIP Local and have the option to join the Federation.**
	
	```
	Please enter an id for the main dataset to process, e.g. 'demo' and a 
	readable label for it, e.g. 'Demo data'
	Id for the main dataset > demo
	Label for the main dataset > Demo data
	
	Is Matlab 2016b installed on this machine?
	1) yes
	2) no
	> 1
	
	[If "yes":]
	Enter the root of Matlab installation, e.g. /opt/MATLAB/2016b :
	path >
	
	Do you want to send progress and alerts on data processing to a Slack channel?
	1) yes
	2) no
   > 2
	
	Do you want to secure access to the local MIP Web portal?
	1) yes
	2) no
	> 2
	
	To enable Google analytics, please enter the Google tracker ID or leave this blank to disable it
	Google tracker ID > 
	```
	
	
	```
	TASK [Suggested target server hostname]***********************
	ok: [localhost] => {
		"ansible_hostname": "suggested_ansible_hostname"
	}
	
	TASK [Suggested target server FQDN]***************************
	ok: [localhost] => {
		"ansible_fqdn": "suggested_ansible_fqdn"
	}
	
	TASK [Suggested target server IP address]***********************
	ok: [localhost] => {
		"msg": "suggested_IP_address"
	}
	
	Target server hostname, e.g. myserver . Use ansible_hostname value if you agree with it. 
	```
	Apparently, using another hostname than the current one will modify the machine's hostname.
	
	```
	Target server FQDN, e.g. myserver.myorg.com .If the full server name cannot be reached 
	by DNS (ping myserver.myorg.com fails), you can use the IP address instead:
	```
	
	If unsure that the `suggested_ansible_fqdn` given above is valid, use the `suggested_IP_address` instead. (Or check if ping works on the `suggested_ansible_fqdn` from another computer.)
	
	
	```
	Target server IP address:
	```
	Use the suggested `suggested_IP_address` given above.
	
	```
	Base URL for the frontend, for example http://myserver.myorg.com:7000
	```
	
	This is the address the WebPortal will be accessed through.
	The server's address must be valid on the local network (check with nslookup).
	The port must be open.
	
	```
	Username on Gitlab to download private Docker images. 
	Leave blank if you do not have access to this information:
	
	Password on Gitlab to download private Docker images. 
	Leave blank if you do not have access to this information:
	
	```
	
	Provide a Gitlab access to download the research data docker images.
	
	```
	Use research data only? (Y/n): Y
	```
	
	Using only the research data ("Y") should lead directly to a working MIP Local, accessing research data in a table name `mip_cde_features`. 
	
	Using also hospital data (i.e. answering "n") requires additional (uncertain) steps: see section [Adding clinical data](#adding-clinical-data). 
	
	In this case, MIP Local will use the view named `mip_local_features` to access data. This view groups the research and the clinical data in a uniform flat schema. It is automatically created when hospital data, in the form of a csv file name `harmonized_clinical_data.csv`, is dropped in the `/data/ldsm` folder of the MIP Local server. (See [PostgresRAW-UI documentation](https://github.com/HBPMedical/PostgresRAW-UI/blob/master/README.md#3-automated-mip-view-creation) for details.)
	
	
	```
	Generate the PGP key for this user...
	[details]
	Please select what kind of key you want:
	 (1) RSA and RSA (default)
	 (2) DSA and Elgamal
	 (3) DSA (sign only)
	 (4) RSA (sign only)
	Your selection?
	
	RSA keys may be between 1024 and 4096 bits long.
	What keysize do you want? (2048) 
	
	Please specify how long the key should be valid.
	         0 = key does not expire
	      <n>  = key expires in n days
	      <n>w = key expires in n weeks
	      <n>m = key expires in n months
	      <n>y = key expires in n years
	Key is valid for? (0) 
	
	Is this correct? (y/N)
	```
	Just type "enter" to use the default values (and confirm with "y").
	
	```
	You need a user ID to identify your key; the software constructs the user ID
	from the Real Name, Comment and Email Address in this form:
	    "Heinrich Heine (Der Dichter) <heinrichh@duesseldorf.de>"
	
	Real name:
	
	Email address:
	
	Comment: 
	
	You selected this USER-ID:
	    [...]
	
	Change (N)ame, (C)omment, (E)mail or (O)kay/(Q)uit?
	
	
	You need a Passphrase to protect your secret key.
	
	Enter passphrase: 
	                  
	Repeat passphrase: 
	
	```
	
	This information is used by git-crypt to encrypt in the Git repository the sensitive information. This precaution is taken if the configuration is uploaded (pushed) to a different server. The passphrase might be needed for those steps: keep it secure somewhere.

5. Once the configuration script ends successfully with a message "Generation of the standard configuration for MIP Local complete!", commit the modifications before continuing.
	
	```sh
	git add .
	git commit -m "Configuration for MIP Local"
	```

6. Run the setup script, twice if required, or more if errors are encountered. Re-running the script might solve some problems.

	```sh
	./setup.sh
	```
	
	The script should end with the following message:
	
	```
	PLAY RECAP *************************************************************************************
	localhost                  : ok=??   changed=??   unreachable=0    failed=0   
	```

## Uploading the configuration on bitbucket

For production maintenance, the dedicated MIP team request that the configuration be uploaded on bitbucket. The configuration script should enable the encryption of sensitive information before the upload is done. 

The secure key generated during the configuration phase (which requests the passphrase to be used), seems to be stored under `mip-local/.git-crypt/keys/default/0/`. It should be automatically found for the next steps, but make sure it is not encrypted if you want to encrypt the full configuration folder.

The most important file to encrypt is `mip-local/envs/mip-local/etc/ansible/host_vars/localhost`. You can make sure it will be encrypted by running the following command:

```sh
git-crypt status | grep -v ^not
    encrypted: envs/mip-local/etc/ansible/host_vars/localhost
```

To give access to the encrypted configuration to the maintenance team, the public gpg key of a member of the team must be obtained and copied to the server. The member can then be authorised following these steps:

```sh
gpg --import <path>/<key-name.key>
gpg --sign-key the-public-key-id # This id is given by the previous command under "gpg: key xxxxxxxx"
git-crypt add-gpg-user the-public-key-id
```

The existing keys can be listed with:

```sh
gpg --list-secret-keys
```

Create a local branch "master" or another name, depending on which branch you want to push the configuration.

```sh
cd mip-local
git checkout -b master
```

Set remote "origin" to a bitbucket repository where you will upload the config

```sh
git remote add origin https://<username>@bitbucket.org/hbpmip_private/<instance-name>-infrastructure.git
```

It is also possible to use an ssh connection, but this requires an ssh key registered on the repository and a network configuration allowing ssh access to bitbucket. In that case, use the following remote repository:

```sh
git remote add origin git@bitbucket.org:hbpmip_private/<instance-name>-infrastructure.git
```


## Deployment validation

If the deployment was successful, the Web Portal should be accessible on the `target server IP address` defined at the configuration step. The Marathon interface allows to check the status of the MIP Local services; it is accessible through a web browser on port 5080.

The Web Portal documentation [HBP\_SP8\_UserGuide\_latest.pdf](https://hbpmedical.github.io/documentation/HBP_SP8_UserGuide_latest.pdf) can help check that the deployed MIP Local is running as expected. The Web Portal should provide similar results but not exactly the results shown in the doc.

The validation performed at the end of [this report](https://drive.google.com/file/d/136RcsLOSECm4ZoLJSORpeM3RLaUdCTVe/view) of a successful deployment is the official way to check that MIP Local is behaving correctly.

The PostgresRAW-UI can be validated following this <a href="https://drive.google.com/open?id=0B5oCNGEe0yovNWU5eW5LYTAtbWs">test protocol</a>. PostgresRAW-UI should be accessible locally at `http://localhost:31555`; it requires LDSM credentials to access the local data (see next section).



## Direct access to the deployed databases

The ports and credentials to access the databases used in the MIP can be found in these files:

```sh
cat install_dir/envs/mip-local/etc/ansible/host_vars/localhost
cat install_dir/vars/hospital-database/endpoints.yml
cat install_dir/vars/reference/endpoints.yml
```

Adapt this command to connect to the databases:

```sh
psql -U ldsm -p 31432 -h hostname
```


## Reboot

The MIP is not automatically restarted if the server is shut down or rebooted. 

The last instructions provided to restart it are:

[//]: # (Slack, MIP-Local & IAAN workspace, general channel, 06.12.2017)

```sh
./common/scripts/fix-mesos-cluster.sh --reset
./setup.sh
```

The following error might appear:

```
TASK [marathon : Install Marathon package] ***********************************************************************************************************
fatal: [localhost]: FAILED! => {"cache_update_time": 1520419443, "cache_updated": false, "changed": false, "failed": true, "msg": "'/usr/bin/apt-get -y -o \"Dpkg::Options::=--force-confdef\" -o \"Dpkg::Options::=--force-confold\"     install 'marathon=1.5.2' -o APT::Install-Recommends=no' failed: E: Packages were downgraded and -y was used without --allow-downgrades.\n", "stderr": "E: Packages were downgraded and -y was used without --allow-downgrades.\n", "stderr_lines": ["E: Packages were downgraded and -y was used without --allow-downgrades."], "stdout": "Reading package lists...\nBuilding dependency tree...\nReading state information...\nThe following packages will be DOWNGRADED:\n  marathon\n0 upgraded, 0 newly installed, 1 downgraded, 0 to remove and 101 not upgraded.\n", "stdout_lines": ["Reading package lists...", "Building dependency tree...", "Reading state information...", "The following packages will be DOWNGRADED:", "  marathon", "0 upgraded, 0 newly installed, 1 downgraded, 0 to remove and 101 not upgraded."]}
```

It can be solved using 

```
sudo apt install -y --allow-downgrades --allow-change-held-packages marathon=1.5.2
```


Before an updated version of the installer can be provided, it might be necessary to:
> stop all services, uninstall mesos, marathon and docker-ce, then run the installer again.


## Upgrades


> When you perform an upgrade, in most cases you will not need to run again the pre-configuration script mip-local-configuration.sh.
> 
> In the few cases where that is necessary, for example if you want to install a new component such as the Data Factory or there has been a big update that affects configuration, then you need to be careful about the changes that this script brings to the configuration. For example, passwords are always re-generated. But the passwords for the existing databases should not be modified. To counter that, you can use Git features and do a review on all changes, line by line, and commit only the changes that are actually needed. 


**TODO: Clarify procedure. How to guess which changes are needed? Revert at least the changes to `install_dir/envs/mip-local/etc/ansible/host_vars/` or to file `localhost` in particular?**

If the update is performed by cleaning the current install and re-deploying, make sure to backup the clinical data, normally stored in `/data/ldsm/harmonized_clinical_data.csv`. 

The list of other elements to backup is not known. **TODO: Obtain this list.**


## Adding clinical data

Clinical data must be processed and harmonised so that the variables corresponding to the MIP CDE (common data elements) have the MIP standard name and encoding. Additional hospital-specific variables can also be added.

The harmonised data must be exported to a CSV file name `harmonized_clinical_data.csv` and dropped in the `/data/ldsm` folder. PostgresRAW-UI will automatically detect the file, show it as a table in the PostgresRAW database and create the following views:

- `mip_local_features`: shows the research and the clinical data in one big table for the Local Web Portal and Woken's usage.
- `mip_federation_features`: shows only the clinical data fitting the CDE in a star schema based on the Federation software requirements.

More steps are required to enable Woken and the Local Web Portal so see and use the clinical data. It seems that the main requirement is to update the `meta` database, which contains an entry holding a json field describing all the available variables. The entry must be adapted to make sure that the `mip_local_features` view is used as source, and that all the variables available in the `harmonized_clinical_data.csv` file are described in the json field.

Modification to the `meta` database are not taken into account automatically. Restarting the following services from the Marathon interface (running on port 5080) might be sufficient (not tested):

- Web Portal backend + frontend
- Woken
- Data Factory


**TODO: This section needs to be checked, and properly documented. Only general information is available.**

Draft guidelines to add clinical data the official way (not tested):

[//]: # (Ludovic, technical meeting on January 9th, 2018; untested)


>   - Create a clone of gitlab project https://github.com/HBPMedical/mip-cde-meta-db-setup.
>   - Modify clm.patch.json so that it can modify the default variables.json file to add the relevant new variables.
>   - Adapt first line of Docker file to select / define the version / rename the Docker image, from hbpmip/mip-cde-meta-db-setup to something else (?)
>   - Create the docker image and push it to gitlab (?)
>   - Once the MIP-Local configuration for the deployment exist, modify (line 20 of) the file
>		   envs/mip-local/etc/ansible/group_vars/reference to reference the right docker image
>   - Run setup.sh so that the new docker image is run and copies the data in the meta-db database
>   - Restart all services of the following building blocks from Marathon (if necessary, scale them down to 0, then up again to 1)
>    - web portal
>    - woken
>    - data factory



## Cleanup MIP installation

Before attempting a second installation, in case a couple of updates have been delivered to your Linux distribution package manager, you will need to follow the next steps to ensure a proper deployment.

Please be advised this is drastic steps which will remove entirely several softwares, their configuration, as well as any and all data they might store.

### Ubuntu 16.04 LTS

 1. Purge installed infrastructure:

    ```sh
	$ sudo apt purge -y --allow-change-held-packages docker-ce marathon zookeeper mesos
    ```

 2. Remove all remaining configuration as it will prevent proper installation:

    ```sh
	$ sudo rm -rf /etc/marathon /etc/mip
	$ sudo reboot
	$ sudo rm -rf /etc/sysconfig/mesos-agent /etc/sysconfig/mesos-master /var/lib/mesos /var/lib/docker
	$ sudo rm -rf /etc/systemd/system/marathon.service.d
	$ sudo find /var /etc /usr -name \*marathon\* -delete
	$ sudo find /etc /usr /var -name \*mesos\* -delete
	$ sudo rm -rf /srv/docker/ldsmdb /srv/docker/research-db /srv/docker/woken /srv/docker/woken-validation
    ```

------
   **WARNING:**
   Backup your data before executing the command above. This will remove anything placed inside databases, as well as stored insides docker images.

------

3. Reload the system initialisation scripts, and reboot:

   ```sh
	$ sudo systemctl daemon-reload
	$ sudo reboot
   ```

4. Manually pre-install the packages. As this requires to specify precise version numbers, this list will be out of date really soon:

   ```sh
   $ sudo apt install -y --allow-downgrades --allow-change-held-packages docker-ce=17.09.0~ce-0~ubuntu
   ```

## Troubleshooting

- Running several times the `setup.sh` script might solve some issues: in case of error, run it one more time to check if it fails at the same step.

- In case the installation fails because a package (docker-ce, marathon, zookeeper or mesos) cannot be downgraded or installed because of previous installs, use the following commands (possibly purge only one package rather than all):

    ```sh
    $ sudo apt purge -y --allow-change-held-packages docker-ce marathon zookeeper mesos
    $ sudo apt install -y --allow-downgrades --allow-change-held-packages docker-ce=17.09.0~ce-0~ubuntu
    ```
- Purging Marathon might also fix the problem if the deployment gets stuck at:

    ```
    TASK [ldsm-database : wait for marathon] *******************************************
    FAILED - RETRYING: wait for marathon (3600 retries left).
    FAILED - RETRYING: wait for marathon (3599 retries left).
    ```


[//]: # (from Slack)


- > Zookeeper in an unstable state, cannot be restarted
>  
> -> ```/common/scripts/fix-mesos-cluster.sh --reset, then ./setup.sh ```


The documentation folder on Github contains a few specific fixes.
