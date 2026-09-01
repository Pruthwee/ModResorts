#!/bin/bash
set -e
set -o pipefail

echo "============================================"
echo "  ModResorts - Deploy to AWS EKS"
echo "============================================"
echo ""

read -p "Enter AWS Region (e.g. us-east-1): " AWS_REGION
if [ -z "$AWS_REGION" ]; then
  echo "ERROR: AWS Region is required."
  exit 1
fi

read -p "Enter EKS Cluster Name: " CLUSTER_NAME
if [ -z "$CLUSTER_NAME" ]; then
  echo "ERROR: EKS Cluster Name is required."
  exit 1
fi

read -p "Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): " IMAGE_URI
if [ -z "$IMAGE_URI" ]; then
  echo "ERROR: Docker image URI is required."
  exit 1
fi

echo ""
echo "--- Optional Application Environment Variables ---"
echo "(Press Enter to skip any variable)"
echo ""

read -p "Enter value for WEATHER_API_KEY (or press Enter to skip): " WEATHER_API_KEY_VAL
read -p "Enter value for REDIS_HOST (e.g. my-redis.abc123.ng.0001.use1.cache.amazonaws.com, or press Enter to skip): " REDIS_HOST_VAL
read -p "Enter value for REDIS_PORT (default: 6379, or press Enter to skip): " REDIS_PORT_VAL
read -p "Enter value for SERVICE_DISCOVERY_URL (or press Enter to skip): " SERVICE_DISCOVERY_URL_VAL

echo ""
echo "Configuring kubectl for EKS cluster: ${CLUSTER_NAME} in region: ${AWS_REGION}"
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || { echo "ERROR: Cannot connect to EKS cluster. Check your credentials and cluster name."; exit 1; }

echo ""
echo "Updating Kubernetes manifests with image URI and environment variables..."

cp kubernetes/deployment.yaml kubernetes/deployment.yaml.bak

sed -i "s|{{IMAGE_URI}}|${IMAGE_URI}|g" kubernetes/deployment.yaml

if [ -n "$WEATHER_API_KEY_VAL" ]; then
  sed -i "s|{{WEATHER_API_KEY}}|${WEATHER_API_KEY_VAL}|g" kubernetes/deployment.yaml
else
  sed -i "s|{{WEATHER_API_KEY}}||g" kubernetes/deployment.yaml
fi

if [ -n "$REDIS_HOST_VAL" ]; then
  sed -i "s|{{REDIS_HOST}}|${REDIS_HOST_VAL}|g" kubernetes/deployment.yaml
else
  sed -i "s|{{REDIS_HOST}}|localhost|g" kubernetes/deployment.yaml
fi

if [ -n "$REDIS_PORT_VAL" ]; then
  sed -i "s|{{REDIS_PORT}}|${REDIS_PORT_VAL}|g" kubernetes/deployment.yaml
else
  sed -i "s|{{REDIS_PORT}}|6379|g" kubernetes/deployment.yaml
fi

if [ -n "$SERVICE_DISCOVERY_URL_VAL" ]; then
  sed -i "s|{{SERVICE_DISCOVERY_URL}}|${SERVICE_DISCOVERY_URL_VAL}|g" kubernetes/deployment.yaml
else
  sed -i "s|{{SERVICE_DISCOVERY_URL}}||g" kubernetes/deployment.yaml
fi

echo ""
echo "Applying Kubernetes manifests..."

echo "  [1/4] Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

echo "  [2/4] Applying deployment..."
kubectl apply -f kubernetes/deployment.yaml

echo "  [3/4] Applying service..."
kubectl apply -f kubernetes/service.yaml

echo "  [4/4] Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/modresorts -n modresorts --timeout=300s

echo ""
echo "Verifying deployed resources..."
kubectl get pods,svc,ingress -n modresorts

echo ""
echo "Retrieving application URL..."
INGRESS_HOST=$(kubectl get ingress modresorts-ingress -n modresorts -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || echo "pending")
if [ "$INGRESS_HOST" != "pending" ] && [ -n "$INGRESS_HOST" ]; then
  echo "Application URL: http://${INGRESS_HOST}"
else
  echo "Ingress hostname is still provisioning. Run the following to check:"
  echo "  kubectl get ingress modresorts-ingress -n modresorts"
fi

echo ""
echo "Restoring original deployment manifest..."
mv kubernetes/deployment.yaml.bak kubernetes/deployment.yaml

echo ""
echo "============================================"
echo "  Deployment complete!"
echo "  Namespace: modresorts"
echo "  Image: ${IMAGE_URI}"
echo "============================================"
echo ""
echo "Rollback command (if needed):"
echo "  kubectl rollout undo deployment/modresorts -n modresorts"
