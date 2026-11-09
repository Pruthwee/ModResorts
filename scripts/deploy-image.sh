#!/bin/bash
set -e
set -o pipefail

PROJECT_NAME="ModResorts"
NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "=== Deploy Image to AWS EKS for $NAME ==="
echo ""

# Ask for AWS region and cluster name
read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION
read -p "Enter EKS cluster name: " CLUSTER_NAME
read -p "Enter image URI (full path with registry and tag, e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI

# Ask for environment variables
echo ""
echo "Enter values for environment variables (press Enter to skip):"
read -p "Enter value for DB_HOST (or press Enter to skip): " DB_HOST_VALUE
read -p "Enter value for DB_PORT (or press Enter to skip): " DB_PORT_VALUE
read -p "Enter value for DB_NAME (or press Enter to skip): " DB_NAME_VALUE
read -s -p "Enter value for DB_USER (or press Enter to skip): " DB_USER_VALUE
echo ""
read -s -p "Enter value for DB_PASSWORD (or press Enter to skip): " DB_PASSWORD_VALUE
echo ""
read -s -p "Enter value for WEATHER_API_KEY (or press Enter to skip): " WEATHER_API_KEY_VALUE
echo ""

# Configure kubectl for EKS
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME in region $AWS_REGION"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

# Check cluster connectivity
echo "Checking cluster connectivity..."
kubectl cluster-info || exit 1

# Apply namespace
echo "Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

# Create secrets
echo "Creating Kubernetes secrets..."
SECRET_ARGS=""
if [ -n "$WEATHER_API_KEY_VALUE" ]; then
    SECRET_ARGS="$SECRET_ARGS --from-literal=weather-api-key=$WEATHER_API_KEY_VALUE"
fi
if [ -n "$DB_USER_VALUE" ]; then
    SECRET_ARGS="$SECRET_ARGS --from-literal=db-user=$DB_USER_VALUE"
fi
if [ -n "$DB_PASSWORD_VALUE" ]; then
    SECRET_ARGS="$SECRET_ARGS --from-literal=db-password=$DB_PASSWORD_VALUE"
fi

if [ -n "$SECRET_ARGS" ]; then
    kubectl create secret generic "$NAME-secrets" -n "$NAME" $SECRET_ARGS --dry-run=client -o yaml | kubectl apply -f -
fi

# Replace placeholders in deployment.yaml
echo "Updating deployment manifest with image URI and environment variables..."
sed -i.bak "s|{{IMAGE_URI}}|$IMAGE_URI|g" kubernetes/deployment.yaml
sed -i.bak "s|{{DB_HOST}}|$DB_HOST_VALUE|g" kubernetes/deployment.yaml
sed -i.bak "s|{{DB_PORT}}|$DB_PORT_VALUE|g" kubernetes/deployment.yaml
sed -i.bak "s|{{DB_NAME}}|$DB_NAME_VALUE|g" kubernetes/deployment.yaml
rm -f kubernetes/deployment.yaml.bak

# Apply deployment, service, and ingress
echo "Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

echo "Applying service..."
kubectl apply -f kubernetes/service.yaml

echo "Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

# Wait for deployment rollout
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/"$NAME" -n "$NAME"

# Show deployment status
echo ""
echo "=== Deployment Status ==="
kubectl get pods,svc,ingress -n "$NAME"

# Show application URL
echo ""
echo "Application URL: https://$NAME.example.com"

echo ""
echo "=== Deployment Complete ==="
echo "If the deployment fails, you can rollback with:"
echo "  kubectl rollout undo deployment/$NAME -n $NAME"
