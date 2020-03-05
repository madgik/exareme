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

Make sure you have read the <a href="./Documentation/Federation_Specifications.md">Federation_Specifications.md</a>
that sums up everything regarding Docker Swarm and the <a href="./Documentation/Firewall_Configuration.md">Firewall_Configuration.md</a>
that sums up how to deal if a firewall exists in the Federation nodes.

# Preparation

## Data Structure
In every Target node there should be a *DATA FOLDER* which contains the *DATA* existing in that specific Target node.
We will refer to the path leading to *DATA FOLDER* as ```data_path```. The ```data_path``` can be different across the Target nodes.

In every node the *DATA FOLDER* should follow a specific structure.
The *DATA FOLDER* should contain one folder for each pathology that it has datasets for. Inside that folder there should be:
1) the .csv files that will contain one or more datasets, 
2) the CDEsMetadata.json file for that specific pathology.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> adni.csv <br />
----------> ppmi.csv <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />

On the Target node where the Exareme master node will be running, we *must include* the CDEsMetadata.json file for *every pathology* that exists even if it *doesn't contain* any .csv file.
For example on the Target node where Exareme master node will be running:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />

For more information on what these files should contain you can see <a href="../Documentation/InputRequirements.md#input-requirements">here</a>.

## Deployment

Under *Docker-Ansible/scripts/* folder run the ```deploy.sh``` to start the deployment.
You will be prompted to provide any more information needed.

Some of the steps provided in the script above can be also done manually:<br/>
<a href="./Documentation/Optionals.md#optional-initialize-exareme-version">Initialize Exareme Version</a><br/>
<a href="./Documentation/Optionals.md#optional-initialize-hosts">Initialize Hosts</a><br/>
<a href="./Documentation/Optionals.md#optional-ansible-vault">Ansible-vault</a><br/>
<a href="../Documentation/SecurePortainer.md#optional-secure-portainer">Secure Portainer</a><br/>
Information about how to <a href="./Documentation/Troubleshoot.md#portainer">launch Portainer for the first time.</a>

## Deployment by Hospital

You can navigate <a href="./Documentation/DeployByHospital.md#deployment-by-hospital">here</a> for a work around in case a Hospital *need* to deploy at each own (authorization restrictions for example).

## Deployment [Manual]

You can also deploy everything manually by following the instructions from <a href="./Documentation/ManualDeployment.md#swarm-initialization">here</a><br/>

# Test that everything is up and running

If all went well, everything should be deployed! Some insights <a href="./Documentation/Troubleshoot.md#troubleshooting">here</a> to check if everything is deployed correctly.
