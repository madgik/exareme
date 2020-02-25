# Exareme with Docker 

You can follow the instructions below to create a Docker image for Exareme.
Tested on *Ubuntu 16.04 LTS* *not tested* on other platforms

## Procedure

1. Install build requirements. <br/>
```
sudo apt-get install -y git maven curl python python-apsw jq
sudo apt-get install -y python-dev build-essential python-pip libblas-dev liblapack-dev libatlas-base-dev gfortran
```
2. Create an account in Docker Hub. <br/>
Go to https://hub.docker.com/ and navigate through *Get Started* to create a user.

3. Clone the source. <br/>
```
git clone https://github.com/madgik/exareme.git
cd exareme/
```

4. Package Exareme. <br/>
```
cd Exareme-Docker/src/exareme/
mvn clean install
```

5. Build a Docker image for Exareme where *username* is your username for Docker Hub and *tag* whichever tag (latest by default). <br/>
```
cd ../..
docker build -t username/exareme:tag .
```

6. (Optional) Push your image to Docker Hub where *username* is your username for Docker hub and *tag* whichever tag (latest by default). <br/>
```
docker push username/exareme:tag
```
