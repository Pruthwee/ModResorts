@echo off
setlocal enabledelayedexpansion

echo === ModResorts Docker Build & Push (Windows) ===

set /p PROJECT_NAME="Enter project name [ModResorts]: "
if "%PROJECT_NAME%"=="" set PROJECT_NAME=ModResorts

for /f "delims=" %%A in ('powershell -NoLogo -NoProfile -Command "'%PROJECT_NAME%' .ToLower() -replace '[^a-z0-9]', '-' -replace '^-+', '' -replace '-+$', ''"') do set IMAGE_NAME=%%A
if "%IMAGE_NAME%"=="" set IMAGE_NAME=modresorts

echo Select container registry type:
echo   1^) Azure Container Registry ^(ACR^)
echo   2^) Docker Hub
set /p REGISTRY_CHOICE="Enter choice [1/2]: "

set REGISTRY=
set REPOSITORY=

if "%REGISTRY_CHOICE%"=="1" (
  set /p ACR_NAME="Enter Azure ACR name (e.g., myregistry): "
  if "!ACR_NAME!"=="" (
    echo ACR name is required
    exit /b 1
  )
  set REGISTRY=!ACR_NAME!.azurecr.io
  set REPOSITORY=!IMAGE_NAME!
  echo Logging in to Azure ACR...
  az acr login --name !ACR_NAME!
  if !ERRORLEVEL! neq 0 (
    echo ACR login failed
    exit /b 1
  )
) else if "%REGISTRY_CHOICE%"=="2" (
  set /p DOCKER_USERNAME="Enter Docker Hub username: "
  if "!DOCKER_USERNAME!"=="" (
    echo Docker Hub username is required
    exit /b 1
  )
  set REGISTRY=docker.io
  set REPOSITORY=!DOCKER_USERNAME!/!IMAGE_NAME!
  echo Logging in to Docker Hub...
  set /p DOCKER_PASSWORD="Enter Docker Hub password: "
  echo !DOCKER_PASSWORD! | docker login --username !DOCKER_USERNAME! --password-stdin
  if !ERRORLEVEL! neq 0 (
    echo Docker Hub login failed
    exit /b 1
  )
) else (
  echo Invalid registry choice
  exit /b 1
)

set /p IMAGE_TAG="Enter image tag [latest]: "
if "%IMAGE_TAG%"=="" set IMAGE_TAG=latest
for /f "delims=" %%A in ('powershell -NoLogo -NoProfile -Command "'%IMAGE_TAG%' .ToLower() -replace '[^a-z0-9]', '-' -replace '^-+', '' -replace '-+$', ''"') do set IMAGE_TAG=%%A
if "%IMAGE_TAG%"=="" set IMAGE_TAG=latest

set FULL_IMAGE_NAME=%REGISTRY%/%REPOSITORY%:%IMAGE_TAG%

echo Building Docker image: %FULL_IMAGE_NAME%

docker build -f Dockerfile -t %FULL_IMAGE_NAME% .
if %ERRORLEVEL% neq 0 (
  echo Docker build failed
  exit /b 1
)

echo Pushing Docker image: %FULL_IMAGE_NAME%

docker push %FULL_IMAGE_NAME%
if %ERRORLEVEL% neq 0 (
  echo Docker push failed
  exit /b 1
)

echo Build ^& push completed successfully.

endlocal
