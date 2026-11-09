#!/bin/bash
set -e

PROJECT_NAME="ModResorts"
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "=== Build and Push Script for $IMAGE_NAME ==="
echo ""

# Ask for target platform
echo "Choose target platform:"
echo "1. AWS ECR (Amazon Elastic Container Registry)"
echo "2. Docker Hub"
echo ""
read -p "Enter choice (1 or 2): " PLATFORM_CHOICE

if [ "$PLATFORM_CHOICE" = "1" ]; then
    # AWS ECR
    read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION
    read -p "Enter AWS account ID: " AWS_ACCOUNT_ID

    REGISTRY_URL="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
    ECR_REPO="$IMAGE_NAME"
    FULL_IMAGE_NAME="${REGISTRY_URL}/${ECR_REPO}"

    read -p "Enter image tag (press Enter for 'latest'): " IMAGE_TAG
    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    # Login to ECR
    echo "Logging in to Amazon ECR..."
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY_URL"

    # Create repository if it doesn't exist
    echo "Checking if ECR repository exists..."
    aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || \
        aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"

    # Build and push
    echo "Building Docker image: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"
    docker build -f Dockerfile -t "${FULL_IMAGE_NAME}:${IMAGE_TAG}" .

    echo "Pushing Docker image: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"
    docker push "${FULL_IMAGE_NAME}:${IMAGE_TAG}"

    echo "Successfully built and pushed: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"

elif [ "$PLATFORM_CHOICE" = "2" ]; then
    # Docker Hub
    read -p "Enter Docker Hub username: " DOCKER_USERNAME
    read -s -p "Enter Docker Hub password: " DOCKER_PASSWORD
    echo ""

    read -p "Enter image tag (press Enter for 'latest'): " IMAGE_TAG
    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    FULL_IMAGE_NAME="docker.io/${DOCKER_USERNAME}/${IMAGE_NAME}"

    # Login to Docker Hub
    echo "Logging in to Docker Hub..."
    echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

    # Build and push
    echo "Building Docker image: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"
    docker build -f Dockerfile -t "${FULL_IMAGE_NAME}:${IMAGE_TAG}" .

    echo "Pushing Docker image: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"
    docker push "${FULL_IMAGE_NAME}:${IMAGE_TAG}"

    echo "Successfully built and pushed: ${FULL_IMAGE_NAME}:${IMAGE_TAG}"

else
    echo "Invalid choice. Exiting."
    exit 1
fi
