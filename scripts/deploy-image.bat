@echo off
setlocal enabledelayedexpansion

echo ============================================
echo   ModResorts - Deploy to AWS EKS
echo ============================================
echo.

set /p AWS_REGION="Enter AWS Region (e.g. us-east-1): "
if "!AWS_REGION!"=="" (
    echo ERROR: AWS Region is required.
    exit /b 1
)

set /p CLUSTER_NAME="Enter EKS Cluster Name: "
if "!CLUSTER_NAME!"=="" (
    echo ERROR: EKS Cluster Name is required.
    exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI (e.g. 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): "
if "!IMAGE_URI!"=="" (
    echo ERROR: Docker image URI is required.
    exit /b 1
)

echo.
echo --- Optional Application Environment Variables ---
echo (Press Enter to skip any variable)
echo.

set /p WEATHER_API_KEY_VAL="Enter value for WEATHER_API_KEY (or press Enter to skip): "
set /p REDIS_HOST_VAL="Enter value for REDIS_HOST (or press Enter to skip): "
set /p REDIS_PORT_VAL="Enter value for REDIS_PORT (default: 6379, or press Enter to skip): "
set /p SERVICE_DISCOVERY_URL_VAL="Enter value for SERVICE_DISCOVERY_URL (or press Enter to skip): "

echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! in region: !AWS_REGION!
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo ERROR: Failed to configure kubectl for EKS cluster.
    exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (
    echo ERROR: Cannot connect to EKS cluster. Check your credentials and cluster name.
    exit /b 1
)

echo.
echo Updating Kubernetes manifests with image URI and environment variables...

copy kubernetes\deployment.yaml kubernetes\deployment.yaml.bak >nul

powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Set-Content kubernetes\deployment.yaml"

if "!WEATHER_API_KEY_VAL!"=="" (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{WEATHER_API_KEY}}', '' | Set-Content kubernetes\deployment.yaml"
) else (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{WEATHER_API_KEY}}', '!WEATHER_API_KEY_VAL!' | Set-Content kubernetes\deployment.yaml"
)

if "!REDIS_HOST_VAL!"=="" (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_HOST}}', 'localhost' | Set-Content kubernetes\deployment.yaml"
) else (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_HOST}}', '!REDIS_HOST_VAL!' | Set-Content kubernetes\deployment.yaml"
)

if "!REDIS_PORT_VAL!"=="" (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_PORT}}', '6379' | Set-Content kubernetes\deployment.yaml"
) else (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{REDIS_PORT}}', '!REDIS_PORT_VAL!' | Set-Content kubernetes\deployment.yaml"
)

if "!SERVICE_DISCOVERY_URL_VAL!"=="" (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SERVICE_DISCOVERY_URL}}', '' | Set-Content kubernetes\deployment.yaml"
) else (
    powershell -Command "(Get-Content kubernetes\deployment.yaml) -replace '{{SERVICE_DISCOVERY_URL}}', '!SERVICE_DISCOVERY_URL_VAL!' | Set-Content kubernetes\deployment.yaml"
)

echo.
echo Applying Kubernetes manifests...

echo   [1/4] Applying namespace...
kubectl apply -f kubernetes\namespace.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply namespace & exit /b 1 )

echo   [2/4] Applying deployment...
kubectl apply -f kubernetes\deployment.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply deployment & exit /b 1 )

echo   [3/4] Applying service...
kubectl apply -f kubernetes\service.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply service & exit /b 1 )

echo   [4/4] Applying ingress...
kubectl apply -f kubernetes\ingress.yaml
if !ERRORLEVEL! neq 0 ( echo ERROR: Failed to apply ingress & exit /b 1 )

echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/modresorts -n modresorts --timeout=300s
if !ERRORLEVEL! neq 0 (
    echo ERROR: Deployment rollout failed or timed out.
    echo Run: kubectl describe deployment modresorts -n modresorts
    exit /b 1
)

echo.
echo Verifying deployed resources...
kubectl get pods,svc,ingress -n modresorts

echo.
echo Restoring original deployment manifest...
copy kubernetes\deployment.yaml.bak kubernetes\deployment.yaml >nul
del kubernetes\deployment.yaml.bak >nul

echo.
echo ============================================
echo   Deployment complete!
echo   Namespace: modresorts
echo   Image: !IMAGE_URI!
echo ============================================
echo.
echo Rollback command (if needed):
echo   kubectl rollout undo deployment/modresorts -n modresorts

endlocal
