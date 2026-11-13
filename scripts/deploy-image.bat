@echo off
setlocal enabledelayedexpansion

REM Project name
set "PROJECT_NAME=modresorts"

REM Ask for AWS region and EKS cluster name
set /p AWS_REGION="Enter AWS region: "
set /p CLUSTER_NAME="Enter EKS cluster name: "

REM Ask for the image URI
set /p IMAGE_URI="Enter full image URI (registry/repository:tag): "

REM Ask for environment variables
echo.
echo Enter environment variable values for the application.
echo Press Enter to skip (use placeholder default).
echo.

REM WEATHER_API_KEY (credential - goes to Secret)
set /p WEATHER_API_KEY="Enter WEATHER_API_KEY (or press Enter to skip): "

REM NAMING_PROVIDER_URL
set /p NAMING_PROVIDER_URL="Enter NAMING_PROVIDER_URL (or press Enter to skip): "

REM NAMING_FACTORY_INITIAL
set /p NAMING_FACTORY_INITIAL="Enter NAMING_FACTORY_INITIAL (or press Enter to skip): "

REM DATASOURCE_JNDI
set /p DATASOURCE_JNDI="Enter DATASOURCE_JNDI (or press Enter to skip): "

echo.
echo Configuring kubectl for EKS cluster: !CLUSTER_NAME! in region !AWS_REGION!...
aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (echo Failed to configure kubectl & exit /b 1)

echo Verifying cluster connectivity...
kubectl cluster-info
if !ERRORLEVEL! neq 0 (echo Cluster connectivity check failed & exit /b 1)

echo.
echo Applying namespace...
kubectl apply -f kubernetes/namespace.yaml
if !ERRORLEVEL! neq 0 (echo Namespace apply failed & exit /b 1)

REM Create Secret for credentials
if defined WEATHER_API_KEY (
    echo.
    echo Creating Kubernetes Secret for credentials...
    kubectl create secret generic !PROJECT_NAME!-secrets -n !PROJECT_NAME! --from-literal=weather-api-key="!WEATHER_API_KEY!" --dry-run=client -o yaml | kubectl apply -f -
    if !ERRORLEVEL! neq 0 (echo Secret creation failed & exit /b 1)
)

echo.
echo Applying deployment...
REM Replace placeholders in deployment.yaml using PowerShell
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{IMAGE_URI}}', '!IMAGE_URI!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
if !ERRORLEVEL! neq 0 (echo Failed to replace IMAGE_URI placeholder & exit /b 1)
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{NAMING_PROVIDER_URL}}', '!NAMING_PROVIDER_URL!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
if !ERRORLEVEL! neq 0 (echo Failed to replace NAMING_PROVIDER_URL placeholder & exit /b 1)
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{NAMING_FACTORY_INITIAL}}', '!NAMING_FACTORY_INITIAL!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
if !ERRORLEVEL! neq 0 (echo Failed to replace NAMING_FACTORY_INITIAL placeholder & exit /b 1)
powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{DATASOURCE_JNDI}}', '!DATASOURCE_JNDI!' | Out-File -encoding ASCII kubernetes/deployment.yaml"
if !ERRORLEVEL! neq 0 (echo Failed to replace DATASOURCE_JNDI placeholder & exit /b 1)

kubectl apply -f kubernetes/deployment.yaml
if !ERRORLEVEL! neq 0 (echo Deployment apply failed & exit /b 1)

echo.
echo Applying service...
kubectl apply -f kubernetes/service.yaml
if !ERRORLEVEL! neq 0 (echo Service apply failed & exit /b 1)

echo.
echo Applying ingress...
kubectl apply -f kubernetes/ingress.yaml
if !ERRORLEVEL! neq 0 (echo Ingress apply failed & exit /b 1)

echo.
echo Waiting for deployment rollout...
kubectl rollout status deployment/!PROJECT_NAME! -n !PROJECT_NAME!
if !ERRORLEVEL! neq 0 (echo Rollout failed & exit /b 1)

echo.
echo Deployment details:
kubectl get pods,svc,ingress -n !PROJECT_NAME!

echo.
echo Deployment complete!
echo.
echo To rollback if needed:
echo   kubectl rollout undo deployment/!PROJECT_NAME! -n !PROJECT_NAME!
echo   kubectl rollout history deployment/!PROJECT_NAME! -n !PROJECT_NAME!

endlocal
