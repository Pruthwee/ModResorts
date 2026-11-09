@echo off
setlocal enabledelayedexpansion

set "PROJECT_NAME=ModResorts"
set "NAME=modresorts"

echo === Deploy Image to AWS EKS for %NAME% ===
echo.

set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "
set /p CLUSTER_NAME="Enter EKS cluster name: "
set /p IMAGE_URI="Enter image URI (full path with registry and tag, e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): "

echo.
echo Enter values for environment variables (press Enter to skip):
set /p DB_HOST_VALUE="Enter value for DB_HOST (or press Enter to skip): "
set /p DB_PORT_VALUE="Enter value for DB_PORT (or press Enter to skip): "
set /p DB_NAME_VALUE="Enter value for DB_NAME (or press Enter to skip): "
set /p DB_USER_VALUE="Enter value for DB_USER (or press Enter to skip): "
set /p DB_PASSWORD_VALUE="Enter value for DB_PASSWORD (or press Enter to skip): "
set /p WEATHER_API_KEY_VALUE="Enter value for WEATHER_API_KEY (or press Enter to skip): "

echo Configuring kubectl for EKS cluster: %CLUSTER_NAME% in region %AWS_REGION%
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl
    exit /b 1
)

echo Checking cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo Failed to connect to cluster
    exit /b 1
)

echo Applying namespace...
kubectl apply -f kubernetes/namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply namespace
    exit /b 1
)

echo Creating Kubernetes secrets...
set "SECRET_ARGS="
if not "!WEATHER_API_KEY_VALUE!"=="" (
    set "SECRET_ARGS=!SECRET_ARGS! --from-literal=weather-api-key=!WEATHER_API_KEY_VALUE!"
)
if not "!DB_USER_VALUE!"=="" (
    set "SECRET_ARGS=!SECRET_ARGS! --from-literal=db-user=!DB_USER_VALUE!"
)
if not "!DB_PASSWORD_VALUE!"=="" (
    set "SECRET_ARGS=!SECRET_ARGS! --from-literal=db-password=!DB_PASSWORD_VALUE!"
)

if not "!SECRET_ARGS!"=="" (
    kubectl create secret generic %NAME%-secrets -n %NAME% !SECRET_ARGS! --dry-run=client -o yaml | kubectl apply -f -
    if !ERRORLEVEL! neq 0 (
        echo Failed to create secrets
        exit /b 1
    )
)

echo Updating deployment manifest with image URI and environment variables...
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{DB_HOST}}', '!DB_HOST_VALUE!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{DB_PORT}}', '!DB_PORT_VALUE!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{DB_NAME}}', '!DB_NAME_VALUE!' | Out-File -encoding ASCII kubernetes/deployment.yaml"

echo Applying deployment...
kubectl apply -f kubernetes/deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply deployment
    exit /b 1
)

echo Applying service...
kubectl apply -f kubernetes/service.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply service
    exit /b 1
)

echo Applying ingress...
kubectl apply -f kubernetes/ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply ingress
    exit /b 1
)

echo Waiting for deployment rollout...
kubectl rollout status deployment/%NAME% -n %NAME%
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed
    exit /b 1
)

echo.
echo === Deployment Status ===
kubectl get pods,svc,ingress -n %NAME%

echo.
echo Application URL: https://%NAME%.example.com

echo.
echo === Deployment Complete ===
echo If the deployment fails, you can rollback with:
echo   kubectl rollout undo deployment/%NAME% -n %NAME%

endlocal
