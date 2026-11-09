@echo off
setlocal enabledelayedexpansion

set "PROJECT_NAME=ModResorts"
for /f "usebackq delims=" %%i in (`powershell -Command "$p = '%PROJECT_NAME%' -creplace '[^a-zA-Z0-9]', '-'; $p = $p.ToLower(); $p = $p.Trim('-'); Write-Output $p"`) do set "IMAGE_NAME=%%i"

echo === Build and Push Script for !IMAGE_NAME! ===
echo.

echo Choose target platform:
echo 1. AWS ECR (Amazon Elastic Container Registry)
echo 2. Docker Hub
echo.
set /p PLATFORM_CHOICE="Enter choice (1 or 2): "

if "!PLATFORM_CHOICE!"=="1" (
    set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "
    set /p AWS_ACCOUNT_ID="Enter AWS account ID: "

    set "REGISTRY_URL=!AWS_ACCOUNT_ID!.dkr.ecr.!AWS_REGION!.amazonaws.com"
    set "ECR_REPO=!IMAGE_NAME!"
    set "FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!"

    set /p IMAGE_TAG="Enter image tag (press Enter for 'latest'): "
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    echo Logging in to Amazon ECR...
    for /f "usebackq delims=" %%p in (`aws ecr get-login-password --region !AWS_REGION!`) do set "ECR_PASSWORD=%%p"
    echo !ECR_PASSWORD! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ECR login failed
        exit /b 1
    )

    echo Checking if ECR repository exists...
    aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository...
        aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
        if !ERRORLEVEL! neq 0 (
            echo Failed to create ECR repository
            exit /b 1
        )
    )

    echo Building Docker image: !FULL_IMAGE_NAME!:!IMAGE_TAG!
    docker build -f Dockerfile -t "!FULL_IMAGE_NAME!:!IMAGE_TAG!" .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    echo Pushing Docker image: !FULL_IMAGE_NAME!:!IMAGE_TAG!
    docker push "!FULL_IMAGE_NAME!:!IMAGE_TAG!"
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo Successfully built and pushed: !FULL_IMAGE_NAME!:!IMAGE_TAG!
) else if "!PLATFORM_CHOICE!"=="2" (
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    set /p DOCKER_PASSWORD="Enter Docker Hub password: "

    set /p IMAGE_TAG="Enter image tag (press Enter for 'latest'): "
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"

    set "FULL_IMAGE_NAME=docker.io/!DOCKER_USERNAME!/!IMAGE_NAME!"

    echo Logging in to Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo Docker Hub login failed
        exit /b 1
    )

    echo Building Docker image: !FULL_IMAGE_NAME!:!IMAGE_TAG!
    docker build -f Dockerfile -t "!FULL_IMAGE_NAME!:!IMAGE_TAG!" .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    echo Pushing Docker image: !FULL_IMAGE_NAME!:!IMAGE_TAG!
    docker push "!FULL_IMAGE_NAME!:!IMAGE_TAG!"
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo Successfully built and pushed: !FULL_IMAGE_NAME!:!IMAGE_TAG!
) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

endlocal
