#!/bin/bash
set -e
set -o pipefail

# Project name
PROJECT_NAME="modresorts"

# Ask for AWS region and EKS cluster name
read -p "Enter AWS region: " AWS_REGION
read -p "Enter EKS cluster name: " CLUSTER_NAME

# Ask for the image URI
read -p "Enter full image URI (registry/repository:tag): " IMAGE_URI

# Create an array to hold secret key=value pairs
declare -a SECRET_KEYS
declare -a SECRET_VALUES

# Ask for environment variables
echo ""
echo "Enter environment variable values for the application."
echo "Press Enter to skip (use placeholder default)."
echo ""

# WEATHER_API_KEY (credential - goes to Secret)
read -s -p "Enter WEATHER_API_KEY (or press Enter to skip): " WEATHER_API_KEY
echo ""
if [ -n "$WEATHER_API_KEY" ]; then
    SECRET_KEYS+=("weather-api-key")
    SECRET_VALUES+=("$WEATHER_API_KEY")
fi

# NAMING_PROVIDER_URL
read -p "Enter NAMING_PROVIDER_URL (or press Enter to skip): " NAMING_PROVIDER_URL

# NAMING_FACTORY_INITIAL
read -p "Enter NAMING_FACTORY_INITIAL (or press Enter to skip): " NAMING_FACTORY_INITIAL

# DATASOURCE_JNDI
read -p "Enter DATASOURCE_JNDI (or press Enter to skip): " DATASOURCE_JNDI

echo ""
echo "Configuring kubectl for EKS cluster: $CLUSTER_NAME in region $AWS_REGION..."
aws eks update-kubeconfig --region "$AWS_REGION" --name "$CLUSTER_NAME"

echo "Verifying cluster connectivity..."
kubectl cluster-info || exit 1

echo ""
echo "Applying namespace..."
kubectl apply -f kubernetes/namespace.yaml

# Create Secret for credentials
if [ ${#SECRET_KEYS[@]} -gt 0 ]; then
    echo ""
    echo "Creating Kubernetes Secret for credentials..."
    SECRET_ARGS=""
    for i in "${!SECRET_KEYS[@]}"; do
        SECRET_ARGS="$SECRET_ARGS --from-literal=${SECRET_KEYS[$i]}=${SECRET_VALUES[$i]}"
    done
    kubectl create secret generic ${PROJECT_NAME}-secrets -n ${PROJECT_NAME} $SECRET_ARGS --dry-run=client -o yaml | kubectl apply -f -
fi

echo ""
echo "Applying deployment..."
# Create a temporary copy of deployment.yaml with placeholders replaced
sed -i.bak 's|{{IMAGE_URI}}|'$IMAGE_URI'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{NAMING_PROVIDER_URL}}|'"${NAMING_PROVIDER_URL:-}"'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{NAMING_FACTORY_INITIAL}}|'"${NAMING_FACTORY_INITIAL:-}"'|g' kubernetes/deployment.yaml
sed -i.bak 's|{{DATASOURCE_JNDI}}|'"${DATASOURCE_JNDI:-}"'|g' kubernetes/deployment.yaml
rm -f kubernetes/deployment.yaml.bak

kubectl apply -f kubernetes/deployment.yaml

echo ""
echo "Applying service..."
kubectl apply -f kubernetes/service.yaml

echo ""
echo "Applying ingress..."
kubectl apply -f kubernetes/ingress.yaml

echo ""
echo "Waiting for deployment rollout..."
kubectl rollout status deployment/${PROJECT_NAME} -n ${PROJECT_NAME}

echo ""
echo "Deployment details:"
kubectl get pods,svc,ingress -n ${PROJECT_NAME}

echo ""
echo "Application URL:"
kubectl get ingress ${PROJECT_NAME}-ingress -n ${PROJECT_NAME} -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

echo ""
echo ""
echo "Deployment complete!"
echo ""
echo "To rollback if needed:"
echo "  kubectl rollout undo deployment/${PROJECT_NAME} -n ${PROJECT_NAME}"
echo "  kubectl rollout history deployment/${PROJECT_NAME} -n ${PROJECT_NAME}"
