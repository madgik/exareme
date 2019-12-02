# Local Exareme Deployment Guide

Here you will find all the information needed in order to deploy Exareme in your environment via docker compose and shell scripts.

# Requirement

1) Install Docker in your machine.

# Ports

Make sure the following ports are available:

```9090: for accessing Exareme```

```(Optional): 8500 for accessing Consul Key Value Store``` 

```(Optional): 9000 for accessing Portainer.io``` 

# Preparation

## Clone the repository 

Clone this repository on your local computer so you can use it to deploy exareme.

## Data Structure
In every node the DATA should follow a specific structure. We will refer to the path of the DATA folder as ```data_path```. The ```data_path``` can be different across the nodes.

The data folder should contain one folder for each pathology that it has datasets for. Inside that folder there should be:
1) the datasets.csv file with all the datasets combined and
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

The master node should have the CDEsMetadata.json for every pathology even if it doesn't contain a datasets.csv file.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />


## [Optional] Exareme Version 

In the ```Local-Deployment/``` folder create an ```exareme.yaml``` file.

The file should contain the following lines, modify them depending on the version of exareme you want to deploy.

```
EXAREME_IMAGE: "hbpmip/exareme"
EXAREME_TAG: "v21.0.0"
```

## [Optional] Data path location

In the ```Local-Deployment/``` folder create an ```dataPath.txt``` file.

The file should contain the following line, modify it according to the place where your data are.

```
LOCAL_DATA_FOLDER=/home/user/data/

```

# Deployment

In the ```Local-Deployment/``` folder, run the ```deployLocal.sh``` to start the deployment.

You will be prompted to provide any information needed.

# Troubleshooting

While ```sudo docker service ls```, if the services are Replicated 0/1:

1) Check that you have enough space in your machine.

2) If there is an ERROR, try ```sudo docker service ps --no-trunc NAME_or_ID_of_service``` to see the whole message.
