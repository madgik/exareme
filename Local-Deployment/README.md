# Local Exareme Deployment Guide

Here you will find all the information needed in order to deploy Exareme in your environment via docker compose and shell scripts.

# Requirement

1) Install Docker in your machine.

2) Since the deployment of a local instance of Exareme involves the creation of a Docker Swarm, you need to make sure that the node has:

- Static public IP
- Network configuration:
  * TCP: ports 2377 and 7946 must be open and available
  * UDP: ports 4789 and 7946 must be open and available
  * IP protocol 50 (ESP) must be enabled

# Ports

Make sure the following ports are available:

```9090: for accessing Exareme```

```(Optional): 8500 for accessing Consul Key Value Store``` 

```(Optional): 9000 for accessing Portainer.io```

# Preparation

## Clone the repository 

Clone this repository on your local computer so you can use it to deploy Exareme.

## Data Structure
DATA should follow a specific structure. A data folder, which will be referred as  ```data_path```, should contain one
folder for each pathology that it has datasets for. Inside that folder there should be:

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

You should include the CDEsMetadata.json file for every pathology even if it doesn't contain a datasets.csv file.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> datasets.csv <br />
----------> CDEsMetadata.json <br />


## [Optional] Exareme Version 
```This step can be done through the deploy script.```

In the ```Local-Deployment/``` folder create an ```exareme.yaml``` file.

The file should contain the following lines, modify them depending on the version of Exareme you want to deploy.

```
EXAREME_IMAGE: "hbpmip/exareme"
EXAREME_TAG: "v21.2.0"
```

## [Optional] Data path location
```This step can be done through the deploy script.```

In the ```Local-Deployment/``` folder create a ```data_path.txt``` file.

The file should contain the following line, modify it according to the path where your data folder is.

```
LOCAL_DATA_FOLDER=/home/user/data/
```

# Deployment

In the ```Local-Deployment/``` folder, run the ```deployLocal.sh``` to start the deployment.

You will be prompted to provide any information needed.

If you wish to also deploy Portainer.io for observing service logs you need to secure Portainer using SSL.

Following the instructions from here: <br />
https://portainer.readthedocs.io/en/stable/deployment.html#secure-portainer-using-ssl <br />
you need to create an SSL certificate for your domain, if that has not already been done.

You can use the following commands to generate the required files with ```openssl```:

``` 
    openssl genrsa -out portainer.key 2048
    openssl ecparam -genkey -name secp384r1 -out portainer.key
    openssl req -new -x509 -sha256 -key portainer.key -out portainer.crt -days 3650
```

You can also use ```letsencrypt``` that provides free SSL certificates too.

For Ubuntu 18.04.3 LTS we **used and tested** ```letsencrypt``` for creating an SSL certificate by following the instruction from here: <br />
https://devanswers.co/lets-encrypt-ssl-apache-ubuntu-18-04/

Although we **ONLY** tested ```letsencrypt```, we give the two (2) options provided by the official documentation: <br /> https://portainer.readthedocs.io/en/stable/deployment.html#secure-portainer-using-ssl
along with the option of deploying Portainer.io *without* https for You to choose the way you want to deploy.

# Troubleshooting

While ```sudo docker service ls```, if the services are Replicated 0/1:

1) Check that you have enough space in your machine.

2) If there is an ERROR, try ```sudo docker service ps --no-trunc NAME_OR_ID_OF_SERVICE``` to see the whole message.
