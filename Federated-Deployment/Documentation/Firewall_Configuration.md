Note: </br> 
This is a modified Documentation from previous work that had been done by DIAS-EPFL.</br>
Initial source can be found here: https://github.com/HBPMedical/mip-federation/tree/master/Documentation

In this document you will find configurations in case of firewall existing in Federation nodes.

# Firewall Configuration

## Configuration requirements

The Docker *Overlay Network* technology is a Software Defined Network (SDN) system which allows the arbitrary definition of network between docker containers.

To accomplish this, it maintains a database of hosts on which each network is available, and multiplexes the network traffic of the docker container over a single network connection between these hosts. It also allows encryption of the tunneled (application) data.

All management communication is done through TLS encrypted communications between the hosts of the Docker Swarm (cluster, or federation in the case of the MIP). These certificates are automatically managed, and regenerated every 30 minutes by default.

The following ports and protocols are required to be open for the proper function of the Docker Swarm overlay network technology:

 * On **all** the Docker hosts which are part of the Swarm (federation):
   * **TCP: 7946**
   * **UDP: 7946**
   * **UDP: 4789**
   * **Protocol 50 (ESP)**

 * **Only** on the Docker manager hosts of the Swarm (federation) :
   * **TCP: 2377**

## UFW Configuration for the MIP

The following command will configure and then enable the firewall on Ubuntu, with the minimum ports required for the federation networks.

Specific public services provided by the MIP to the end-users will require their own configuration to be added.

1. Check the status of UFW

    ```sh
    $ sudo ufw status
    -> Status: inactive
    ```

2. Allow SSH access

    ```sh
    $ sudo ufw allow ssh
    -> Rules updated
    -> Rules updated (v6)
    ```
3. Docker Swarm ports

    ```sh
    $ sudo ufw allow 7946/tcp
    $ sudo ufw allow 7946/udp
    $ sudo ufw allow 4789/udp
    $ sudo ufw allow proto esp from any to any
    -> Rules updated
    -> Rules updated (v6)
    ```

4. Docker Swarm ports for Manager nodes 
  
   **The following is required only on the Docker Swarm manager computers.**

    ```sh
    $ sudo ufw allow 2377/tcp
    -> Rules updated
    -> Rules updated (v6)
    ```

5. Enable UFW to enforce the rules    

    ```sh
    $ sudo ufw enable
    ```

6. Check the status

    *The example below has been executed on a worker node of the federation.*

    ```sh
    $ sudo ufw status
    Status: active
    
    To                         Action      From
    --                         ------      ----
    22                         ALLOW       Anywhere                  
    7946/tcp                   ALLOW       Anywhere                  
    7946/udp                   ALLOW       Anywhere                  
    4789/udp                   ALLOW       Anywhere                  
    Anywhere/esp               ALLOW       Anywhere/esp              
    22 (v6)                    ALLOW       Anywhere (v6)             
    7946/tcp (v6)              ALLOW       Anywhere (v6)             
    7946/udp (v6)              ALLOW       Anywhere (v6)             
    4789/udp (v6)              ALLOW       Anywhere (v6)             
    Anywhere/esp (v6)          ALLOW       Anywhere/esp (v6)         

    ```

7. Docker swarm ports should be behind a firewall

   If docker swarm has some ports published that you want to be behind the ufw firewall follow these instructions:
   https://svenv.nl/unixandlinux/dockerufw/
   
   The reason for this is that docker swarm writes directly to the iptables and when it publishes a port, this port is not behind the ufw
   firewall.



## Firewalld Configuration for the MIP

On RedHat Enterprise Linux, use the Firewalld configuration files `docker-swarm-worker.xml` and `docker-swarm-master.xml` available in the same folder as this file. The following command will configure and then enable the firewall on RHEL, with the minimum ports required for the federation networks.

Specific public services provided by the MIP to the end-users will require their own configuration to be added.

1. Check the status of Firewalld

    ```sh
    $ sudo firewall-cmd --state
    running
    $ sudo firewall-cmd --list-services
    ssh dhcpv6-client
    $ sudo firewall-cmd --info-service=ssh
    ssh
      ports: 22/tcp
      protocols: 
      source-ports: 
      modules: 
      destination: 
    ```

2. If needed, start firewalld

    ```sh
    $ sudo systemctl enable firewalld.service
    ```

3. Docker Swarm ports
   Do one of the following, assuming you are in the folder containing this file. This installs and activate permanently the service profile.
   
    * For worker nodes:

    ```sh
    $ sudo cp docker-swarm-worker.xml /etc/firewalld/services/
    $ sudo firewall-cmd --permanent --add-service=docker-swarm-worker
	 success
    ```

    * For Manager nodes 
   
    ```sh
    $ sudo cp docker-swarm-manager.xml /etc/firewalld/services/
    $ sudo firewall-cmd --permanent --add-service=docker-swarm-manager
	 success
    ```

4. Reload the firewall configuration:    

    ```sh
    $ sudo firewall-cmd --reload
	 success
    ```

5. Check the status

    * For worker nodes:

    ```sh
    $ sudo firewall-cmd --info-service=docker-swarm-worker
    ```

    * For Manager nodes 

    ```sh
    $ sudo firewall-cmd --info-service=docker-swarm-manager
    ```
