@echo off
setlocal enabledelayedexpansion

echo === Deploy ModResorts Image to Azure AKS (Windows) ===

set /p RESOURCE_GROUP="Enter Azure resource group name: "
if "%RESOURCE_GROUP%"=="" (
  echo Resource group is required
  exit /b 1
)

set /p CLUSTER_NAME="Enter Azure AKS cluster name: "
if "%CLUSTER_NAME%"=="" (
  echo AKS cluster name is required
  exit /b 1
)

set /p IMAGE_URI="Enter full Docker image URI (e.g., myregistry.azurecr.io/modresorts:latest): "
if "%IMAGE_URI%"=="" (
  echo Image URI is required
  exit /b 1
)

set /p DATABASE_URL="Enter DATABASE_URL (or press Enter to skip): "
set /p API_KEY="Enter API_KEY (or press Enter to skip): "

echo Configuring kubectl for AKS cluster...
az aks get-credentials --resource-group "%RESOURCE_GROUP%" --name "%CLUSTER_NAME%"
if %ERRORLEVEL% neq 0 (
  echo Failed to get AKS credentials
  exit /b 1
)

echo Verifying cluster connectivity...
kubectl cluster-info >NUL 2>&1
if %ERRORLEVEL% neq 0 (
  echo Failed to connect to Kubernetes cluster
  exit /b 1
)

echo Updating Kubernetes manifests with image and environment variables...

powershell -NoLogo -NoProfile -Command "(Get-Content 'kubernetes/deployment.yaml') -replace '{{IMAGE_URI}}', '%IMAGE_URI%' | Set-Content 'kubernetes/deployment.yaml'"

if not "%DATABASE_URL%"=="" (
  powershell -NoLogo -NoProfile -Command "(Get-Content 'kubernetes/deployment.yaml') -replace '{{DATABASE_URL}}', '%DATABASE_URL%' | Set-Content 'kubernetes/deployment.yaml'"
) else (
  powershell -NoLogo -NoProfile -Command "(Get-Content 'kubernetes/deployment.yaml') -replace '{{DATABASE_URL}}', '' | Set-Content 'kubernetes/deployment.yaml'"
)

if not "%API_KEY%"=="" (
  powershell -NoLogo -NoProfile -Command "(Get-Content 'kubernetes/deployment.yaml') -replace '{{API_KEY}}', '%API_KEY%' | Set-Content 'kubernetes/deployment.yaml'"
) else (
  powershell -NoLogo -NoProfile -Command "(Get-Content 'kubernetes/deployment.yaml') -replace '{{API_KEY}}', '' | Set-Content 'kubernetes/deployment.yaml'"
)

echo Applying Kubernetes manifests...

kubectl apply -f kubernetes/namespace.yaml
if %ERRORLEVEL% neq 0 (
  echo Failed to apply namespace manifest
  exit /b 1
)

kubectl apply -f kubernetes/deployment.yaml
if %ERRORLEVEL% neq 0 (
  echo Failed to apply deployment manifest
  exit /b 1
)

kubectl apply -f kubernetes/service.yaml
if %ERRORLEVEL% neq 0 (
  echo Failed to apply service manifest
  exit /b 1
)

kubectl apply -f kubernetes/ingress.yaml
if %ERRORLEVEL% neq 0 (
  echo Failed to apply ingress manifest
  exit /b 1
)

echo Waiting for deployment rollout...
kubectl rollout status deployment/modresorts -n modresorts

if %ERRORLEVEL% neq 0 (
  echo Deployment rollout failed
  exit /b 1
)

echo Current resources in namespace 'modresorts':
kubectl get pods,svc,ingress -n modresorts

echo If using the sample ingress, the application should be available ^(once DNS is configured^) at:
echo   http://modresorts.example.com/

echo Deployment completed successfully.

endlocal
