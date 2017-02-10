**Instructions for using the Exareme local Docker**
-

- build.sh: build the exaremelocal images (the same as hbpmip/exaremelocal)
- start.sh: start the an exaremelocal container
- docker-compose.yml: containes rules to start and link the exaremelocal and raw-sniffer images

Notes:
- to change the information (credentials, address) that exareme uses to connect to raw you need to change either the start.sh or the docker-compose.yml


**Testing**
-

Use the ip:9090/exa-view/index.html web page to send queries to Exareme and view the results.
