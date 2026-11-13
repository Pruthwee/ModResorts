@echo off
setlocal enabledelayedexpansion

set "PROJECT_NAME=ModResorts"

REM Sanitize image name using PowerShell
call powershell -Command "$n = '%PROJECT_NAME%'.ToLower(); $n = ($n -replace '[^a-z0-9]', '-'); $n = $n.Trim('-'); Write-Output $n" > "%TEMP%\modresorts_deploy_imagename.tmp"
set /p IMAGE_NAME=<"%TEMP%\modresorts_deploy_imagename.tmp"

echo === Deploying !IMAGE_NAME! to AWS EKS ===

REM Ask for AWS region
set /p AWS_REGION="Enter AWS region (e.g., us-east-1): "

REM Ask for EKS cluster name
set /p CLUSTER_NAME="Enter EKS cluster name: "

REM Ask for image URI
set /p IMAGE_URI="Enter full image URI (e.g., 123456789.dkr.ecr.us-east-1.amazonaws.com/modresorts:latest): "

REM Collect environment variables
echo.
echo Enter environment variable values for the deployment.
echo Press Enter to skip any variable (it will remain as a placeholder).
echo.

set /p SERVER_DISPLAY_NAME="Enter value for SERVER_DISPLAY_NAME (or press Enter to skip): "
set /p SERVER_FULL_NAME="Enter value for SERVER_FULL_NAME (or press Enter to skip): "

REM Collect credentials (without echoing)
echo.
echo Enter credential values for the Kubernetes Secret.
call :getPassword WEATHER_API_KEY
echo.

REM Configure kubectl for the EKS cluster
echo.
echo Configuring kubectl for EKS cluster...
call aws eks update-kubeconfig --region !AWS_REGION! --name !CLUSTER_NAME!
if !ERRORLEVEL! neq 0 (
    echo Failed to configure kubectl for EKS cluster
    exit /b 1
)

REM Verify cluster access
echo Verifying cluster access...
call kubectl cluster-info
if !ERRORLEVEL! neq 0 exit /b 1

REM Apply namespace
echo.
echo Applying namespace...
call powershell -Command "(gc kubernetes/namespace.yaml) -replace '{{NAMESPACE}}', '!IMAGE_NAME!' | Out-File -encoding ASCII kubernetes/namespace.yaml"
call kubectl apply -f kubernetes/namespace.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply namespace
    exit /b 1
)

REM Create Secret with credentials
echo.
echo Creating Kubernetes Secret for credentials...
call kubectl create secret generic !IMAGE_NAME!-secrets -n !IMAGE_NAME! --from-literal=weather-api-key="!WEATHER_API_KEY!" --dry-run=client -o yaml | kubectl apply -f -
if !ERRORLEVEL! neq 0 (
    echo Failed to create secret
    exit /b 1
)

REM Prepare deployment.yaml with placeholder replacements
echo.
echo Preparing deployment.yaml...
call powershell -Command "(gc kubernetes/deployment.yaml) -replace '{{NAMESPACE}}', '!IMAGE_NAME!' -replace '{{IMAGE_URI}}', '!IMAGE_URI!' -replace '{{SERVER_DISPLAY_NAME}}', '!SERVER_DISPLAY_NAME!' -replace '{{SERVER_FULL_NAME}}', '!SERVER_FULL_NAME!' | Out-File -encoding ASCII kubernetes/deployment.yaml"

REM Apply deployment
echo.
echo Applying deployment...
call kubectl apply -f kubernetes/deployment.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply deployment
    exit /b 1
)

REM Apply service
echo.
echo Applying service...
call powershell -Command "(gc kubernetes/service.yaml) -replace '{{NAMESPACE}}', '!IMAGE_NAME!' | Out-File -encoding ASCII kubernetes/service.yaml"
call kubectl apply -f kubernetes/service.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply service
    exit /b 1
)

REM Apply ingress
echo.
echo Applying ingress...
call powershell -Command "(gc kubernetes/ingress.yaml) -replace '{{NAMESPACE}}', '!IMAGE_NAME!' | Out-File -encoding ASCII kubernetes/ingress.yaml"
call kubectl apply -f kubernetes/ingress.yaml
if !ERRORLEVEL! neq 0 (
    echo Failed to apply ingress
    exit /b 1
)

REM Wait for deployment rollout
echo.
echo Waiting for deployment rollout...
call kubectl rollout status deployment/!IMAGE_NAME! -n !IMAGE_NAME!
if !ERRORLEVEL! neq 0 (
    echo Deployment rollout failed
    exit /b 1
)

REM Show resources
echo.
echo === Deployment Status ===
call kubectl get pods,svc,ingress -n !IMAGE_NAME!

REM Show application URL
echo.
echo === Application URL ===
echo http://!IMAGE_NAME!.example.com

echo.
echo === Deployment Complete ===
echo If you need to rollback, run: kubectl rollout undo deployment/!IMAGE_NAME! -n !IMAGE_NAME!

exit /b 0

:getPassword
set /p "%1=" <nul
call powershell -Command "$p = Read-Host -AsSecureString; [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($p))" > "%TEMP%\modresorts_deploy_pwd.tmp"
set /p %1=<"%TEMP%\modresorts_deploy_pwd.tmp"
exit /b 0
