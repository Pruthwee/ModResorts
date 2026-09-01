#!/bin/bash
set -e

PROJECT_NAME="modresorts"
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "============================================"
echo "  ModResorts - Docker Build & Push Script"
echo "============================================"
echo ""
echo "Select container registry:"
echo "  1. AWS ECR"
echo "  2. Docker Hub"
echo ""
read -p "Enter choice [1 or 2]: " REGISTRY_CHOICE

read -p "Enter image tag (default: latest): " IMAGE_TAG_INPUT
IMAGE_TAG=$(echo "$IMAGE_TAG_INPUT" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9._-' '-' | sed 's/^-*//;s/-*$//')
if [ -z "$IMAGE_TAG" ]; then
  IMAGE_TAG="latest"
fi

if [ "$REGISTRY_CHOICE" = "1" ]; then
  echo ""
  echo "--- AWS ECR Configuration ---"
  read -p "Enter AWS Region (e.g. us-east-1): " AWS_REGION
  read -p "Enter AWS Account ID: " AWS_ACCOUNT_ID
  read -p "Enter ECR Repository name (default: ${IMAGE_NAME}): " ECR_REPO_INPUT
  ECR_REPO="${ECR_REPO_INPUT:-$IMAGE_NAME}"
  REGISTRY_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
  FULL_IMAGE_NAME="${REGISTRY_URL}/${ECR_REPO}:${IMAGE_TAG}"

  echo ""
  echo "Logging in to AWS ECR..."
  aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY_URL"

  echo "Checking if ECR repository exists..."
  aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || \
    aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"

elif [ "$REGISTRY_CHOICE" = "2" ]; then
  echo ""
  echo "--- Docker Hub Configuration ---"
  read -p "Enter Docker Hub username: " DOCKER_USERNAME
  read -sp "Enter Docker Hub password or access token: " DOCKER_PASSWORD
  echo ""
  read -p "Enter Docker Hub repository (default: ${DOCKER_USERNAME}/${IMAGE_NAME}): " DOCKER_REPO_INPUT
  DOCKER_REPO="${DOCKER_REPO_INPUT:-${DOCKER_USERNAME}/${IMAGE_NAME}}"
  FULL_IMAGE_NAME="${DOCKER_REPO}:${IMAGE_TAG}"

  echo ""
  echo "Logging in to Docker Hub..."
  echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

else
  echo "Invalid choice. Exiting."
  exit 1
fi

echo ""
echo "Building Docker image: ${FULL_IMAGE_NAME}"
docker build -f Dockerfile -t "${FULL_IMAGE_NAME}" .

echo ""
echo "Pushing image: ${FULL_IMAGE_NAME}"
docker push "${FULL_IMAGE_NAME}"

echo ""
echo "============================================"
echo "  Build and push complete!"
echo "  Image: ${FULL_IMAGE_NAME}"
echo "============================================"
