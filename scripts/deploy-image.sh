#!/bin/bash
set -e
set -o pipefail

PROJECT_NAME="ModResorts"
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "=== Deploying $IMAGE_NAME to AWS EKS ==="

# Ask for AWS region
read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION

# Ask for EKS cluster name
read -p "Enter EKS cluster name: " CLUSTER_NAME

# Ask for image URI
read -p "Enter full image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI

# Collect environment variables
echo ""
echo "Enter environment variable values for the deployment."
echo "Press Enter to skip any variable (it will remain as a placeholder)."
echo ""

read -p "Enter value for SERVER_DISPLAY_NAME (or press Enter to skip): " SERVER_DISPLAY_NAME
read -p "Enter value for SERVER_FULL_NAME (or press Enter to skip): " SERVER_FULL_NAME

# Collect credentials (without echoing)
echo ""
echo "Enter credential values for the Kubernetes Secret."
read -s -p "Enter WEATHER_API_KEY: " WEATHER_API_KEY
echo ""

# Configure kubectl for the EKS cluster
echo ""
echo "Configuring kubectl for EKS cluster..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

# Verify cluster access
echo "Verifying cluster access..."
kubectl cluster-info || exit 1

# Apply namespace
echo ""
echo "Applying namespace..."
sed -i.bak 's|{{NAMESPACE}}|'$IMAGE_NAME'|g' kubernetes/namespace.yaml
rm -f kubernetes/namespace.yaml.bak
kubectl apply -f kubernetes/namespace.yaml

# Create Secret with credentials
echo ""
echo "Creating Kubernetes Secret for credentials..."
kubectl create secret generic ${IMAGE_NAME}-secrets -n ${IMAGE_NAME} \
    --from-literal=weather-api-key="$WEATHER_API_KEY" \
    --dry-run=client -o yaml | kubectl apply -f -

# Prepare deployment.yaml with placeholder replacements
echo ""
echo "Preparing deployment.yaml..."
sed -i.bak 's|{{NAMESPACE}}|'$IMAGE_NAME'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{IMAGE_URI}}|'$IMAGE_URI'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{SERVER_DISPLAY_NAME}}|'$SERVER_DISPLAY_NAME'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{SERVER_FULL_NAME}}|'$SERVER_FULL_NAME'|g' kubernetes/deployment.yaml
rm -f kubernetes/deployment.yaml.bak

# Apply deployment
echo ""
echo "Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

# Apply service
echo ""
echo "Applying service..."
sed -i.bak 's|{{NAMESPACE}}|'$IMAGE_NAME'|g' kubernetes/service.yaml
rm -f kubernetes/service.yaml.bak
kubectl apply -f kubernetes/service.yaml

# Apply ingress
echo ""
echo "Applying ingress..."
sed -i.bak 's|{{NAMESPACE}}|'$IMAGE_NAME'|g' kubernetes/ingress.yaml
rm -f kubernetes/ingress.yaml.bak
kubectl apply -f kubernetes/ingress.yaml

# Wait for deployment rollout
echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/${IMAGE_NAME} -n ${IMAGE_NAME}

# Show resources
echo ""
echo "=== Deployment Status ==="
kubectl get pods,svc,ingress -n ${IMAGE_NAME}

# Show application URL
echo ""
echo "=== Application URL ==="
echo "http://${IMAGE_NAME}.example.com"

echo ""
echo "=== Deployment Complete ==="
echo "If you need to rollback, run: kubectl rollout undo deployment/${IMAGE_NAME} -n ${IMAGE_NAME}"
