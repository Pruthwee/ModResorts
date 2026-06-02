#!/bin/bash
set -e

echo "=== ModResorts - Build and Push Docker Image (Linux/macOS) ==="

read -rp "Enter project name [ModResorts]: " PROJECT_NAME
PROJECT_NAME=${PROJECT_NAME:-ModResorts}

# Sanitize image name: lowercase, alphanumeric and hyphen only, trim hyphens
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')
if [ -z "$IMAGE_NAME" ]; then
  IMAGE_NAME="modresorts"
fi

echo "Select container registry type:"
echo "  1) Azure Container Registry (ACR)"
echo "  2) Docker Hub"
read -rp "Enter choice [1/2]: " REGISTRY_CHOICE

REGISTRY_URL=""
REPOSITORY=""

if [ "$REGISTRY_CHOICE" = "1" ]; then
  read -rp "Enter Azure ACR name (e.g., myregistry): " ACR_NAME
  if [ -z "$ACR_NAME" ]; then
    echo "ACR name is required." >&2
    exit 1
  fi
  REGISTRY_URL="${ACR_NAME}.azurecr.io"
  REPOSITORY="$IMAGE_NAME"
  echo "Logging in to Azure ACR..."
  az acr login --name "$ACR_NAME"
elif [ "$REGISTRY_CHOICE" = "2" ]; then
  read -rp "Enter Docker Hub username: " DOCKER_USERNAME
  if [ -z "$DOCKER_USERNAME" ]; then
    echo "Docker Hub username is required." >&2
    exit 1
  fi
  REGISTRY_URL="index.docker.io"
  REPOSITORY="${DOCKER_USERNAME}/${IMAGE_NAME}"
  echo "Logging in to Docker Hub..."
  read -srp "Enter Docker Hub password: " DOCKER_PASSWORD
  echo
  echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin
else
  echo "Invalid registry choice." >&2
  exit 1
fi

read -rp "Enter image tag [latest]: " IMAGE_TAG
IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_TAG=$(echo "$IMAGE_TAG" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')
if [ -z "$IMAGE_TAG" ]; then
  IMAGE_TAG="latest"
fi

FULL_IMAGE_NAME="${REPOSITORY}:${IMAGE_TAG}"

echo "Building Docker image: ${FULL_IMAGE_NAME}"
docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .

echo "Pushing Docker image: ${FULL_IMAGE_NAME}"
docker push "$FULL_IMAGE_NAME"

echo "Build and push completed successfully."
