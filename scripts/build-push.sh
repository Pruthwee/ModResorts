#!/bin/bash
set -e
set -o pipefail

PROJECT_NAME="ModResorts"
IMAGE_NAME=$(echo "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -cs 'a-z0-9' '-' | sed 's/^-*//;s/-*$//')

echo "=== ModResorts Build & Push ==="
echo "Project name: $PROJECT_NAME"
echo "Image name: $IMAGE_NAME"
echo ""

# Ask for target platform
echo "Select target platform:"
echo "1. AWS ECR"
echo "2. Docker Hub"
read -p "Enter choice (1 or 2): " PLATFORM_CHOICE

if [ "$PLATFORM_CHOICE" = "1" ]; then
    echo "=== AWS ECR ==="
    read -p "Enter AWS region (e.g., us-east-1): " AWS_REGION
    read -p "Enter ECR registry URL (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com): " REGISTRY_URL
    read -p "Enter ECR repository name: " ECR_REPO
    read -p "Enter image tag (or press Enter for 'latest'): " IMAGE_TAG

    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    # Sanitize tag
    IMAGE_TAG=$(echo "$IMAGE_TAG" | tr -cs 'a-z0-9-.' '-' | sed 's/^-*//;s/-*$//')
    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    FULL_IMAGE_NAME="$REGISTRY_URL/$ECR_REPO:$IMAGE_TAG"

    echo ""
    echo "Building image: $FULL_IMAGE_NAME"
    echo "Platform: AWS ECR"
    echo ""

    # Login to ECR
    echo "Logging in to AWS ECR..."
    aws ecr get-login-password --region "$AWS_REGION" | docker login --username AWS --password-stdin "$REGISTRY_URL"

    # Create repository if it doesn't exist
    echo "Checking if ECR repository exists..."
    aws ecr describe-repositories --repository-names "$ECR_REPO" --region "$AWS_REGION" >/dev/null 2>&1 || \
        aws ecr create-repository --repository-name "$ECR_REPO" --region "$AWS_REGION"

    # Build the image
    echo "Building Docker image..."
    docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .

    # Push the image
    echo "Pushing image to ECR..."
    docker push "$FULL_IMAGE_NAME"

    echo ""
    echo "=== Build & Push Complete ==="
    echo "Image: $FULL_IMAGE_NAME"
    echo "Registry: $REGISTRY_URL"

elif [ "$PLATFORM_CHOICE" = "2" ]; then
    echo "=== Docker Hub ==="
    read -p "Enter Docker Hub username: " DOCKER_USERNAME
    read -s -p "Enter Docker Hub password: " DOCKER_PASSWORD
    echo ""
    read -p "Enter repository name: " DOCKER_REPO
    read -p "Enter image tag (or press Enter for 'latest'): " IMAGE_TAG

    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    # Sanitize tag
    IMAGE_TAG=$(echo "$IMAGE_TAG" | tr -cs 'a-z0-9-.' '-' | sed 's/^-*//;s/-*$//')
    if [ -z "$IMAGE_TAG" ]; then
        IMAGE_TAG="latest"
    fi

    FULL_IMAGE_NAME="$DOCKER_USERNAME/$DOCKER_REPO:$IMAGE_TAG"

    echo ""
    echo "Building image: $FULL_IMAGE_NAME"
    echo "Platform: Docker Hub"
    echo ""

    # Login to Docker Hub
    echo "Logging in to Docker Hub..."
    echo "$DOCKER_PASSWORD" | docker login --username "$DOCKER_USERNAME" --password-stdin

    # Build the image
    echo "Building Docker image..."
    docker build -f Dockerfile -t "$FULL_IMAGE_NAME" .

    # Push the image
    echo "Pushing image to Docker Hub..."
    docker push "$FULL_IMAGE_NAME"

    echo ""
    echo "=== Build & Push Complete ==="
    echo "Image: $FULL_IMAGE_NAME"
    echo "Registry: Docker Hub"

else
    echo "Invalid choice. Exiting."
    exit 1
fi
