# Exareme   ![Alt text](https://travis-ci.org/madgik/exareme.svg?branch=mip)


##  Building Exareme

* requires : 
    -  git, jdk 1.7, maven
    
* open new terminal and execute    
    ``` 
        git clone https://github.com/madgik/exareme.git -b mip exareme-src
        cd exareme-src
        mvn clean install -DskipTests -Pmultiarch
        mv exareme-distribution/target/*.tar.gz ~/
    ```
    
## Single node installation

* Package dependencies :

    - jre 1.7, Python 2.7, git, [APSW 3.11] (https://rogerbinns.github.io/apsw/download.html)
    - requests, NumPy, SciPy, scikit-learn, titus (can be installed through pip, also listed in requirements.txt) 

* Download mip-algorithms
    ```
    cd ~/
    wget https://github.com/madgik/mip-algorithms/archive/master.zip
    unzip master.zip
    ```

* Configuration under ~/mip-algorithms-master/

    - edit properties.json
        + specify rawdb address, port, credentials, query
        
* Extract Exareme tarball
 
    ```
    mkdir ~/exareme
    tar xf ~/exareme.tar.gz -C ~/exareme
    cd ~/exareme
    ```
    
* Configuration under ~/exareme/

    - Specify master/worker nodes 
    
        ```
        echo $(hostname --ip-address) > etc/exareme/master
        echo "" > etc/exareme/workers
        ```
        
    - Edit the etc/exareme/exareme-env.sh and specify java, python installation (if needed).   
    - Edit the etc/exareme/gateway.properties 
        + specify the mip-algorihtms path (e.g. 
        + specify the gateway port (if needed).

* Start/Stop Exareme and check the logs

    ```
    cd ~/exareme
    ./bin/exareme-admin.sh --start --local
    tail -f /tmp/exareme/var/log/exareme-master.log
    ./bin/exareme-admin.sh --kill --local
    ```

## Cluster installation

* Set up ssh password-less access from master node to all nodes. 
* Install and configure mip-algorithms on each node.
* On master node install Exareme based on single node instructions.
    ```
    cd ~/exareme
    ./bin/exareme-admin.sh --install
    ./bin/exareme-admin.sh --start
    ./bin/exareme-admin.sh --kill
    ```
    
* In order to update configuration files accross nodes
    ```
    ./bin/exareme-admin.sh --update    
    ```
