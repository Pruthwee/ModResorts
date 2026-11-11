@echo off
setlocal enabledelayedexpansion

REM Project name
set "PROJECT_NAME=ModResorts"

REM Ask for target platform
echo Select target platform:
echo 1. AWS ECR
echo 2. Docker Hub
set /p PLATFORM_CHOICE="Enter choice (1 or 2): "

REM Sanitize image name using PowerShell
call powershell -Command "$n = '%PROJECT_NAME%'.ToLower(); $n = ($n -replace '[^a-z0-9]', '-'); $n = $n.Trim('-'); Write-Output $n" > "%TEMP%\modresorts_imagename.tmp"
set /p IMAGE_NAME=<"%TEMP%\modresorts_imagename.tmp"

if "%PLATFORM_CHOICE%"=="1" (
    REM AWS ECR
    set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "
    set /p AWS_ACCOUNT_ID="Enter AWS account ID: "
    set "REGISTRY_URL=!AWS_ACCOUNT_ID!.dkr.ecr.!AWS_REGION!.amazonaws.com"
    set "ECR_REPO=!IMAGE_NAME!"
    set /p IMAGE_TAG="Enter image tag (press Enter for latest): "
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"
    REM Sanitize tag using PowerShell
    call powershell -Command "$t = '!IMAGE_TAG!'; $t = ($t -replace '[^a-z0-9._-]', '-'); $t = $t.Trim('-'); Write-Output $t" > "%TEMP%\modresorts_imagetag.tmp"
    set /p IMAGE_TAG=<"%TEMP%\modresorts_imagetag.tmp"
    set "FULL_IMAGE_NAME=!REGISTRY_URL!/!ECR_REPO!:!IMAGE_TAG!"

    REM Login to ECR
    echo Logging in to AWS ECR...
    call aws ecr get-login-password --region !AWS_REGION! | docker login --username AWS --password-stdin !REGISTRY_URL!
    if !ERRORLEVEL! neq 0 (
        echo ECR login failed
        exit /b 1
    )

    REM Create repository if it doesn't exist
    echo Checking if ECR repository exists...
    call aws ecr describe-repositories --repository-names !ECR_REPO! --region !AWS_REGION! >nul 2>&1
    if !ERRORLEVEL! neq 0 (
        echo Creating ECR repository...
        call aws ecr create-repository --repository-name !ECR_REPO! --region !AWS_REGION!
    )

    REM Build
    echo Building Docker image...
    docker build -f Dockerfile -t !FULL_IMAGE_NAME! .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    REM Push
    echo Pushing Docker image...
    docker push !FULL_IMAGE_NAME!
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo Image pushed successfully: !FULL_IMAGE_NAME!

) else if "%PLATFORM_CHOICE%"=="2" (
    REM Docker Hub
    set /p DOCKER_USERNAME="Enter Docker Hub username: "
    call :getPassword DOCKER_PASSWORD
    set /p IMAGE_TAG="Enter image tag (press Enter for latest): "
    if "!IMAGE_TAG!"=="" set "IMAGE_TAG=latest"
    REM Sanitize tag using PowerShell
    call powershell -Command "$t = '!IMAGE_TAG!'; $t = ($t -replace '[^a-z0-9._-]', '-'); $t = $t.Trim('-'); Write-Output $t" > "%TEMP%\modresorts_imagetag.tmp"
    set /p IMAGE_TAG=<"%TEMP%\modresorts_imagetag.tmp"
    set "FULL_IMAGE_NAME=!DOCKER_USERNAME!/!IMAGE_NAME!:!IMAGE_TAG!"

    REM Login to Docker Hub
    echo Logging in to Docker Hub...
    echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
    if !ERRORLEVEL! neq 0 (
        echo Docker Hub login failed
        exit /b 1
    )

    REM Build
    echo Building Docker image...
    docker build -f Dockerfile -t !FULL_IMAGE_NAME! .
    if !ERRORLEVEL! neq 0 (
        echo Docker build failed
        exit /b 1
    )

    REM Push
    echo Pushing Docker image...
    docker push !FULL_IMAGE_NAME!
    if !ERRORLEVEL! neq 0 (
        echo Docker push failed
        exit /b 1
    )

    echo Image pushed successfully: !FULL_IMAGE_NAME!

) else (
    echo Invalid choice. Exiting.
    exit /b 1
)

exit /b 0

:getPassword
set /p "%1=" <nul
call powershell -Command "$p = Read-Host -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))" > "%TEMP%\modresorts_pwd.tmp"
set /p %1=<"%TEMP%\modresorts_pwd.tmp"
exit /b 0
