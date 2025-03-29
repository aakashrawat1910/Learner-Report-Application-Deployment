pipeline {
    agent any

    environment {
        DOCKER_CREDS = 'aakash-dockerhub'
        DOCKER_REG = 'aakashrawat1910'
        GIT_URL = 'https://github.com/aakashrawat1910/Learner-Report-Application-Deployment.git'
        GIT_BRANCH = 'main'
        AWS_ACCESS_KEY_ID = 'AWS_ACCESS_KEY_ID' // AWS Access Key ID stored in Jenkins
        AWS_SECRET_ACCESS_KEY = 'AWS_SECRET_ACCESS_KEY' // AWS Secret Access Key stored in Jenkins
        AWS_REGION = 'us-west-1' // AWS region where the cluster is located
        CLUSTER_NAME = 'aakash-eksctl-learnerapplication' // EKS cluster name
    }

    stages {
        stage('Checkout Code') {
            steps {
                echo 'Checking out code from GitHub...'
                git url: "${env.GIT_URL}", branch: "${env.GIT_BRANCH}"
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    echo 'Building Docker images...'
                    def backendImage = docker.build("${DOCKER_REG}/learnerreport:backend", "Backend")
                    def frontendImage = docker.build("${DOCKER_REG}/learnerreport:frontendforkubernetes", "Frontend")
                }
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
                    aws eks update-kubeconfig --region ${AWS_REGION} --name ${CLUSTER_NAME}
                    
                    """
                }
            }
        }
                 
        stage('Deploy Backend to Kubernetes') {
            steps {
                script {
                    echo 'Deploying Backend to Kubernetes...'
                    sh """
                    
                    kubectl apply -f './Kubernetes Deployment/backend/deployment.yml'
                    
                    """
                }
            }
        }

        stage('Deploy Frontend to Kubernetes') {
            steps {
                script {
                    echo 'Deploying Frontend to Kubernetes...'
                    sh """
                    kubectl apply -f './Kubernetes Deployment/frontend/deployment.yml'
                    """
                }
            }
        }
    }
}