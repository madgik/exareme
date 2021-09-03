# Deployment by Hospital

In case when a Hospital ```can not/will not``` give the sensitive data like usernames and passwords (credentials) needed in order for Ansible to run, here is a workaround:

1) The Hospital must contact the system administrator so he/she will handle the command needed in order for Hospital to be part of the Swarm as ```Worker node```.
2) The Hospital must run the command ```docker swarm join --token <Swarm Token> <Master Node URL>:2377``` given by the system administrator.

For example the command could look like this:

```(sudo) docker swarm join --token SWMTKN-1-22ya4cjf2c1aq4sbnypwkvs2z87wg2897xi35qvp1hs54s85of-doah1kp92psb8rqvbgshu7ro2 88.197.53.38:2377```

If a node is behind a NAT, its local IP will be advertised by default and other nodes will not be able to contact it.<br/>
**Note:** Non-static public IP should be fine for worker nodes, but this has not been tested.

If the worker node is behind a NAT server, you must specify the Public IP to use to contact that Worker node from other nodes with:

 ```docker swarm join --advertise-addr <Public IP> --token <Swarm Token> <Master Node URL>:2377```

3) Inform the system administrator that the command run, so he/she can Start Exareme instance at the specific Hospital from the ```Manager node``` of Swarm.
