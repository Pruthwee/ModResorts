@echo off
setlocal enabledelayedexpansion

set "PROJECT_NAME=ModResorts"
set "IMAGE_NAME=modresorts"

echo === ModResorts Deploy to AWS EKS ===
echo Project name: %PROJECT_NAME%
echo Image name: %IMAGE_NAME%
echo.

REM Ask for AWS region
set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "

REM Ask for EKS cluster name
set /p CLUSTER_NAME="Enter EKS cluster name: "

REM Ask for image URI
set /p IMAGE_URI="Enter full image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): "

REM Ask for environment variables
echo.
echo Enter values for environment variables (or press Enter to skip):
echo.

set /p WEATHER_API_KEY_VALUE="Enter value for WEATHER_API_KEY (or press Enter to skip): "

REM Configure kubectl for EKS
echo.
echo Configuring kubectl for EKS cluster...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl
    exit /b 1
)

REM Check cluster connectivity
echo Checking cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Cluster check failed
    exit /b 1
)

REM Apply namespace
echo Applying namespace...
kubectl apply -f kubernetes/namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply namespace
    exit /b 1
)

REM Create Secret for credentials
echo Creating Kubernetes Secret for credentials...
set "SECRET_ARGS="
if NOT "!WEATHER_API_KEY_VALUE!"=="" (
    set "SECRET_ARGS=--from-literal=WEATHER_API_KEY=!WEATHER_API_KEY_VALUE!"
)

if NOT "!SECRET_ARGS!"=="" (
    kubectl create secret generic modresorts-secrets -n modresorts !SECRET_ARGS! --dry-run=client -o yaml | kubectl apply -f -
    if !ERRORLEVEL! neq 0 (
        echo Failed to create secret
        exit /b 1
    )
)

REM Replace placeholders in deployment.yaml
echo Updating deployment.yaml with image URI...

REM Use PowerShell to do the sed replacement
powershell -Command "(Get-Content kubernetes/deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes/deployment.yaml"
if !ERRORLEVEL! neq 0 (
    echo Failed to replace IMAGE_URI
    exit /b 1
)

REM Apply deployment
echo Applying deployment...
kubectl apply -f kubernetes/deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply deployment
    exit /b 1
)

REM Apply service
echo Applying service...
kubectl apply -f kubernetes/service.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply service
    exit /b 1
)

REM Apply ingress
echo Applying ingress...
kubectl apply -f kubernetes/ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply ingress
    exit /b 1
)

REM Wait for deployment rollout
echo Waiting for deployment rollout...
kubectl rollout status deployment/modresorts -n modresorts
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed
    exit /b 1
)

REM Show pods, services, and ingress
echo.
echo === Deployment Status ===
kubectl get pods,svc,ingress -n modresorts

REM Show application URL
echo.
echo === Application URL ===
echo https://modresorts.example.com

echo.
echo === Deployment Complete ===
echo To rollback: kubectl rollout undo deployment/modresorts -n modresorts

endlocal
