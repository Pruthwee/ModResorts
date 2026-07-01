#!/bin/bash
set -e
set -o pipefail

echo "-------------------------------------------------------"
echo "ModResorts EKS Deployment Script"
echo "-------------------------------------------------------"

# Prompt for AWS and EKS details
read -p "Enter AWS Region [us-east-1]: " AWS_REGION
AWS_REGION=${AWS_REGION:-us-east-1}
read -p "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
    echo "Error: Cluster name is required."
    exit 1
fi

# Prompt for Docker Image URI
read -p "Enter Docker Image URI (e.g., account.dkr.ecr.region.amazonaws.com/repo:tag): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
    echo "Error: Image URI is required."
    exit 1
fi

# Configure kubectl
echo "Configuring kubectl for EKS cluster..."
aws eks update-kubeconfig --region $AWS_REGION --name $CLUSTER_NAME

# Verify connectivity
echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "Error: Could not connect to EKS cluster"; exit 1; }

# Update manifests
echo "Updating Kubernetes manifests..."
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" kubernetes/deployment.yaml

# Apply manifests in order
echo "Applying manifests..."
kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

# Wait for rollout
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/modresorts -n modresorts

# Verify resources
echo "Verifying resources..."
kubectl get pods,svc,ingress -n modresorts

echo "-------------------------------------------------------"
echo "Deployment complete!"
echo "Application URL: http://modresorts.example.com"
echo "-------------------------------------------------------"
