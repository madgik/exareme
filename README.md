# Exareme

## Docker Instructions
* Install 
    - [docker]  (https://docs.docker.com/engine/installation/), [docker-compose] (https://docs.docker.com/compose/install/)
    - [git] (https://git-scm.com/book/en/v2/Getting-Started-Installing-Git), [maven] (https://maven.apache.org/install.html)
* Clone and Build
```
$ git clone <exareme-docker-branch-rul> 
$ cd exareme
$ mvn clean install -DskipTests
``` 
* Running 
```
$ docker-compose up master worker
$ docker-compose run console
```
* Load Data
```
mterm>
```
* Queries
```
mterm>
```
* Stopping
```
$ docker-compose stop
$ docker-compose rm 
    ```
