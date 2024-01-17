# User Guide

## Introduction
This document covers installation and operation guide for Swisscom interview project.

The goal of this project is to integrate [OPA](https://www.openpolicyagent.org/docs/latest/) (Open policy agent) authorization service
with `my-cool-service` implemented on Spring Boot using REST API to be able to make authorization for the GET, POST request.

It will be needed to create two REST endpoints:
1. GET https://my-cool-service:8000/api/users
   a. Any authenticated user can read users.
   b. The endpoint returns list of users as JSON
   c. User attributes: name, email
2. POST https://my-cool-service:8000/api/users
   a. Only "admin" role can create new users.
   b. User attributes: name, email

Both OPA and your services should run in Kubernetes (k8s) cluster
Every authorization request should be validated and logged.
For OPA service (you can use the following Docker image: /openpolicyagent/opa:edge-rootless).

Access evaluation should be implemented as follows;
- Only users with "admin" role can create new user
- Only authenticated users can read the list of users
- Everyone else has no access

## Getting Started
This project has been implemented with Spring Boot and Spring Security, and it is integrated with OPA for fine-grained authorization.
Spring Security is responsible for the authenticating the users to the system and OPA is responsible for role and method based authorization.
Please follow the next steps to prepare the environment to run the `my-cool-service` application.

### 1. Project Requirements
The main requirements for the `my-cool-service` application is

- ARTIFACT | VERSION
- OPA | 0.60.0
- Docker | 20.10.22
- Kubernetes | 1.25.4

Other requirements can be found in the [pom.xml](pom.xml)

### 2. How to start the clusters

Based on the operating system, you will need [Kubernetes](https://kubernetes.io/releases/download/) (k8s) installed on your machine to start the `my-cool-service` and `OPA` clusters.
[Kubernetes for Docker Desktop](https://docs.docker.com/desktop/kubernetes/) or [MiniKube](https://minikube.sigs.k8s.io/docs/start/) is an alternative where you can also start and deploy the pods kubernetes clusters.

This document explains how to start Kubernetes with Docker Desktop. <br>
Similar actions can be taken on different alternatives. <br>
<br>
Execute the following commands below step by step in the related directory. <br>

1. Enable virtualization from BIOS, and <br>
   Execute Hyper V command below from the command prompt on top of the place where you download the project <br>
   You are ready to install [Docker Desktop for Windows](https://docs.docker.com/desktop/install/windows-install/) and then [Kubernetes for Docker Desktop](https://docs.docker.com/desktop/kubernetes/)
```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ Enable-WindowsOptionalFeature -Online -FeatureName Microsoft-Hyper-V -All
```

2. Configure `kubectl` to use following namespace
```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ kubectl config set-context docker-desktop
$ kubectl config use-context docker-desktop 
```

3. Set the authorization policies for `OPA` and  environment variable for`Kubernetes`

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ kubectl create configmap auth-policy --from-file=policies\swisscom\auth
```

4. Check if configmaps successfully loaded

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ kubectl get configmap
NAME               DATA   AGE
auth-policy        2      5h
kube-root-ca.crt   1      5h31m
```
5. Create a TLS secret and check if it successfully created.
```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ kubectl create secret tls my-tls-secret --cert=certificate.pem --key=private-key.pem
secret/my-tls-secret created
kubectl create secret generic my-cool-service-cert --from-file=rootCA.pem
secret/my-cool-service-cert created
$ kubectl get secret
NAME                   TYPE                DATA   AGE
my-cool-service-cert   Opaque              1      5h
my-tls-secret          kubernetes.io/tls   2      5h
```


6. Start `OPA` and `my-cool-service` and `curl` clusters

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ cd /src/main/resources/
$ kubectl apply -f my-cool-service-deployment.yaml
$ kubectl apply -f opa-deployment.yaml
$ kubectl apply -f curl-deployment.yaml
```

7. Check the status of pods if the deployment is successful and clusters are running. 
Please note <br>curl deployment name `curl-deployment-7577d6cf6c-qwnhf` which will be needed for testing purposes.

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service/src/main/resources/
$ kubectl get deployments
NAME                         READY   UP-TO-DATE   AVAILABLE   AGE
curl-deployment              1/1     1            1           4h4m
my-cool-service-deployment   1/1     1            1           4h4m
opa                          1/1     1            1           5h2m
$ kubectl get pods
NAME                                          READY   STATUS    RESTARTS   AGE
curl-deployment-7577d6cf6c-qwnhf              1/1     Running   0          4h1m
my-cool-service-deployment-7bbbcd5856-fbzmj   1/1     Running   0          4h1m
opa-5cc6d5dcfd-dcgtz                          1/1     Running   0          4h59m
$ kubectl get services
NAME              TYPE        CLUSTER-IP      EXTERNAL-IP   PORT(S)    AGE
kubernetes        ClusterIP   10.96.0.1       <none>        443/TCP    4h59m
my-cool-service   ClusterIP   10.106.33.200   <none>        8000/TCP   4h1m
opa-service       ClusterIP   10.104.245.32   <none>        8181/TCP   4h59m
```

### 3. Testing the REST endpoints

#### 3.1 Predefined users

There are 3 authentication users predefined in the system with the following configuration; <br>
```
USERNAME    |  PASSWORD   |   ROLES
adminuser      adminuser      ROLE_ADMIN
secadminuser   secadminuser   ROLE_SECADMIN
rolelessuser   rolelessuser     -
```

Self-signed CA [certificate](rootCA.pem) created by using [mkcert](https://github.com/FiloSottile/mkcert)

Now you are ready to call REST API's provided for the task by using `kubectl` command below
<br>

##### 3.1.1 GET users endpoint

Basic authentication is needed to invoke API. 
You can use one of the users above to authenticate and retrieve the results.

Example Request with rolelessuser:
```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service/src/main/resources
$ kubectl exec -i --tty curl-deployment-7577d6cf6c-qwnhf -- sh
~ $ curl --location --cacert /etc/ssl/certs/rootCA.pem 'https://my-cool-service:8000/api/users' --header 'Authorization: Basic cm9sZWxlc3N1c2VyOnJvbGVsZXNzdXNlcg=='
```

Example Response:

Returns the users in the list format.
```
[
   {
   "userName": "kaydemir",
   "email": "kemal.aydemir@gmail.com"
   },
   {
   "userName": "kaydemir2",
   "email": "kemal.aydemir@hotmail.com"
   }
]
```

##### 3.1.2 POST user endpoint

Basic authentication and role authorization is needed to invoke API.
You can use only the user with admin role to create user.

Example Request with adminuser:
```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service
$ kubectl exec -i --tty curl-deployment-7577d6cf6c-qwnhf -- sh
~ $ curl --location --cacert /etc/ssl/certs/rootCA.pem 'https://my-cool-service:8000/api/users' --header 'Content-Type: application/json' --header 'Authorization: Basic YWRtaW51c2VyOmFkbWludXNlcg==' --data '{"userName": "kaydemir","email" :"kemal.aydemir@dzsi.com"}'
```

Example Response:

Returns a boolean true if the user it created, otherwise return the details with
appropriate http response code and description.

```
true
```