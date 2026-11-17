@echo off
setlocal enabledelayedexpansion

set "PROJECT_NAME=ModResorts"
REM Sanitize IMAGE_NAME: lowercase, replace non-alphanumeric with hyphens
set "IMAGE_NAME=modresorts"

echo === ModResorts Build & Push ===
echo Project name: %PROJECT_NAME%
echo Image name: %IMAGE_NAME%
echo.

REM Ask for target platform
echo Select target platform:
echo 1. AWS ECR
echo 2. Docker Hub
set /p PLATFORM_CHOICE="Enter choice (1 or 2): "

if "%PLATFORM_CHOICE%"=="1" (
    echo === AWS ECR ===
    set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "
    set /p REGISTRY_URL="Enter ECR registry URL (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com): "
    set /p ECR_REPO="Enter ECR repository name: "
    set /p IMAGE_TAG="Enter image tag (or press Enter for 'latest'): "

    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    REM Sanitize tag
    set "IMAGE_TAG=!IMAGE_TAG: =!"
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    set "FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!:!IMAGE_TAG!"

    echo.
    echo Building image: !FULL_IMAGE_NAME!
    echo Platform: AWS ECR
    echo.

    REM Login to ECR
    echo Logging in to AWS ECR...
    aws ecr get-login-password --region !AWS_REGION! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ECR login failed
        exit /b 1
    )

    REM Create repository if it doesn't exist
    echo Checking if ECR repository exists...
    aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository...
        aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
    )

    REM Build the image
    echo Building Docker image...
    docker build -f Dockerfile -t "!FULL_IMAGE_NAME!" .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    REM Push the image
    echo Pushing image to ECR...
    docker push "!FULL_IMAGE_NAME!"
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo.
    echo === Build ^& Push Complete ===
    echo Image: !FULL_IMAGE_NAME!
    echo Registry: !REGISTRY_URL!

) else if "%PLATFORM_CHOICE%"=="2" (
    echo === Docker Hub ===
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password: "
    set /p DOCKER_REPO="Enter repository name: "
    set /p IMAGE_TAG="Enter image tag (or press Enter for 'latest'): "

    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    REM Sanitize tag
    set "IMAGE_TAG=!IMAGE_TAG: =!"
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    set "FULL_IMAGE_NAME=!DOCKER_USERNAME!/!DOCKER_REPO!:!IMAGE_TAG!"

    echo.
    echo Building image: !FULL_IMAGE_NAME!
    echo Platform: Docker Hub
    echo.

    REM Login to Docker Hub
    echo Logging in to Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username "!DOCKER_USERNAME!" --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo Docker login failed
        exit /b 1
    )

    REM Build the image
    echo Building Docker image...
    docker build -f Dockerfile -t "!FULL_IMAGE_NAME!" .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    REM Push the image
    echo Pushing image to Docker Hub...
    docker push "!FULL_IMAGE_NAME!"
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo.
    echo === Build ^& Push Complete ===
    echo Image: !FULL_IMAGE_NAME!
    echo Registry: Docker Hub

) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

endlocal
