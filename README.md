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
$ cd src/main/resources
$ kubectl create configmap auth-policy --from-file=policies\swisscom\auth
```

4. Check if configmaps successfully loaded

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service/src/main/resources
$ kubectl get configmap
NAME               DATA   AGE
auth-policy        2      5h
kube-root-ca.crt   1      5h31m
```
5. Start `OPA` and `my-cool-service` clusters

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service/src/main/resources/
$ cd ~/IdeaProjects/my-cool-service/
$ kubectl apply -f my-cool-service-deployment.yaml
$ kubectl apply -f opa-deployment.yaml
```

6. Check the status of pods if the deployment is successful and clusters are running

```
User@DESKTOP MINGW64 ~/IdeaProjects/my-cool-service/src/main/resources/
$ kubectl get deployments
NAME                         READY   UP-TO-DATE   AVAILABLE   AGE
my-cool-service-deployment   1/1     1            1           4h4m
opa                          1/1     1            1           5h2m
$ kubectl get pods
NAME                                          READY   STATUS    RESTARTS   AGE
my-cool-service-deployment-7bbbcd5856-fbzmj   1/1     Running   0          4h1m
opa-5cc6d5dcfd-dcgtz                          1/1     Running   0          4h59m
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

You are ready to call REST API's provided for the task by using `curl` command below or [Postman](https://www.postman.com/downloads/) <br>
<br>
<b>Note:</b> For bypassing SSL verification, you can use `-k` (it is not a recommended practice) or use http in the curl command directly <br>

For SSL verification, you need to import the [server.crt](server.crt) to your computer for SSL handshake between client and server.<br>


##### 3.1.1 GET users endpoint

Basic authentication is needed to invoke API. 
You can use one of the users above to authenticate and retrieve the results.

Request:
```
curl --location 'https://localhost:30000/api/users' \
--header 'Authorization: Basic cm9sZWxlc3N1c2VyOnJvbGVsZXNzdXNlcg=='
```

Response:

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

Request:
```
curl --location 'https://localhost:30000/api/users' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic YWRtaW51c2VyOmFkbWludXNlcg==' \
--data-raw '{
    "userName": "kaydemir",
    "email" :"kemal.aydemir@gmail.com"
}'
```

Response:

Returns a boolean true if the user it created, otherwise return the details with
appropriate http response code and description.

```
true
```