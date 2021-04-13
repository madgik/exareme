# Local Exareme Deployment Guide

Here you will find all the information needed in order to deploy Exareme in your environment via Docker swarm and shell scripts.

# Requirement

Install Docker and Docker-Compose on your machine.

# Preparation

## Clone the repository 

Clone this repository on your local computer so you can use it to deploy Exareme.

## Data Structure
In the node in which you will deploy Exareme there should be a *DATA FOLDER* which contains the *DATA* existing in that node.

The *DATA FOLDER* should follow a specific structure.
It should contain one folder for each pathology that it has datasets for. Inside that folder there should be:
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

On the current node where Exareme will be deployed, we *must include* the CDEsMetadata.json file for *every pathology* that exists even if it *doesn't contain* any .csv file.
For example:

-> Data Folder <br />
------> Dementia <br />
----------> CDEsMetadata.json <br />
------> Neuropathology <br />
----------> demo.csv <br />
----------> CDEsMetadata.json <br />

For more information on what these files should contain you can see <a href="../Documentation/InputRequirements.md#input-requirements">here</a>.

# Deployment

Create a ```.env``` file in the *Local-Deployment* folder and add the following:
```
EXAREME_IMAGE=hbpmip/exareme:23.0.0
DATA_FOLDER=/home/user/data
```

Then run the ```docker-compose up``` to start the deployment. You can run ```docker-compose up -d``` to run it in the background.
