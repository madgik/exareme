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
----------> adni.csv <br />
----------> ppmi.csv <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />

You should include the CDEsMetadata.json file for every pathology even if it doesn't contain a datasets.csv file.

For example:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />

For more information on what these files should contain you can see here:
https://github.com/madgik/exareme/Documentation/InputRequirements.md


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

## [Optional] Secure Portainer

As stated in the official Deployment Documentation<br />
(https://portainer.readthedocs.io/en/stable/deployment.html#secure-portainer-using-ssl) <br />
**"By default, Portainer’s web interface and API is exposed over HTTP. This is not secured, it’s recommended to enable SSL in a production environment."** <br />

To enable SSL, you need to create an SSL certificate. <br />

For Ubuntu 18.04.3 LTS we **used and tested** ```letsencrypt```. Following the instruction from here: <br />
https://devanswers.co/lets-encrypt-ssl-apache-ubuntu-18-04/ <br />
to generate an SSL certificate you need to:

1. Install Let’s Encrypt client (Certbot)
```sudo apt-get update && sudo apt-get install software-properties-common```
```sudo add-apt-repository universe && sudo add-apt-repository ppa:certbot/certbot```
```sudo apt-get update && sudo apt-get install certbot python-certbot-apache```

Press Enter or Yes when prompted to continue.

2. Get an SSL Certificate
```sudo certbot --apache```

```Enter email address (used for urgent renewal and security notices) (Enter 'c' to cancel):```
Enter an email address where you can be contacted in case of urgent renewal and security notices.

```Please read the Terms of Service at
   https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf. You must
   agree in order to register with the ACME server at
   https://acme-v02.api.letsencrypt.org/directory```

Press a and ENTER to agree to the Terms of Service.

```Would you be willing to share your email address with the Electronic Frontier
   Foundation, a founding partner of the Let's Encrypt project and the non-profit
   organization that develops Certbot? We'd like to send you email about EFF and
   our work to encrypt the web, protect its users and defend digital rights.
```

Press n and ENTER to not share your email address with EFF.

```Which names would you like to activate HTTPS for?```

If you do not already have a list with names, you need to add a Domain name for your Portainer service.<br />
One suggestion is to give a name in the following format: <br />
```hostname.dnsdomainname```<br />
you can find the ```hostname``` and the ```dnsdomainname``` by executing the corresponding commands in the machine you want to run Portainer.

Make sure you remember the Domain name because you will be prompted to enter it while executing ```deployLocal.sh``` script.

If you do have a list with names:
Select option 1 if you don’t want to use the www. prefix in your website address, otherwise select option 2.

```Obtaining a new certificate......```

Press 1 and ENTER to No redirect - Make no further changes to the webserver configuration.

*The SSL certificate just created.*

**Keep in mind that ```letsencrypt``` certifications expire after 90 days**

# Troubleshooting

While ```sudo docker service ls```, if the services are Replicated 0/1:

1) Check that you have enough space in your machine.

2) If there is an ERROR, try ```sudo docker service ps --no-trunc NAME_OR_ID_OF_SERVICE``` to see the whole message.
