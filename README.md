# Learner Report Application

## Project Overview
This is a MERN (MongoDB, Express.js, React.js, Node.js) stack application for tracking and managing learner reports. The project demonstrates containerization, Docker integration, and Kubernetes deployment.

## Prerequisites
- Node.js (v16 recommended)
- Docker
- Kubernetes (Minikube recommended)
- kubectl
- MongoDB Atlas account

## Project Repositories
- Frontend: [GitHub Repository](https://github.com/UnpredictablePrashant/learnerReportCS_frontend)
- Backend: [GitHub Repository](https://github.com/UnpredictablePrashant/learnerReportCS_backend)

## Local Development Setup

### Clone Repositories
```bash
git clone https://github.com/UnpredictablePrashant/learnerReportCS_backend.git
git clone https://github.com/UnpredictablePrashant/learnerReportCS_frontend.git
```

### Backend Setup
1. Navigate to backend directory
2. Install dependencies:
```bash
npm install
```
3. Update `config.env` with correct MongoDB URL
4. Start the server:
```bash
node index.js
```
- Runs on PORT 3001

### Frontend Setup
1. Navigate to frontend directory
2. Install dependencies:
```bash
npm install
npm install --legacy-peer-deps
```
3. Start the application:
```bash
npm start
```
- Runs on PORT 3000

### Initial User Creation
- Use Postman to create an initial user in the MongoDB database for login functionality

## Dockerization

### Backend Dockerfile
- Base Image: node:16
- Exposes PORT 3001
- Installs dependencies
- Runs `node index.js`

### Frontend Dockerfile
- Base Image: node:16
- Uses multi-stage build for production
- Serves via Nginx
- Exposes PORT 80

### Building Docker Images
```bash
# Backend
docker build -t learnerreport:v3 .

# Frontend
docker build -t learnerreport:frontend .
```

### Docker Hub Deployment
Images pushed to Docker Hub: `aakashrawat1910/learnerreport`
```bash
# Login to Docker Hub
docker login

# Push Backend
docker tag learnerreport:v3 aakashrawat1910/learnerreport:backend
docker push aakashrawat1910/learnerreport:backend

# Push Frontend
docker tag learnerreport:frontend aakashrawat1910/learnerreport:frontend
docker push aakashrawat1910/learnerreport:frontend
```

## Kubernetes Deployment

### Deployment Architecture
- Separate namespaces for database, backend, and frontend
- Persistent Volume for MongoDB
- Secrets for configuration management
- LoadBalancer services for external access

### Kubernetes Components
1. **Database Namespace (`lrdatabase`)**
   - PersistentVolume (2Gi)
   - PersistentVolumeClaim
   - MongoDB Deployment
   - ClusterIP Service

2. **Backend Namespace (`lrbackend`)**
   - Deployment with 2 replicas
   - Secret for MongoDB URI
   - LoadBalancer Service
   - Port: 3001

3. **Frontend Namespace (`lrfrontend`)**
   - Deployment with 2 replicas
   - Secret for Backend URL
   - LoadBalancer Service
   - Served via Nginx
   - Port: 80

### Deployment Steps
```bash
# Create Namespaces
kubectl apply -f namespace.yml

# Apply Persistent Volumes
kubectl apply -f db-pv.yml
kubectl apply -f db-pvc.yml

# Deploy Database
kubectl apply -f deployment.yml
kubectl apply -f service.yml

# Deploy Backend
kubectl apply -f backend/secret.yml
kubectl apply -f backend/deployment.yml
kubectl apply -f backend/service.yml

# Deploy Frontend
kubectl apply -f frontend/secret.yml
kubectl apply -f frontend/deployment.yml
kubectl apply -f frontend/service.yml
```

## Troubleshooting
- Ensure MongoDB URL is correctly configured
- Use `--legacy-peer-deps` for npm install if facing dependency conflicts
- Check Kubernetes namespace and service configurations
- Verify Docker image tags match Kubernetes deployment configs

## Future Improvements
- Implement HELM Chart for easier deployment
- Add Jenkins CI/CD pipeline
- Enhanced error handling and logging

## Contributing
Please document any changes, challenges, and solutions encountered during development.

## License
Educational assignment project
