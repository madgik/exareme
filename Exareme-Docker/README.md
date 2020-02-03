# Docker container for Exareme

You can follow the instructions below to create a Docker image for Exareme.
*Ubuntu 16.04 LTS not tested on other platforms*

## Build procedure

1. Install build requirements
```
sudo apt-get install -y git maven curl python python-apsw jq
sudo apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
```
2. Create an account in Docker Hub
Go to https://hub.docker.com/.

3. Clone the source
```
git clone https://github.com/madgik/exareme.git
cd exareme/
```

4. Maven clean install
```
cd Exareme-Docker/src/exareme/
mvn clean install
```

5. Build the Docker image where *username* is your username for Docker Hub and *tag* whichever tag (latest by default)
```
cd ../..
docker build -t username/exareme:tag .
```

6. (Optional) Push your image to Docker hub where *username* is your username for Docker hub and *tag* whichever tag (latest by default)
```
docker push username/exareme:tag
```