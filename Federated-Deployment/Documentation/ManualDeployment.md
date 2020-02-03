# Swarm Initialization

For the initialization of Swarm you have to run on the master node:

```ansible-playbook -i hosts.ini Init-Swarm.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv```

## Join Workers

If you have worker nodes available you should do the following for each worker:

``` ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv -e "my_host=workerX"``` by changing the value ```my_host``` with the worker name.

## Start Exareme Services

Next thing would be to run Exareme services and Portainer service. The Exareme services will run on all available exareme nodes (master,workers).

```ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv```

If you want to exclude Portainer service from running, you need to add ```--skip-tags portainer``` in the command, meaning:

```ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko  --ask-vault-pass -e@vault.yaml --skip-tags portainer -vvvv```

If you want to start only Portainer Service you need to:
```
ansible-playbook -i hosts.ini Start-Exareme.yaml -c paramiko --ask-vault-pass -e@vault.yaml -vvvv --tags portainer
```

## Stop Services

If you want to stop Exareme services [master/workers] but no Portainer services, you can do so by:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv --tags exareme -vvvv```

If you only want to stop the Portainer service you can do so by:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko --ask-vault-pass -e@vault.yaml -vvvv --skip-tags exareme -vvvv```

If you want to stop all services [Exareme master/Exareme workers/Portainer]:

```ansible-playbook -i hosts.ini Stop-Services.yaml -c paramiko --ask-vault-pass -e@vault.yaml  -vvvv```


## Add an Exareme Worker when the master is already running

0) Insert the nodes information in the hosts.ini following the structure: <a href="https://github.com/madgik/exareme/blob/reorderReadme/Federated-Deployment/Documentation/Optionals.md#optional-initialize-hosts">Initialize Hosts</a><br/>
and update ansible-vault file following the instructions: <a href="https://github.com/madgik/exareme/blob/reorderReadme/Federated-Deployment/Documentation/Optionals.md#optional-ansible-vault">Ansible-vault</a><br/>

1) Join the particular worker by replacing workerN with the appropriate name:
``` ansible-playbook -i hosts.ini Join-Workers.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv -e "my_host=workerX"```
by changing the value ```my_host``` with the worker name.

2) Start the Exareme service for that particular worker by replacing workerN with the appropriate name: ``` ansible-playbook -i hosts.ini Start-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv -e "my_host=workerX"```
by changing the value ```my_host``` with the worker name.

## Stop ΟΝΕ Exareme Worker

If at some point you need to stop only one worker, you can do so by the following command replacing workerN with the appropriate identifier:
``` ansible-playbook -i hosts.ini Stop-Exareme-Worker.yaml -c paramiko  --ask-vault-pass -e@vault.yaml -vvvv -e "my_host=workerX"```
by changing the value ```my_host``` with the worker name.
