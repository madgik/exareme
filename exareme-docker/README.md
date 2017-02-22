**Instructions for using the Exareme local Docker**
-

1) Execute startall.sh ( set ups the enviroment and executes the RAW-deploy/docker-compose.yml)
2) Use the ip:9090/exa-view/index.html web page to send queries to Exareme and view the results.

**Important files**
-

- datasets: add your csv here to be loaded into RAW
- exalocal/build.sh: build the latest exaremelocal image (the same as hbpmip/exaremelocal)
- exalocal/start.sh: starts an exaremelocal container
- exalocal/docker-compose.yml: used only for testing

- RAW-deploy/docker-compose.yml: use by executing ./start unsecured up, this will start exareme in local mode and raw


Notes:
- to change the information (credentials, address) that exareme uses to connect to raw you need to change either the start.sh or the docker-compose.yml 
