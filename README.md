# Learner Report Application Deployment

## Project Overview
This is a MERN (MongoDB, Express.js, React.js, Node.js) stack application for tracking and managing learner reports. The project demonstrates containerization, Docker integration, and preparatory steps for Kubernetes deployment.

## Prerequisites
- Node.js (v16 recommended)
- Docker
- npm
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
- Exposes PORT 3000
- Installs dependencies with `--legacy-peer-deps`
- Runs `npm start`

### Building Docker Images
```bash
# Backend
docker build -t learnerreport:v3 .

# Frontend
docker build -t learnerreport:frontend .
```

### Running Docker Containers
```bash
# Backend
docker run -p 3001:3001 learnerreport:v3

# Frontend
docker run -p 3000:3000 learnerreport:frontend
```

## Docker Hub Deployment
Images are pushed to Docker Hub under the repository `aakashrawat1910/learnerreport`
```bash
# Login to Docker Hub
docker login

# Tag and Push Backend
docker tag learnerreport:v3 aakashrawat1910/learnerreport:backend
docker push aakashrawat1910/learnerreport:backend

# Tag and Push Frontend
docker tag learnerreport:frontend aakashrawat1910/learnerreport:frontend
docker push aakashrawat1910/learnerreport:frontend
```

## Upcoming Features
- Kubernetes Deployment Files
- HELM Chart
- Jenkins CI/CD Pipeline

## Troubleshooting
- Ensure MongoDB URL is correctly configured
- Use `--legacy-peer-deps` for npm install if facing dependency conflicts
- Check network configurations when running in containers

## Contributing
Please read the assignment guidelines and document any changes or improvements.

## License
This project is part of an educational assignment.
