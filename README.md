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

### Backend Docker Configuration
See [`Backend/Dockerfile`](./Backend/Dockerfile) for the Node.js backend containerization setup.

### Frontend Docker Configuration
- Production: See [`Frontend/Dockerfile`](./Frontend/Dockerfile) (multi-stage build with Nginx for Kubernetes)

### Build and Push Docker Images
```bash
# Backend
docker build -t learnerreport:backend .
docker tag learnerreport:backend yourusername/learnerreport:backend
docker push yourusername/learnerreport:backend

# Frontend (Production)
docker build -f Dockerfile.prod -t learnerreport:frontendforkubernetes .
docker tag learnerreport:frontendforkubernetes yourusername/learnerreport:frontendforkubernetes
docker push yourusername/learnerreport:frontendforkubernetes
```

## Kubernetes Deployment

All Kubernetes configuration files can be found in the [`Kubernetes Deployment`](./Kubernetes%20Deployment/) directory.

### Database Deployment

Database configuration files:
- [`database/namespace.yml`](./Kubernetes%20Deployment/database/namespace.yml)
- [`database/db-pv.yml`](./Kubernetes%20Deployment/database/db-pv.yml)
- [`database/db-pvc.yml`](./Kubernetes%20Deployment/database/db-pvc.yml)
- [`database/deployment.yml`](./Kubernetes%20Deployment/database/deployment.yml)
- [`database/service.yml`](./Kubernetes%20Deployment/database/service.yml)

Apply the configurations:
```bash
kubectl apply -f ./Kubernetes\ Deployment/database/
```

### Backend Deployment

Backend configuration files:
- [`backend/namespace.yml`](./Kubernetes%20Deployment/backend/namespace.yml)
- [`backend/secret.yml`](./Kubernetes%20Deployment/backend/secret.yml)
- [`backend/deployment.yml`](./Kubernetes%20Deployment/backend/deployment.yml)
- [`backend/service.yml`](./Kubernetes%20Deployment/backend/service.yml)

Apply the configurations:
```bash
kubectl apply -f ./Kubernetes\ Deployment/backend/
```

### Frontend Deployment

Frontend configuration files:
- [`frontend/namespace.yml`](./Kubernetes%20Deployment/frontend/namespace.yml)
- [`frontend/secret.yml`](./Kubernetes%20Deployment/frontend/secret.yml)
- [`frontend/deployment.yml`](./Kubernetes%20Deployment/frontend/deployment.yml)
- [`frontend/service.yml`](./Kubernetes%20Deployment/frontend/service.yml)

Apply the configurations:
```bash
kubectl apply -f ./Kubernetes\ Deployment/frontend/
```

## CI/CD Pipeline with Jenkins

The Jenkins pipeline configuration can be found in [`jenkins/Jekins-deployment.groovy`](./jenkins/Jekins-deployment.groovy).

To set up the pipeline:
1. Create a new pipeline job in Jenkins
2. Configure it to use the Pipeline script from SCM
3. Set the SCM to Git and provide your repository URL
4. Set the script path to `Jenkinsfile`
5. Configure credentials for Docker Hub and AWS in Jenkins credentials store
6. Run the pipeline

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
  <img src="https://github.com/user-attachments/assets/e8048026-a0ca-46c8-a2bf-7bf8aefe2300" width="300px" alt="Local application dashboard" />
  <img src="https://github.com/user-attachments/assets/1c5277ce-6f61-4b09-a4e2-debd175fda66" width="300px" alt="Local application data view" />
  <img src="https://github.com/user-attachments/assets/e6abcbd6-2cb8-43b7-b92e-a95583c0d79c" width="300px" alt="Local application interface" />
  
  <p><strong>User Creation in Postman</strong></p>
  <img src="https://github.com/user-attachments/assets/44acd76f-1d6a-450f-9cf9-293a61f5ad73" width="300px" alt="Postman user creation" />
  
  <p><strong>Application Login</strong></p>
  <img src="https://github.com/user-attachments/assets/c5edf8be-f992-43c1-9ab1-d992a930c5fc" width="300px" alt="Application login screen" />
  
  <p><strong>Docker Container Running</strong></p>
  <img src="https://github.com/user-attachments/assets/2b962f7d-189f-43ed-bfd5-21677ebf8ea3" width="300px" alt="Docker containers running" />
  
  <p><strong>Kubernetes Deployment</strong></p>
  <img src="https://github.com/user-attachments/assets/f2936ea3-d478-4097-ad8e-28c337f74b57" width="300px" alt="Kubernetes deployment status" />
  <img src="https://github.com/user-attachments/assets/c63bfe47-d9bb-49de-aab4-6486d4d43a9a" width="300px" alt="Kubernetes pods status" />
  <img src="https://github.com/user-attachments/assets/c0be9058-e054-4eae-bb65-cfb793c7b188" width="300px" alt="Kubernetes services status" />
  
  <p><strong>Jenkins Pipeline</strong></p>
  <img src="https://github.com/user-attachments/assets/8b0421c7-e382-4e4b-b16c-30cb039db2cc" width="300px" alt="Jenkins pipeline execution" />
  
  <p><strong>EKS Deployment</strong></p>
  <img src="https://github.com/user-attachments/assets/9aad0009-0bf1-4632-82d5-6db32787167e" width="300px" alt="EKS cluster status" />
  <img src="https://github.com/user-attachments/assets/bb9c95c1-ce18-4a0c-bb91-4846107e7a51" width="300px" alt="EKS deployment view" />
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
