#!/bin/bash
set -e

# Project name
PROJECT_NAME="ModResorts"

# Sanitize the project name for use as a Docker image name
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

# Ask for the target platform
echo "Choose the target platform:"
echo "1. AWS ECR"
echo "2. Docker Hub"
read -p "Enter choice (1 or 2): " PLATFORM_CHOICE

# Ask for IMAGE_TAG first
read -p "Enter image tag (press Enter for 'latest'): " IMAGE_TAG

if [ -z "$IMAGE_TAG" ]; then
    IMAGE_TAG="latest"
fi

# Sanitize tag
IMAGE_TAG=$(echo "$IMAGE_TAG" | tr -cs 'a-z0-9._-' '-' | sed 's/^-*//;s/-*$//')

if [ "$PLATFORM_CHOICE" = "1" ]; then
    # AWS ECR
    read -p "Enter AWS region: " AWS_REGION
    read -p "Enter ECR registry URL (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com): " REGISTRY_URL
    read -p "Enter ECR repository name: " ECR_REPO

    FULL_IMAGE_NAME="$REGISTRY_URL/$ECR_REPO:$IMAGE_TAG"

    echo "Logging in to Amazon ECR..."
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY_URL"

    echo "Checking if ECR repository exists..."
    aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"

    echo "Building Docker image..."
    docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .

    echo "Pushing Docker image to ECR..."
    docker push "$FULL_IMAGE_NAME"

    echo "Done! Image pushed to: $FULL_IMAGE_NAME"
elif [ "$PLATFORM_CHOICE" = "2" ]; then
    # Docker Hub
    read -p "Enter Docker Hub username: " DOCKER_USERNAME

    FULL_IMAGE_NAME="$DOCKER_USERNAME/$IMAGE_NAME:$IMAGE_TAG"

    echo "Logging in to Docker Hub..."
    read -s -p "Enter Docker Hub password: " DOCKER_PASSWORD
    echo ""
    echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

    echo "Building Docker image..."
    docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .

    echo "Pushing Docker image to Docker Hub..."
    docker push "$FULL_IMAGE_NAME"

    echo "Done! Image pushed to: $FULL_IMAGE_NAME"
else
    echo "Invalid choice. Exiting."
    exit 1
fi
