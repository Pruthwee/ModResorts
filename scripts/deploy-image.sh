#!/bin/bash
set -e
set -o pipefail

PROJECT_NAME="ModResorts"
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "=== ModResorts Deploy to AWS EKS ==="
echo "Project name: $PROJECT_NAME"
echo "Image name: $IMAGE_NAME"
echo ""

# Ask for AWS region
read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION

# Ask for EKS cluster name
read -p "Enter EKS cluster name: " CLUSTER_NAME

# Ask for image URI
read -p "Enter full image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI

# Ask for environment variables
echo ""
echo "Enter values for environment variables (or press Enter to skip):"
echo ""

read -p "Enter value for WEATHER_API_KEY (or press Enter to skip): " WEATHER_API_KEY_VALUE

# Configure kubectl for EKS
echo ""
echo "Configuring kubectl for EKS cluster..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

# Check cluster connectivity
echo "Checking cluster connectivity..."
kubectl cluster-info || exit 1

# Apply namespace
echo "Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

# Create Secret for credentials
echo "Creating Kubernetes Secret for credentials..."
SECRET_ARGS=""
if [ -n "$WEATHER_API_KEY_VALUE" ]; then
    SECRET_ARGS="$SECRET_ARGS --from-literal=WEATHER_API_KEY=$WEATHER_API_KEY_VALUE"
fi

if [ -n "$SECRET_ARGS" ]; then
    kubectl create secret generic modresorts-secrets -n modresorts $SECRET_ARGS --dry-run=client -o yaml | kubectl apply -f -
fi

# Replace placeholders in deployment.yaml
echo "Updating deployment.yaml with image URI..."
sed -i.bak 's|{{IMAGE_URI}}|'$IMAGE_URI'|g' kubernetes/deployment.yaml

# Remove backup files
rm -f kubernetes/deployment.yaml.bak

# Apply deployment
echo "Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

# Apply service
echo "Applying service..."
kubectl apply -f kubernetes/service.yaml

# Apply ingress
echo "Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

# Wait for deployment rollout
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/modresorts -n modresorts

# Show pods, services, and ingress
echo ""
echo "=== Deployment Status ==="
kubectl get pods,svc,ingress -n modresorts

# Show application URL
echo ""
echo "=== Application URL ==="
echo "https://modresorts.example.com"

echo ""
echo "=== Deployment Complete ==="
echo "To rollback: kubectl rollout undo deployment/modresorts -n modresorts"
