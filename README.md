# Container Orchestration Project: Learner Report Application Deployment

This repository contains Kubernetes configurations and CI/CD pipeline setup for deploying a MERN (MongoDB, Express.js, React.js, Node.js) application using Kubernetes and Jenkins.

## Table of Contents
- [Project Overview](#project-overview)
- [Prerequisites](#prerequisites)
- [Application Architecture](#application-architecture)
- [Local Setup](#local-setup)
- [Dockerization](#dockerization)
- [Kubernetes Deployment](#kubernetes-deployment)
  - [Database Deployment](#database-deployment)
  - [Backend Deployment](#backend-deployment)
  - [Frontend Deployment](#frontend-deployment)
- [CI/CD Pipeline with Jenkins](#cicd-pipeline-with-jenkins)
- [AWS EKS Deployment](#aws-eks-deployment)
- [Screenshots](#screenshots)
- [Troubleshooting](#troubleshooting)

## Project Overview

This project demonstrates how to deploy a MERN stack application using container orchestration tools. The application consists of:
- MongoDB database
- Node.js/Express.js backend
- React.js frontend

The deployment uses Kubernetes for orchestration and Jenkins for CI/CD automation.

## Prerequisites

- Docker
- Kubernetes (Minikube for local testing)
- kubectl
- AWS CLI (for EKS deployment)
- eksctl (for EKS deployment)
- Jenkins
- Git

## Application Architecture

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Frontend  │     │   Backend   │     │  Database   │
│  React.js   │────▶│  Express.js │────▶│   MongoDB   │
│   (Port 80) │     │ (Port 3001) │     │ (Port 27017)│
└─────────────┘     └─────────────┘     └─────────────┘
```

## Local Setup

### Clone Repositories
```bash
git clone https://github.com/UnpredictablePrashant/learnerReportCS_backend.git
git clone https://github.com/UnpredictablePrashant/learnerReportCS_frontend.git
```

### Setup Backend
```bash
cd learnerReportCS_backend
npm install
# Update config.env with MongoDB connection string
node index.js
```

### Setup Frontend
```bash
cd learnerReportCS_frontend
npm install --legacy-peer-deps
npm start
```

### Create User via Postman
Send a POST request to create a user before login:
- Endpoint: `http://localhost:3001/api/users/register`
- Method: POST
- Body: JSON with user credentials

## Dockerization

### Backend Dockerfile
```dockerfile
FROM node:16
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 3001
CMD [ "node", "index.js" ]
```

### Frontend Dockerfile (Development)
```dockerfile
FROM node:16
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install --legacy-peer-deps --silent
COPY . .
EXPOSE 3000
CMD ["npm","start"]
```

### Frontend Dockerfile (Production for Kubernetes)
```dockerfile
# Stage 1: Build the React application
FROM node:16 AS build
WORKDIR /usr/src/app
COPY package*.json ./
RUN npm install --legacy-peer-deps --silent
COPY . .
RUN npm run build

# Stage 2: Serve the application using nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html
RUN rm -rf ./*
COPY --from=build /usr/src/app/build .
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### Build and Push Docker Images
```bash
# Backend
docker build -t learnerreport:backend .
docker tag learnerreport:backend yourusername/learnerreport:backend
docker push yourusername/learnerreport:backend

# Frontend (Development)
docker build -t learnerreport:frontend .
docker tag learnerreport:frontend yourusername/learnerreport:frontend
docker push yourusername/learnerreport:frontend

# Frontend (Production)
docker build -t learnerreport:frontendforkubernetes .
docker tag learnerreport:frontendforkubernetes yourusername/learnerreport:frontendforkubernetes
docker push yourusername/learnerreport:frontendforkubernetes
```

## Kubernetes Deployment

### Database Deployment

1. Create namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: lrdatabase
```

2. Create PersistentVolume
```yaml
apiVersion: v1
kind: PersistentVolume
metadata:
  name: db-pv
  namespace: lrdatabase
spec:
  capacity:
    storage: 2Gi
  accessModes:
    - ReadWriteOnce
  hostPath:
    path: /mnt/data
```

3. Create PersistentVolumeClaim
```yaml
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: db-pvc
  namespace: lrdatabase
spec:
  accessModes:
    - ReadWriteOnce
  resources:
    requests:
      storage: 2Gi
```

4. Create Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: db-deployment
  namespace: lrdatabase
spec:
  replicas: 1
  selector:
    matchLabels:
      app: mongodb
  template:
    metadata:
      labels:
        app: mongodb
    spec:
      containers:
      - name: lrmongodb
        image: mongo:latest
        ports:
        - containerPort: 27017
        volumeMounts:
        - mountPath: /data/db
          name: mongodb-storage
      volumes:
      - name: mongodb-storage
        persistentVolumeClaim:
          claimName: db-pvc
```

5. Create Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: db-service
  namespace: lrdatabase
spec:
  selector:
    app: mongodb
  ports:
  - protocol: TCP
    port: 27017
    targetPort: 27017
  type: ClusterIP
```

### Backend Deployment

1. Create namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: lrbackend
```

2. Create Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: be-secret
  namespace: lrbackend
type: Opaque
data:
  MONGO_URI: bW9uZ29kYjovL2RiLXNlcnZpY2UubHJkYXRhYmFzZS5zdmMuY2x1c3Rlci5sb2NhbDoyNzAxNy9MZWFybmVyUmVwb3J0
```

3. Create Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: be-deployment
  namespace: lrbackend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
      - name: lrbackend
        image: yourusername/learnerreport:backend
        ports:
        - containerPort: 3001
        env:
        - name: PORT
          value: "3001"
        - name: MONGO_URI
          valueFrom:
            secretKeyRef:
              name: be-secret
              key: MONGO_URI
```

4. Create Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: be-service
  namespace: lrbackend
spec:
  selector:
    app: backend
  ports:
  - protocol: TCP
    port: 3001
    targetPort: 3001
  type: LoadBalancer
```

### Frontend Deployment

1. Create namespace
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: lrfrontend
```

2. Create Secret
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: fe-secret
  namespace: lrfrontend
type: Opaque
data:
  REACT_APP_BACKEND_URL: aHR0cDovL2JlLXNlcnZpY2UubHJiYWNrZW5kLnN2Yy5jbHVzdGVyLmxvY2FsOjMwMDE=
```

3. Create Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: fe-deployment
  namespace: lrfrontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: lrfrontend
        image: yourusername/learnerreport:frontendforkubernetes
        ports:
        - containerPort: 80
        resources:
          limits:
            memory: "512Mi"
            cpu: "500m"
          requests:
            memory: "256Mi"
            cpu: "250m"
        env:
        - name: PORT
          value: "3000"
        - name: REACT_APP_BACKEND_URL
          valueFrom:
            secretKeyRef:
              name: fe-secret
              key: REACT_APP_BACKEND_URL
```

4. Create Service
```yaml
apiVersion: v1
kind: Service
metadata:
  name: fe-service
  namespace: lrfrontend
spec:
  selector:
    app: frontend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 80
  type: LoadBalancer
```

## CI/CD Pipeline with Jenkins

### Jenkins Pipeline Script
```groovy
pipeline {
    agent any
    environment {
        DOCKER_CREDS = 'aakash-dockerhub'
        DOCKER_REG = 'aakashrawat1910'
        GIT_URL = 'https://github.com/aakashrawat1910/Learner-Report-Application-Deployment.git'
        GIT_BRANCH = 'main'
        AWS_ACCESS_KEY_ID = 'AWS_ACCESS_KEY_ID'
        AWS_SECRET_ACCESS_KEY = 'AWS_SECRET_ACCESS_KEY'
        AWS_REGION = 'us-west-1'
        CLUSTER_NAME = 'aakash-learnerapplication'
    }
    
    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out code from GitHub...'
                git url: "${env.GIT_URL}", branch: "${env.GIT_BRANCH}"
            }
        }
        
        stage('Push Docker Images') {
            steps {
                script {
                    echo 'Pushing Docker images to Docker Hub...'
                    docker.withRegistry('', DOCKER_CREDS) {
                        docker.image("${DOCKER_REG}/learnerreport:backend").push()
                        docker.image("${DOCKER_REG}/learnerreport:frontendforkubernetes").push()
                    }
                }
            }
        }
        
        stage('Configure kubectl') {
            steps {
                script {
                    echo 'Configuring kubectl with AWS EKS...'
                    sh """
                    export AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID}
                    export AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY}
                    export AWS_REGION=${AWS_REGION}
                    aws --version
                    """
                }
            }
        }
        
        stage('Create Kubernetes Cluster') {
            steps {
                script {
                    sh """
                    # Check if cluster exists
                    if ! aws eks describe-cluster --name ${CLUSTER_NAME} --query 'cluster.status' --output text 2>/dev/null; then
                        echo "Cluster does not exist. Creating a new cluster..."
                        eksctl create cluster --name ${CLUSTER_NAME} \\
                            --region ${AWS_REGION} \\
                            --version 1.31 \\
                            --nodegroup-name aakash-standard-workers \\
                            --node-type t3.medium \\
                            --nodes 2 \\
                            --nodes-min 1 \\
                            --nodes-max 3 \\
                            --with-oidc \\
                            --managed
                    else
                        echo "Cluster already exists. Using existing cluster."
                    fi
                   
                    # Update kubeconfig regardless of creation
                    aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}
                    """
                }
            }
        }
        
        stage('Deploy Database to Kubernetes') {
            steps {
                script {
                    echo 'Deploying Database to Kubernetes...'
                    sh """
                    kubectl apply -f './Kubernetes Deployment/database/namespace.yml'
                    kubectl apply -f './Kubernetes Deployment/database/db-pv.yml'
                    kubectl apply -f './Kubernetes Deployment/database/db-pvc.yml'
                    kubectl apply -f './Kubernetes Deployment/database/deployment.yml'
                    kubectl apply -f './Kubernetes Deployment/database/service.yml'                    
                    """
                }
            }
        }
        
        stage('Deploy Backend to Kubernetes') {
            steps {
                script {
                    echo 'Deploying Backend to Kubernetes...'
                    sh """                    
                    kubectl apply -f './Kubernetes Deployment/backend/namespace.yml'
                    kubectl apply -f './Kubernetes Deployment/backend/secret.yml'
                    kubectl apply -f './Kubernetes Deployment/backend/deployment.yml'
                    kubectl apply -f './Kubernetes Deployment/backend/service.yml'
                    """
                }
            }
        }
        
        stage('Update loadbalancer IP in Frontend') {
            steps {
                script {
                    echo 'Updating load balancer IP in Frontend...'
                   
                    // Fetch the backend service hostname
                    def backendService = sh(script: "kubectl get svc be-service -n lrbackend -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'", returnStdout: true).trim()
                   
                    // Debugging: Print the backendService value
                    echo "Backend Service Hostname: ${backendService}"
                   
                    // Check if backendService is empty
                    if (!backendService) {
                        error "Failed to retrieve backend service hostname. Ensure the service 'be-service' is deployed and has a valid load balancer IP/hostname."
                    }
                   
                    // Create base64 encoded URL for secret
                    def backendUrl = "http://${backendService}:3001"
                    echo "Backend URL: ${backendUrl}"
                   
                    def encodedUrl = sh(script: "echo -n '${backendUrl}' | base64", returnStdout: true).trim()
                    echo "Encoded Backend URL: ${encodedUrl}"
                   
                    // Use a temporary file to update secret.yml
                    sh """
                    temp_file=\$(mktemp)
                    echo "REACT_APP_BACKEND_URL: \\\"${encodedUrl}\\\"" > \$temp_file
                    sed -i '/REACT_APP_BACKEND_URL:/r \$temp_file' './Kubernetes Deployment/frontend/secret.yml'
                    rm -f \$temp_file
                    """
                }
            }
        }
        
        stage('Deploy Frontend to Kubernetes') {
            steps {
                script {
                    echo 'Deploying Frontend to Kubernetes...'
                    sh """
                    kubectl apply -f './Kubernetes Deployment/frontend/namespace.yml'
                    kubectl apply -f './Kubernetes Deployment/frontend/secret.yml'
                    kubectl apply -f './Kubernetes Deployment/frontend/deployment.yml'
                    kubectl apply -f './Kubernetes Deployment/frontend/service.yml'
                    """
                }
            }
        }
    }
}
```

## AWS EKS Deployment

### Create EKS Cluster
```bash
eksctl create cluster \
  --name aakash-eksctl-learnerapplication \
  --region us-west-1 \
  --version 1.31 \
  --nodegroup-name aakash-standard-workers \
  --node-type t3.medium \
  --nodes 2 \
  --nodes-min 1 \
  --nodes-max 3 \
  --with-oidc \
  --ssh-access \
  --ssh-public-key eks-key.pub \
  --managed
```

### Connect to EKS Cluster
```bash
aws eks --region us-west-1 update-kubeconfig --name aakash-eksctl-learnerapplication
```

### Deploy Application
Follow the same Kubernetes deployment steps as above, but make sure to update the frontend secret with the EKS backend service URL:

```bash
# Get backend service URL
kubectl get svc -n lrbackend

# Encode URL in base64
echo -n "http://BACKEND_SERVICE_URL:3001" | base64

# Update frontend secret with encoded URL
# Then apply all Kubernetes configurations
```

## Screenshots

<div align="center">
  <p><strong>Local Application Running</strong></p>
  ![image](https://github.com/user-attachments/assets/e8048026-a0ca-46c8-a2bf-7bf8aefe2300)
  ![image](https://github.com/user-attachments/assets/1c5277ce-6f61-4b09-a4e2-debd175fda66)
  ![image](https://github.com/user-attachments/assets/e6abcbd6-2cb8-43b7-b92e-a95583c0d79c)
  
  <p><strong>User Creation in Postman</strong></p>
  ![image](https://github.com/user-attachments/assets/44acd76f-1d6a-450f-9cf9-293a61f5ad73)
  
  <p><strong>Application Login</strong></p>
  ![image](https://github.com/user-attachments/assets/c5edf8be-f992-43c1-9ab1-d992a930c5fc)
  
  <p><strong>Docker Container Running</strong></p>
  ![image](https://github.com/user-attachments/assets/2b962f7d-189f-43ed-bfd5-21677ebf8ea3)
  
  <p><strong>Kubernetes Deployment</strong></p>
  ![image](https://github.com/user-attachments/assets/f2936ea3-d478-4097-ad8e-28c337f74b57)
  ![image](https://github.com/user-attachments/assets/c63bfe47-d9bb-49de-aab4-6486d4d43a9a)
  ![image](https://github.com/user-attachments/assets/c0be9058-e054-4eae-bb65-cfb793c7b188)
  
  <p><strong>Jenkins Pipeline</strong></p>
  ![image](https://github.com/user-attachments/assets/8b0421c7-e382-4e4b-b16c-30cb039db2cc)
  
  <p><strong>EKS Deployment</strong></p>
  ![image](https://github.com/user-attachments/assets/9aad0009-0bf1-4632-82d5-6db32787167e)
  ![image](https://github.com/user-attachments/assets/bb9c95c1-ce18-4a0c-bb91-4846107e7a51)
</div>

## Troubleshooting

### Frontend Not Connecting to Backend
- Verify the backend service is running: `kubectl get svc -n lrbackend`
- Check that the frontend secret contains the correct encoded backend URL
- Ensure all services are exposed correctly and have the right ports configured

### Database Connection Issues
- Check MongoDB pod status: `kubectl get pods -n lrdatabase`
- Verify the MongoDB connection string in the backend secret
- Ensure the PersistentVolume and PersistentVolumeClaim are correctly bound

### Jenkins Pipeline Failures
- Verify Docker Hub credentials are correctly configured in Jenkins
- Ensure AWS credentials are properly set up as Jenkins credentials
- Check that all path references in Jenkins pipeline script are correct
