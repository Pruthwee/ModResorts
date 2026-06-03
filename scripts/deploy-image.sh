#!/bin/bash
set -e
set -o pipefail

echo "=== Deploy ModResorts Image to Azure AKS (Linux/macOS) ==="

read -rp "Enter Azure resource group name: " RESOURCE_GROUP
if [ -z "$RESOURCE_GROUP" ]; then
  echo "Resource group is required" >&2
  exit 1
fi

read -rp "Enter Azure AKS cluster name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "AKS cluster name is required" >&2
  exit 1
fi

read -rp "Enter full Docker image URI (e.g., myregistry.azurecr.io/modresorts:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "Image URI is required" >&2
  exit 1
fi

read -rp "Enter DATABASE_URL (or press Enter to skip): " DATABASE_URL
read -rp "Enter API_KEY (or press Enter to skip): " API_KEY

# Configure kubectl for the AKS cluster
echo "Configuring kubectl for AKS cluster..."
az aks get-credentials --resource-group "$RESOURCE_GROUP" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info > /dev/null

echo "Updating Kubernetes manifests with image and environment variables..."

# Replace placeholders using sed with pipe delimiter
sed -i "s|{{IMAGE_URI}}|$IMAGE_URI|g" kubernetes/deployment.yaml

if [ -n "$DATABASE_URL" ]; then
  sed -i "s|{{DATABASE_URL}}|$DATABASE_URL|g" kubernetes/deployment.yaml
else
  sed -i "s|{{DATABASE_URL}}||g" kubernetes/deployment.yaml
fi

if [ -n "$API_KEY" ]; then
  sed -i "s|{{API_KEY}}|$API_KEY|g" kubernetes/deployment.yaml
else
  sed -i "s|{{API_KEY}}||g" kubernetes/deployment.yaml
fi

echo "Applying Kubernetes manifests..."

kubectl apply -f kubernetes/namespace.yaml
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml
kubectl apply -f kubernetes/ingress.yaml

echo "Waiting for deployment rollout..."
kubectl rollout status deployment/modresorts -n modresorts

echo "Current resources in namespace 'modresorts':"
kubectl get pods,svc,ingress -n modresorts

echo "If using the sample ingress, the application should be available (once DNS is configured) at:"
echo "  http://modresorts.example.com/"

echo "Deployment completed successfully."
