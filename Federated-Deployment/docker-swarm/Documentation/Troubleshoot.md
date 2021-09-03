# Troubleshooting

## Portainer

If you followed the instructions for <a href="../../Documentation/SecurePortainer.md#optional-secure-portainer">Secure Portainer</a> you can access Portainer by *DOMAIN_NAME:9000* or *www.DOMAIN_NAME:9000*  with respect to the instructions that you followed. If you did run Portainer in an un-secure way, you can access Portainer by *MANAGER_OF_SWARM'S_IP:9000*

The first time you launch Portainer you have to create a user.
Fill in the ```Username```, ```Password```, ```Confirm Password``` fields and click ```Create user```.<br/>
[Mind that we create a folder called ```portainer``` where the credentials you provided will be saved for the next times, until you delete the folder. For Federated-Deployment that path is the ```home_path```, for Local-Deployment that path is the path from where you run the *deployLocal.sh* script]<br/>
The next page ```Connect Portainer to the Docker environment you want to manage.``` will ask you to Connect Portainer to an Environment. Click the first option  ```Local``` and ```Connect```.<br/>
After that, you should click on your Local Swarm and navigate from the left menu.

Once Portainer service is launched:

Under ```Services``` in the left menu check that all services has 1 replicas: ```replicated 1 / 1```.<br/>

If this is not the case, meaning you get ```replicated 0 / 1```, the service for the specific node did not run:

    From the specific node:

    1) Make sure you have enough space on the specific node

    From the Manager node of Swarm:

    1) Check the ERROR message by doing ```sudo docker service ps --no-trunc NAME_OR_ID_OF_service```
    2) If Worker node ```Manually``` joined the Swarm:
        - Make sure the node has actually joined the Swarm and that it is tagged with a proper name.


## Check that all workers are seen by Exareme

Connect to the Exareme test page at `http://localhost:9090/exa-view/index.html`.
The algorithm LIST\_DATASET should show datasets for each pathology available at each node of the Federation.

If a node's information is missing:

    From the specific node of Swarm:

    1) Check the network configuration for this node. ```Obtaining the correct network configuration for each server that must join the Federation might not be straightforward.```
    2) Make sure the datasets and the CDEs are in the correct data path under ```pathology``` folder.

    From the Manager node of Swarm:

    1) Check the logs and report them to Exareme expertise team.

## Restart Exareme

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
