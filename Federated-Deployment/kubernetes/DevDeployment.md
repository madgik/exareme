# Exareme Development deployment with Kubernetes in one node

## Configuration

The following packages need to be installed:

```
docker
kubectl
helm
```

## Setup the kubernetes cluster with kind

1. Create the cluster using the e2e_tests setup (you can create a custom one if you want) :
```
kind create cluster --config Federated-Deployment/kubernetes/kind_configuration/kind_cluster.yaml 
```

2. After the nodes are started, you need to taint them properly:
```
kubectl taint nodes kind-control-plane node-role.kubernetes.io/master-
kubectl label node kind-control-plane master=true
kubectl label node kind-worker worker=true
kubectl label node kind-worker2 worker=true
```

3. (Optional) Load the docker images to the kuberentes cluster, if not the images will be pulled from dockerhub:
```
kind load docker-image hbpmip/exareme:latest
```

4. Deploy the MIP-Engine kubernetes pods using helm charts:
```
helm install exareme Federated-Deployment/kubernetes/
```
